package parking_sensor_test;

import java.util.LinkedList;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.cdtemplar.parking_sensor.*;

public class SensorServerHandler extends SimpleChannelInboundHandler<String> {
    
	LinkedList<CNetgate> cngList = new LinkedList<CNetgate>();
	LinkedList<CSensorValues> csvList = new LinkedList<CSensorValues>();
	CNetgate GetTheNetgate(int nID)
	{
		for(int i=0; i<cngList.size(); i++)
		{
			CNetgate ng = cngList.get(i);
			if(ng.CID == nID)
				return ng;
		}
		return null;
	}
	CSensorValues GetTheSensorValues(int nID)
	{
		for(int i=0; i<csvList.size(); i++)
		{
			CSensorValues ng = csvList.get(i);
			if(ng.ID == nID)
				return ng;
		}
		return null;
	}
    /*
     * 
     * 覆盖 channelActive 方法 在channel被启用的时候触发 (在建立连接的时候)
     * */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        
        System.out.println("RamoteAddress : " + ctx.channel().remoteAddress() + " active !");
        
        ctx.writeAndFlush( "OK\r\n");
        
        super.channelActive(ctx);
    }

	@Override
	protected void messageReceived(ChannelHandlerContext arg0, String arg1) throws Exception {
		// TODO 自动生成的方法存根
		 //System.out.println("Rec : " + arg1 );
		 int nStart = arg1.indexOf("{");
		 if(nStart >= 0)
			 arg1 = arg1.substring(nStart);
		 else
			 return;
		 CNetGateMsg smv2 = SensorInterface.GetSiteMsg(arg1);
		if(smv2 != null)
		{
			if(smv2.VAR == 0)	//版本0,只返回OK
			{
				arg0.writeAndFlush("OK\r\n");
			}
			else			//新版本的网关,具有数据存储功能
			{
				CNetgate ng = GetTheNetgate(smv2.CID);		//网关当前信息,一般应保存在数据库中,但本程序中直接放在了内存中,而且以第一次的数据做为网关的指针值,不会读取历史数据
				if(ng == null)
				{
					ng = new CNetgate();
					ng.CID = smv2.CID;
					ng.POT = smv2.POT + smv2.SensorNum();
					cngList.add(ng);
					
					if(smv2.CRC != -1)
					{
						arg0.writeAndFlush(SensorInterface.GetReadString(-1));	
					}
				}
				else
				{
					if(smv2.CRC != -1)
					{
						if(smv2.PD == ng.POT)
						{
							arg0.writeAndFlush(SensorInterface.GetReadString(-1));
							ng.POT += smv2.SensorNum();
							ng.POT %= SensorInterface.getMaxPoint();	//防止超过最大值
							
							arg0.writeAndFlush(SensorInterface.GetReadString(-1));
						}
						else
						{
							arg0.writeAndFlush(SensorInterface.GetReadString(ng.POT));
							return;			//不连接的数据不做处理
						}


					}
				}
				ng.POT %= SensorInterface.getMaxPoint();	//防止超过最大值
				arg0.writeAndFlush(SensorInterface.GetReadString(ng.POT));
			}
			
			System.out.println("\r\n数据结果================================== " + smv2.CID);
			for(int i=0; i<smv2.SensorNum(); i++)
			{
				
				CSensorValues csvGet = smv2.getSensorValues(i);		//车检器信息,一般应保存在数据库中,但本程序中直接放在了内存中,而且以第一次的数据做为车检器的初值
				if(csvGet.ID > 0)	//只处理ID>0的数据
				{
					CSensorValues csv = GetTheSensorValues(csvGet.ID);
					
					if(csv == null)
					{
						csv = csvGet;
						csv.X0 = csv.X;
						csv.Y0 = csv.Y;
						csv.Z0 = csv.Z;
					
						csv.BusyRate = 0;
						csvList.add(csv);
					}
					else
					{
						csv.OnUpdate(csvGet.X, csvGet.Y, csvGet.Z, csvGet.D);	//更新车检器信息,并计算有车概率
					}
					System.out.println("\r\n车检器数据:" + csv);
				}
			}
			System.out.println("\r\n========================================");
		}
	}

}

package parking_sensor_test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class SensorServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        
        ByteBuf buf = Unpooled.copiedBuffer("\r".getBytes());	//用“\r”做为分隔符
        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, buf));


        // 字符串解码 和 编码
        //pipeline.addLast(new LineBasedFrameDecoder(1024));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());
        


        // 自己的逻辑Handler
        pipeline.addLast("handler", new SensorServerHandler());
        

    }
}
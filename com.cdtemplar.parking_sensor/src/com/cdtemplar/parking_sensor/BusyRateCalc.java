// The MIT License (MIT)
// Copyright (C) 2016 by Lixiong <lx@cdtemplar.com>,http://www.cdtemplar.com
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.cdtemplar.parking_sensor;


public class BusyRateCalc {
	static int[][] m_nssPointMag = {
		{0, 1}, 					//0 ~ 1%
	    {10, 1}, 
	    {40, 10},
	    {120, 83},
	    {150, 98},
	    {200, 99},						//200 ~ 99%
	    {300, 99},	
	};
	
	static int[][] m_nssPointIR = {
		{0, 99}, 			//0 ~ 99%
	    {10, 99}, 
	    {60, 90},
	    {80, 55},
	    {100, 1},				//100 ~ 1%	
	    {110, 1}
	};
	
	/**
	 * ��ȡ����
	 * @param nssPoint
	 * @param nIn
	 * @return
	 */
	public static int ReadTable2Value(int[][] nssPoint, int nIn){
	    int nStart = 0;
	    if (nssPoint.length < 2)
	        return 0;
	    if (nIn > nssPoint[0][0]) {
	        for (nStart = 0; nStart < nssPoint.length - 2; nStart++) {
	            if (nIn > nssPoint[nStart][0] && nIn <= nssPoint[nStart + 1][0]) {
	                break;
	            }
	        }
	    }
	    int nn = (nIn - nssPoint[nStart][0]) * (nssPoint[nStart][1] - nssPoint[nStart + 1][1]) / (nssPoint[nStart][0] - nssPoint[nStart + 1][0]);
	    nn += nssPoint[nStart][1];
	    return nn;
	}
	
	/**
	 * ����0~1000��С��500��ʾ�޳� ���ڵ���500��ʾ�г�
	 * @param nMag �ų��仯ֵ nMag=sqrt((X-X0)^2 + (Y-Y0)^2 + (Z-Z0)^2)
	 * @param nIR �������ֵ
	 * @return
	 */
	public static int CalcProbability(int nMag, int nIR) {
			if(nMag < 100)
				return 900;
	    int nMagL = ReadTable2Value(m_nssPointMag, nMag);
	    int nIRL = ReadTable2Value(m_nssPointIR, nIR);
	    int nn = nMagL * 55 + nIRL * 45;
	    return (nn + 5) / 10;
	}
	/**
	 * ����0~1000��С��500��ʾ�޳� ���ڵ���500��ʾ�г�
	 * @param nMagLast ԭ�ų�
	 * @param nMag 		��ǰ�ų�
	 * @param nIRLast  ԭ�������ֵ 
	 * @param nIR �������ֵ
	 * @param nRateLast	ԭ�ۺ��г�����
	 * @param nRate ��ǰ��λ����
	 * @return
	 */
	public static int CalcProbabilityAmend(int nMagLast,int nMag, int nIRLast, int nIR, int nRateLast, int nRate) {
	
	    if (nRateLast >= 500)   //ԭ״̬�г�
	    {
	        if ( Math.abs(nMag - nMagLast) > 12 && nIR - nIRLast > 10)  //�شű仯����������
	        {
	        	nRate -= 200;
	        }
	        else
	        {
	        	nRate += 200;
	        }
	    }
	    else
	    {
	        if (Math.abs(nMag - nMagLast) > 12 && nIRLast - nIR > 10)  //�شű仯�������С
	        {
	        	nRate += 200;
	        }
	        else
	        {
	        	nRate -= 200;
	        }
	    }
        if (nRate > 999)
        	nRate = 999;
        if (nRate < 1)
        	nRate = 1;
        
        return nRate;
	}
	
	public static int IsDiffMag(int X,int Y,int Z){
		if(Math.abs(X) > 12)
			return 1;
		if(Math.abs(Y) > 12)
			return 1;
		if(Math.abs(Z) > 12)
			return 1;
		return 0;
	}
	/**
	 * ����0~1000��С��500��ʾ�޳� ���ڵ���500��ʾ�г�
	 * @param nMagLast ԭ�ų�
	 * @param nMag 		��ǰ�ų�
	 * @param nIRLast  ԭ�������ֵ 
	 * @param nIR �������ֵ
	 * @param nRateLast	ԭ�ۺ��г�����
	 * @param nRate ��ǰ��λ����
	 * @return
	 */
	public static int CalcProbabilityAmend2(int nIsDiffMag, int nIRLast, int nIR, int nRateLast, int nRate) {
	
	    if (nRateLast >= 500)   //ԭ״̬�г�
	    {
	        if ( nIsDiffMag > 0 && nIR - nIRLast > 20)  //�شű仯����������
	        {
	        	nRate -= 100;
	        }
	        else
	        {
	        	nRate += 100;
	        }
	    }
	    else
	    {
	        if (nIsDiffMag > 0 && nIRLast - nIR > 20)  //�شű仯�������С
	        {
	        	nRate += 100;
	        }
	        else
	        {
	        	nRate -= 100;
	        }
	    }
        if (nRate > 999)
        	nRate = 999;
        if (nRate < 1)
        	nRate = 1;
        
        return nRate;
	}
}



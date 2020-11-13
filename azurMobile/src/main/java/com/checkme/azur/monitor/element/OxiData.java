package com.checkme.azur.monitor.element;

import com.checkme.azur.monitor.utils.LogUtils;
import com.checkme.azur.monitor.utils.NumUtils;

public class OxiData implements GeneralData{
	
	public static final int PKG_LENGTH = 16;
	protected static final int DATA_LENGTH = 5;
	
	private Float[] datas = new Float[DATA_LENGTH];
	private int spo2;
	private int pr;
	private float pi;
	private int pFlag;
	private int result;
	
	public OxiData() {
		super();
		// TODO Auto-generated constructor stub
	}

	public OxiData(byte[] buf) {
		if (buf==null || buf.length!=PKG_LENGTH) {
			LogUtils.e("oxi buf length err");
			return;
		}
		
		int index = 0;
		
		//Decode Oxi wave data
		for (int i = 0; i < DATA_LENGTH; i++) {
			datas[i] = (float)(NumUtils.bbTos(buf[index++], buf[index++]));
		}
		
		//Decode results
		pr = NumUtils.bbToi(buf[index++], buf[index++]);
		spo2 = NumUtils.bToi(buf[index++]);
		pi = (float)NumUtils.bToi(buf[index++])/10;
		pFlag = buf[index++];
		result = buf[index++];
	}


	@Override
	public Float[] getDatas() {
		// TODO Auto-generated method stub
		return datas;
	}


	public int getSpo2() {
		return spo2;
	}


	public int getPr() {
		return pr;
	}


	public float getPi() {
		return pi;
	}


	public int getpFlag() {
		return pFlag;
	}


	public int getResult() {
		return result;
	}
	
}

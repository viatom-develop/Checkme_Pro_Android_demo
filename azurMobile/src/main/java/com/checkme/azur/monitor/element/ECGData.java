package com.checkme.azur.monitor.element;

import com.checkme.azur.monitor.utils.LogUtils;
import com.checkme.azur.monitor.utils.NumUtils;


public class ECGData implements GeneralData{
	
	public static final int PKG_LENGTH = 20;
	protected static final int DATA_LENGTH = 5;
	
	private Float[] datas = new Float[DATA_LENGTH];
	private int hr;
	private int qrs;
	private float st;
	private int pvcs;
	private int rFlag;
	private int result;
	
	public ECGData(byte[] buf) {
		if (buf==null || buf.length!=PKG_LENGTH) {
			LogUtils.e("ecg buf length err");
			return;
		}
		
		int index = 0;
		
		//Decode ECG wave data
		for (int i = 0; i < DATA_LENGTH; i++) {
			datas[i] = (float)(NumUtils.bbTos(buf[index++], buf[index++]));
		}
		
		//Decode results
		hr = NumUtils.bbToi(buf[index++], buf[index++]);
		qrs = NumUtils.bbToi(buf[index++], buf[index++]);
		st = ((float)NumUtils.bbToi(buf[index++], buf[index++])) / 100;
		pvcs = NumUtils.bbToi(buf[index++], buf[index++]);
		rFlag = buf[index++];
		result = buf[index++];
	}
	
	@Override
	public Float[] getDatas() {
		// TODO Auto-generated method stub
		return datas;
	}

	public int getHr() {
		return hr;
	}

	public int getQrs() {
		return qrs;
	}

	public float getSt() {
		return st;
	}

	public int getPvcs() {
		return pvcs;
	}

	public int getrFlag() {
		return rFlag;
	}

	public int getResult() {
		return result;
	}
	
	
}

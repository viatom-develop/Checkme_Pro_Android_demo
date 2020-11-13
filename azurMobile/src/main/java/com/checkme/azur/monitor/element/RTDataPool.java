package com.checkme.azur.monitor.element;

import com.checkme.azur.monitor.utils.LogUtils;
import com.checkme.azur.monitor.utils.NumUtils;

import java.util.LinkedList;
import java.util.List;

public class RTDataPool {

	//Package flags
	private static final int ECG_PKG_FLAG = 1;
	private static final int OXI_PKG_FLAG = 2;
	private static final int OTHER_PKG_FLAG = 0xF1;
	private static final int CONTROL_PKG_FLAG = 0xF2;

	// A package must contains front and data size and CRC (4byte)
	protected static final int MIN_PKG_LENGTH = 4;

	private List<ECGData> ecgDatas;
	private List<OxiData> oxiDatas;
	private List<OtherData> otherDatas;

	public RTDataPool() {
		super();
		// TODO Auto-generated constructor stub
		ecgDatas = new LinkedList<ECGData>();
		oxiDatas = new LinkedList<OxiData>();
		otherDatas = new LinkedList<OtherData>();
	}

	public void addDatas(byte[] buf) {
		if (buf==null || buf.length<MIN_PKG_LENGTH) {
			LogUtils.e("RTDataPool pkg length err");
			return;
		}

		//Check buf front and data chunk length
		int index = 0, dataLength = 0;
		if (buf[index++]!=(byte)0xA5 || buf[index++]!=(byte)0x5A) {
			LogUtils.e("RTDataPool front err");
			return;
		}else if ((dataLength=buf[index++]) < 0) {
			LogUtils.e("RTDataPool data length err");
			return;
		}else if (!isCRCNumRight(buf)) {
			LogUtils.e("RTDataPool CRC err");
			return;
		}

		//Decode real data
		while (index < buf.length-2) { //-2, not contains CRC check number
			int dataFlag = NumUtils.bToi(buf[index++]);

			if ((dataFlag==ECG_PKG_FLAG) && (index+ECGData.PKG_LENGTH<=buf.length)) {
				byte[] ecgBuf = new byte[ECGData.PKG_LENGTH];
				System.arraycopy(buf, index, ecgBuf, 0, ecgBuf.length);
				ECGData ecgData = new ECGData(ecgBuf);
				synchronized (ecgDatas) {
					//大于10不添加，丢点
					if (ecgDatas.size() < 10) {
						ecgDatas.add(ecgData);
					}
					//	LogUtils.d("ECG数据列表长：" + ecgDatas.size());
				}
				index += ECGData.PKG_LENGTH;

			}else if ((dataFlag==OXI_PKG_FLAG) && (index+OxiData.PKG_LENGTH<=buf.length)) {
				byte[] oxiBuf = new byte[OxiData.PKG_LENGTH];
				System.arraycopy(buf, index, oxiBuf, 0, oxiBuf.length);
				OxiData oxiData = new OxiData(oxiBuf);
				synchronized (oxiDatas) {
					//大于10不添加，丢点
					if (oxiDatas.size() < 10) {
						oxiDatas.add(oxiData);
						//	LogUtils.d("OXI数据列表长：" + oxiDatas.size());
					}
				}
				index += OxiData.PKG_LENGTH;

			}else if ((dataFlag==OTHER_PKG_FLAG) && (index+OtherData.PKG_LENGTH<=buf.length)) {
				byte[] otherBuf = new byte[OtherData.PKG_LENGTH];
				System.arraycopy(buf, index, otherBuf, 0, otherBuf.length);
				OtherData otherData = new OtherData(otherBuf);
				synchronized (otherDatas) {
					//防止数据池爆满，丢掉一些数据。
					if (otherDatas.size() < 10) {
						//大于10不添加，丢点
						otherDatas.add(otherData);
					}
				}
				index += OtherData.PKG_LENGTH;
			}else if ((dataFlag==CONTROL_PKG_FLAG) && (index+ControlData.CONTROL_PKG_LENGTH<=buf.length)) {

				index += ControlData.CONTROL_PKG_LENGTH;

			}else {
				//	LogUtils.e("No correct data to add to RTDataPool");
				break;
			}
		}
	}

	public List<ECGData> getEcgDatas() {
		return ecgDatas;
	}

	public List<OxiData> getOxiDatas() {
		return oxiDatas;
	}

	public List<OtherData> getOtherDatas() {
		return otherDatas;
	}

	private boolean isCRCNumRight(byte[] buf) {

		return true;
	}

}

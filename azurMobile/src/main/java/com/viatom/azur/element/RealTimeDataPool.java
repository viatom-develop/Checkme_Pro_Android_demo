package com.viatom.azur.element;

import java.util.ArrayList;
import java.util.List;

public class RealTimeDataPool {

	public static final String REAL_TIME_DATA_POOL_LOCK = "REAL_TIME_DATA_POOL_LOCK";

	//	List<ECGRealTimeItem> ecgRealTimeItems;
	ECGDataPool ecgDataPool;
	Spo2DataPool spo2DataPool;

	public RealTimeDataPool() {
		super();
		// TODO Auto-generated constructor stub
		ecgDataPool = new ECGDataPool();
		spo2DataPool = new Spo2DataPool();
	}

	public ECGDataPool getEcgDataPool() {
		return ecgDataPool;
	}

	public Spo2DataPool getSpo2DataPool() {
		return spo2DataPool;
	}

	public interface CommonDataPoolInterface {
		public List<Integer> getDataList();
	}

	public class ECGDataPool implements CommonDataPoolInterface{
		private List<Integer> hrList;
		private List<Integer> dataList;

		//warning 测试
		public ECGDataPool() {
			super();
			// TODO Auto-generated constructor stub
			hrList = new ArrayList<Integer>();
			dataList = new ArrayList<Integer>();
		}
		public List<Integer> getHrList() {
			return hrList;
		}
		public List<Integer> getDataList() {
			return dataList;
		}
	}

	public class Spo2DataPool implements CommonDataPoolInterface{
		private List<Integer> spo2List;
		private List<Integer> dataList;

		//warning 测试
		public Spo2DataPool() {
			super();
			// TODO Auto-generated constructor stub
			spo2List = new ArrayList<Integer>();
			dataList = new ArrayList<Integer>();
		}
		public List<Integer> getSpo2List() {
			return spo2List;
		}
		public List<Integer> getDataList() {
			return dataList;
		}
	}
}



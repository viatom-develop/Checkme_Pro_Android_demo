package com.viatom.azur.measurement;

import java.util.ArrayList;
import java.util.List;

import com.viatom.azur.utils.LogUtils;

import android.util.Log;

/**
 * The internal item of Sleep Monitor
 * 
 * @author zouhao
 */
public class SLMInnerItem {

	// PR value list
	private List<Integer> prList;
	// SPO2 value list
	private List<Integer> spo2List;

	public int[] getSpo2Dat() {
		return spo2Dat;
	}

	public void setSpo2Dat(int[] spo2Dat) {
		this.spo2Dat = spo2Dat;
	}

	private int[] spo2Dat;

	public SLMInnerItem(byte[] buf) {

		if (buf.length % MeasurementConstant.SLM_DETAIL_ITEM_LENGTH != 0) {
			LogUtils.d("SLM Inner buffer length error");
			return;
		}
//		Log.d("CD", buf.length+"");
		prList = new ArrayList<Integer>();
		spo2List = new ArrayList<Integer>();
		for (int i = 0; i < buf.length; i += 2) {
			spo2List.add(Integer.valueOf(buf[i] & 0xFF));
			prList.add(Integer.valueOf(buf[i + 1] & 0xFF));
		}
		spo2Dat=new int[spo2List.size()];
		for (int i=0;i<spo2List.size();i++) {
			spo2Dat[i]=spo2List.get(i);
		}
	}

	public List<Integer> getPrList() {
		return prList;
	}

	public List<Integer> getSpo2List() {
		return spo2List;
	}

	/**
	 * Get spo2 list after resampling, and select the minimum in step
	 * 
	 * @param step
	 *            Sampling distance
	 * @return Spo2 list
	 */
	public List<Integer> getSimpleSpo2List(int step) {
		if (spo2List == null || step <= 0) {
			return null;
		}
		List<Integer> simpleSpo2List = new ArrayList<Integer>();
		int sampleNum = spo2List.size() / step;

		for (int i = 0; i < sampleNum; i++) {
			int lowest = 0xFF;
			for (int j = 0; j < Math.min(step, spo2List.size() - i * step); j++) {
				lowest = Math.min(lowest, spo2List.get(i * step + j));
			}
			simpleSpo2List.add(lowest);
		}

		return simpleSpo2List;
	}

	/**
	 * Get PR list after resampling, and calculate the average value in step
	 * 
	 * @param step
	 *            Sampling distance
	 * @return PR list
	 */
	public List<Integer> getSimplePrList(int step) {
		if (prList == null || step <= 0) {
			return null;
		}
		List<Integer> simplePrList = new ArrayList<Integer>();
		int sampleNum = prList.size() / step;

		for (int i = 0; i < sampleNum; i++) {
			int average = 0;
			int availableNum = 0;
			for (int j = 0; j < Math.min(step, prList.size() - i * step); j++) {
				if (prList.get(i * step + j) != 0xFF) {
					// Not the dump value
					availableNum++;
					average += prList.get(i * step + j);
				}
			}
			// If all value in step are dump value
			if (availableNum == 0) {
				simplePrList.add(0xFF);
			} else {
				simplePrList.add(average / availableNum);
			}
		}

		return simplePrList;
	}

	/**
	 * Get the max PR value
	 * 
	 * @return
	 */
	public int getMaxPR() {
		if (prList == null) {
			return 0;
		}

		Integer max = 0;
		for (int i = 0; i < prList.size(); i++) {
			if (prList.get(i).intValue() != 0xFF) {
				max = Math.max(max, prList.get(i));
			}
		}

		return max;
	}

}

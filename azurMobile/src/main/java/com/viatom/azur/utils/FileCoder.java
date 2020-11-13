package com.viatom.azur.utils;

import java.util.List;

import com.viatom.azur.measurement.CommonItem;

/**
 * Make file from original data
 * @author zouhao
 */
public class FileCoder {
	
	/**
	 * Make list file from item list
	 * @param items Any List that elements implements CommonItem interface
	 * @return Data buffer
	 */
	public static byte[] codeListFile(List<? extends CommonItem> items) {
		if (items == null || items.size() == 0) {
			return new byte[0];
		}
		
		int singleItemLength = ((CommonItem)items.get(0)).getDataBuf().length;  
		byte[] buf = new byte[items.size()*singleItemLength];
		for (int i = 0; i < items.size(); i++) {
			CommonItem item = (CommonItem)items.get(i);
			System.arraycopy(item.getDataBuf(), 0, buf, i*singleItemLength, singleItemLength);
		}
		return buf;
	}
	
}

package com.checkme.azur.utils;

import java.util.ArrayList;
import java.util.List;

import com.checkme.azur.measurement.CommonItem;

/**
 * Tools used to process CommonItem list, 
 * such as get downloaded list and delete the same item in the list. 
 * @author zouhao
 */
public class CommonItemFilter {
	
	/**
	 * Remove no download items of list
	 * @param inList Any list that elements implements CommonItem interface
	 * @return A list has been removed no download items
	 */
	public static List<CommonItem> filterDownloadedList(List<? extends CommonItem> inList) {
		if (inList == null) {
			return null;
		}
		
		List<CommonItem> outList = new ArrayList<CommonItem>();
		
		for (int j = 0; j < inList.size(); j++) {
			CommonItem item = (CommonItem)inList.get(j);
			if (item.isDownloaded()) {
				outList.add(item);
			}
		}
		
		return outList;
	}
	
	/**
	 * Remove same items of list
	 * Same items: Items measured at same time 
	 * @param inList Any list that elements implements CommonItem interface
	 */
	public static void removeSameItems(List<? extends CommonItem> inList) {
		if (inList == null) {
			return;
		}
		
		for (int i = 0; i < inList.size(); i++) {
			CommonItem item = (CommonItem)inList.get(i);
			for (int j = i+1; j < inList.size(); j++) {
				if (item.getDate().equals(((CommonItem)inList.get(j)).getDate())) {
					inList.remove(j);
					//Otherwise, the same will be missed two consecutive
					j --;
				}
			}
		}
	}
}

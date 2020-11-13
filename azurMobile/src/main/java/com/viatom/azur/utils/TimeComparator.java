package com.viatom.azur.utils;

import java.util.Comparator;
import java.util.Date;

import com.viatom.azur.measurement.CommonItem;

/**
 * Sort items by measuring time.
 * @author zouhao
 */
public class TimeComparator implements Comparator<CommonItem>{
	
	
	@Override
	public int compare(CommonItem item0, CommonItem item1) {
		// TODO Auto-generated method stub
		Date date0 = item0.getDate();
		Date date1 = item1.getDate();
		
		if (date0.before(date1)) {
			return 1;
		}else if (date0.after(date1)) {
			return -1;
		}
		
		return 0;
	}
}

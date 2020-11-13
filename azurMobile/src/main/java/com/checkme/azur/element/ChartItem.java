package com.checkme.azur.element;

import java.util.Date;

/**
 * @author zouhao
 *	Used to draw general chart
 */
public class ChartItem {
	public float value;
	public Date date;
	
	public ChartItem(float value, Date date) {
		super();
		this.value = value;
		this.date = date;
	}
}

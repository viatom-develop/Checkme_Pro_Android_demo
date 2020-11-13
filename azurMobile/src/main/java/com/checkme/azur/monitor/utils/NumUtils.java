package com.checkme.azur.monitor.utils;

public class NumUtils {
	
	/**
	 * Convert bytes to int
	 * @param b1
	 * @param b2
	 * @return
	 */
	public static int bbToi(byte b1, byte b2) {
		
		int val = (b1 & 0xFF) + ((b2 & 0xFF) << 8);
		return val;
	}
	
	/**
	 * Convert bytes to short
	 * @param b1
	 * @param b2
	 * @return
	 */
	public static short bbTos(byte b1, byte b2) {
		
		short val = (short)((b1 & 0xFF) + ((b2 & 0xFF) << 8));
		return val;
	}
	
	/**
	 * Convert byte to int
	 * @param b
	 * @return
	 */
	public static int bToi(byte b) {
		return b & 0xFF;
	}
}

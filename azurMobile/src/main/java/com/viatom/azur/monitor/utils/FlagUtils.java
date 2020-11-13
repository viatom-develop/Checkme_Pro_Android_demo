package com.viatom.azur.monitor.utils;

public class FlagUtils {

	public static FlagUtils flag = null ;

	public static FlagUtils getInstence(){

		if(flag == null){
			flag = new FlagUtils();
		}

		return flag;

	}
	//标识心电有时，只播放心电声音。  1 -- 心电声。  2  --血氧声   0   --都不响
	public int mark = 1 ;

}

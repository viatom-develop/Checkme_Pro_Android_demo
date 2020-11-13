package com.viatom.azur.monitor.element;

import android.content.Context;
import android.os.Environment;

import com.viatom.azur.monitor.bt.BTUtils;
import com.viatom.azur.monitor.utils.LogUtils;

import java.io.File;

/**
 * Storing common variables and constants
 *
 * @author zouhao
 *
 */
public class Constant {

	private static Context mContext;

	/**
	 * initialize variable
	 */
	public static void init(Context context) {
		if (context == null) {
			return;
		}
		mContext = context;
//		btConnectFlag = false;
	}

	/**
	 * initialize directory
	 */
	public static void initDir() {
		root = Environment.getExternalStorageDirectory();
		if (root == null) {
			root = Environment.getDataDirectory();
		}

		pic_dir = new File(root,"CheckmeMonitor/Picture");
		if (!pic_dir.exists()) {
			pic_dir.mkdir();
		}

		//Use default directory if the device is error

		dir = new File(root,"CheckmeMonitor/RecordedData" );

		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				LogUtils.d("Create dir failed");
			}
		}
		LogUtils.d("Current dir:" + dir.toString());
	}

	/**
	 *  销毁变量
	 */
	public static void destroyVariable() {
		binder = null;
	}

	/**
	 * Get string from resource
	 * @param id
	 * @return
	 */
	public static String getString(int id) {
		if (mContext == null) {
			return "";
		}
		return mContext.getResources().getString(id);
	}

	//Communication Protocol and file Protocol version
	public final static String SPCP_VER = "1.0";
	public final static String FILE_VER = "1.0";

	// Activity request
	public final static int REQUEST_TURN_ON_BT = 2001;
	public final static int REQUEST_COME_INTO_MONITOR = 2002;

	// Local directory
	public static File root;
	public static File dir;
	public static File pic_dir;

	// Network, sever address
	public static final String URL_GET_APP_PATCH =
			"http://198.57.171.197:8080/CheckmeMobileUpdate/GetPatchsServlet";
	public static final String SERVER_ADDRESS = "http://198.57.171.197:8080";

	// Global variable
//	public static boolean btConnectFlag = false;
	public static BTUtils.BTBinder binder;
	//定义的消息常量
	public static final int RE_PLAY = 1010 ;
	public static final int BACK_PLAY = 1011 ;
	public static final int PAUSE_PLAY = 1012 ;
	public static final int PLAY_TIME = 1013 ;
	public static final int PLAY_FNISH = 1003 ;
	public static final int ALERT_LIST = 1004 ;

	public static final int DELETE_ALL = 1014 ;
	public static final int DELETE = 1015 ;
	public static final int PLAY = 1016 ;
	public static final int START_JISHI = 1020 ;
	public static final int SCREEN_CUT = 1021 ;

}

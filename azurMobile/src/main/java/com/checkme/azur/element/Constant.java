package com.checkme.azur.element;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;

import com.checkme.azur.bluetooth.BTBinder;
import com.checkme.azur.measurement.DailyCheckItem;
import com.checkme.azur.measurement.ECGItem;
import com.checkme.azur.measurement.SLMItem;
import com.checkme.azur.measurement.SPO2Item;
import com.checkme.azur.measurement.TempItem;
import com.checkme.newazur.R;
import com.checkme.azur.measurement.SpotUser;
import com.checkme.azur.measurement.User;
import com.checkme.azur.tools.PreferenceUtils;
import com.checkme.azur.utils.LogUtils;

import org.xutils.common.Callback;

/**
 * Storing common variables and constants
 *
 * @author zouhao
 *
 */
public class Constant {


    public static Context mContext;

	/**
	 * initialize variable
	 */
	public static void init(Context context) {
		if (context == null) {
			return;
		}
		mContext = context;

		unit = PreferenceUtils.readIntPreferences(context, "UNIT");
		thermometerUnit = PreferenceUtils.readIntPreferences(context, "THERMOMETER");

		btConnectFlag = false;
		defaultUser = new User();
	}

	/**
	 * initialize directory
	 * @param deviceName
	 */
	public static void initDir(Context context, String deviceName) {
		root = Environment.getExternalStorageDirectory();
		if (root == null) {
			root = Environment.getDataDirectory();
		}

		pic_dir = new File(Environment.getExternalStorageDirectory(),"DCIM/Camera");
		if (!pic_dir.exists()) {
			pic_dir.mkdir();
		}

		download_dir = new File(Environment.getExternalStorageDirectory(), "CheckmeMobile");
		if (!download_dir.exists()) {
			download_dir.mkdir();
		}

		//Use default directory if the device is error
		if (deviceName==null || deviceName.equals("")) {
			dir = new File(root, "CheckmeMobile/UnknowDevice");
		}else {
			dir = new File(root,"CheckmeMobile/" + deviceName);
		}
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				LogUtils.d("Create dir failed");
			}
		}
		LogUtils.d("Current dir:" + dir.toString());

		initSharedDir();
	}

	/**
	 * initialize directory
	 * @param deviceName
	 */
	public static void initDir(String deviceName) {
		root = Environment.getExternalStorageDirectory();
		if (root == null) {
			root = Environment.getDataDirectory();
		}

		pic_dir = new File(Environment.getExternalStorageDirectory(),"DCIM/Camera");
		if (!pic_dir.exists()) {
			pic_dir.mkdir();
		}

		download_dir = new File(Environment.getExternalStorageDirectory(), "CheckmeMobile");
		if (!download_dir.exists()) {
			download_dir.mkdir();
		}

		//Use default directory if the device is error
		if (deviceName==null || deviceName.equals("")) {
			dir = new File(root, "CheckmeMobile/UnknowDevice");
		}else {
			dir = new File(root,"CheckmeMobile/" + deviceName);
		}
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				LogUtils.d("Create dir failed");
			}
		}
		LogUtils.d("Current dir:" + dir.toString());

		initSharedDir();
	}

	public static void initSharedDir() {
		shared_pic_dir = new File(Environment.getExternalStorageDirectory(),"DCIM/CheckmeMobile");
		if (!shared_pic_dir.exists()) {
			shared_pic_dir.mkdir();
		}
	}

	/**
	 *  销毁变量
	 */
	public static void destroyVariable() {
		userList = null;
		defaultUser = null;
		curUser = null;
		binder = null;
		spotUserList = null;
		defaultSpotUser = null;
		curSpotUser = null;
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
	public final static String FILE_VER_NEW = "1.1";
	public final static String FILE_VER_KEY = "file_ver";
	public final static int QT_VALUE = Integer.MIN_VALUE;

	// Message
	public final static int MSG_PART_FINISHED = 1002;
	public final static int MSG_FILE_NOTEXIST = 1003;
	public final static int MSG_BT_FIND = 1007;
	public final static int MSG_BT_CONNECTED = 1008;
	public final static int MSG_BT_CONNECT_TIMEOUT = 1009;
	public final static int MSG_BNDLC_CLICKED = 1012;
	public final static int MSG_BNECG_CLICKED = 1013;
	public final static int MSG_BNSPO2_CLICKED = 1014;
	public final static int MSG_BNTEMP_CLICKED = 1015;
	public final static int MSG_BNSLM_CLICKED = 1016;
	public final static int MSG_BNPED_CLICKED = 1017;
	public final static int MSG_BNABOUT_CKM_CLICKED = 1018;
	public final static int MSG_BNSETTINGS_CLICKED = 1019;
	public final static int MSG_BNDOWNLOAD_CLICKED = 1022;
	public final static int MSG_BNABOUT_APP_CLICKED = 1020;
	public final static int MSG_BNSPOT_CLICKED = 1021;
	public final static int MSG_BNBP_CLICKED = 1024;

	public final static int MSG_USER_CHOSED = 1031;
	public final static int MSG_GOTO_DLC_DETAIL = 1033;
	public final static int MSG_GOTO_ECG_DETAIL = 1034;
	public final static int MSG_GOTO_SLM_DETAIL = 1036;
	public final static int MSG_GOTO_CHOOSE_DEVICE = 1037;
	public final static int MSG_GOTO_SPOT_DETAIL = 1038;
	public final static int MSG_GOTO_DIALOG = 1039;

	public final static int MSG_DOWNLOAD_PART_FINISH = 1040;
	public final static int MSG_DOWNLOAD_FAILED = 1041;
	public final static int MSG_SHOW_UNNECESSARY_UPDATE = 1042;
	public final static int MSG_SHOW_SWITCH_LANGUAGE = 1043;
	public final static int MSG_SHOW_UPDATE_SUCCESS = 1044;
	public final static int MSG_SHOW_UPDATE_FAILED = 1045;
	public final static int MSG_BNUPDATE_CLICKED = 1046;
	public final static int MSG_SHOW_CANT_UPDATE_IN_OFFLINE = 1047;
	public final static int MSG_SHOW_UPDATE_WARNING = 1048;
	public final static int MSG_BACK_TO_LAST_FRAG = 1049;
	public final static int MSG_SHOW_CHANGE_LANGUAGE_SUCCESS = 1050;
	public final static int MSG_PING_SUCCESS = 1051;
	public final static int MSG_PING_FAILED = 1052;
	public final static int MSG_REPORT_BITMAP_CONVERTED = 1053;
	public final static int MSG_SAVE_BMP_SUCCESS = 1054;
	public final static int MSG_SAVE_BMP_FAILED = 1055;
	public final static int MSG_CHECKME_MOBILE_PATCH_EXIST = 1056;
	public final static int MSG_CHECKME_MOBILE_PATCH_NOT_EXIST = 1057;
	public final static int MSG_SHOW_POLICY_DIALOG = 1058;

//	public final static int MSG_SIGN_IN_SUCCESS = 1058;
//	public final static int MSG_SIGN_IN_FAIL = 1059;
//
//	public final static int MSG_UPLOAD_DLC_LIST = 1060;
//	public final static int MSG_UPLOAD_ECG_LIST = 1061;
//	public final static int MSG_UPLOAD_SPO2_LIST = 1062;
//	public final static int MSG_UPLOAD_SLM_LIST = 1063;
//	public final static int MSG_UPLOAD_TMP_LIST = 1064;

	public final static int MSG_PATIENT_SUCCESS = 1065;
	public final static int MSG_PATIENT_ERROR = 1066;
//	public final static int MSG_PATIENT_STARTED = 1067;
//	public final static int MSG_OBSERVATION_SUCCESS = 1068;
	public final static int MSG_OBSERVATION_ERROR = 1069;
//	public final static int MSG_OBSERVATION_STARTED = 1070;
//
//	public final static int MSG_UPLOAD_DAT = 1071;
	public final static int MSG_PARA_SYNC_SUCCESS = 1072;
	public final static int MSG_PARA_SYNC_FAILED = 1073;

	public final static int MSG_UPLOAD_TO_CLOUD = 1074;

	public final static int MSG_GET_INFO_FAILED = 1075;
	public final static int MSG_GO_TO_MONITOR = 1076;

	public final static int MSG_TRACE_CONNECT_SUCCESS = 1077;
	public final static int MSG_TRACE_CONNECT_FAILED = 1078;

	// Activity request
	public final static int REQUEST_TURN_ON_BT = 2001;
	public final static int REQUEST_COME_INTO_DLC = 2002;
	public final static int REQUEST_LOCATION_SETTINGS = 2004;

	// Data type
	public final static byte CMD_TYPE_USER_LIST = 1;
	public final static byte CMD_TYPE_DLC = 2;
	public final static byte CMD_TYPE_ECG_LIST = 3;
	public final static byte CMD_TYPE_SLM_LIST = 4;
	public final static byte CMD_TYPE_SPO2 = 5;
	public final static byte CMD_TYPE_TEMP = 6;
	public final static byte CMD_TYPE_ECG_NUM = 7;
	public final static byte CMD_TYPE_VOICE = 8;
	public final static byte CMD_TYPE_SLM_NUM = 9;
	public final static byte CMD_TYPE_BPCAL = 10;
	public final static byte CMD_TYPE_PED = 11;
	public final static byte CMD_TYPE_CKM_INFO = 12;
	public final static byte CMD_TYPE_VOICE_CONVERTED = 20;
	public final static byte CMD_TYPE_SPOT = 22;
	public final static byte CMD_TYPE_BP = 30;

	// DailyCheck detail
	public final static byte DLC_INFO_NONE = -1;
	public final static byte DLC_INFO_OXYGEN = 1;
	public final static byte DLC_INFO_BP_RE = 2;
	public final static byte DLC_INFO_BP_ABS = 3;
	public final static byte DLC_INFO_HR = 4;
	public final static byte DLC_INFO_PI = 5;
	public final static byte DLC_INFO_BP_NONE = 6;

	// ECG measurement mode
	public final static byte ECG_CHECK_MODE_HH = 1;
	public final static byte ECG_CHECK_MODE_HC = 2;
	public final static byte ECG_CHECK_MODE_1L = 3;
	public final static byte ECG_CHECK_MODE_12L = 4;
	public final static int ECG_DATA_SAMPLING_FREQUENCY = 500; //500Hz

	// BP Calibration type
	public final static byte BP_TYPE_NONE = 1; // No Calibration
	public final static byte BP_TYPE_RE = 2; // Relative Calibration
	public final static byte BP_TYPE_ABS = 3; // Absolutely Calibration

	// Units
	public final static int UNIT_METRIC = 0;
	public final static int UNIT_BRITISH = 1;
	public final static int THERMOMETER_C = 0;
	public final static int THERMOMETER_F = 1;

	// Share
	// Share
	public final static int SHARE_TYPE_LOCAL = 1;
	public final static int SHARE_TYPE_NET = 2;
	public final static int SHARE_TYPE_CLOUD = 3;

	//Mode
	public final static String CKM_MODE_HOME = "MODE_HOME";
	public final static String CKM_MODE_HOSPITAL = "MODE_HOSPITAL";
	public final static String CKM_MODE_MULTI = "MODE_MULTI";

	// Local directory
	public static File root;
	public static File dir;
	public static File pic_dir;
	public static File download_dir;
	public static File shared_pic_dir;

	// Network, sever address 119.29.77.15
//	public static final String URL_GET_LANGUAGE_LIST =
//
//			"http://119.29.77.15:8089/CheckmeUpdate/LanguageListServlet" ;
	//	"http://198.57.171.197:8080/CheckmeUpdate/LanguageListServlet";
//	public static final String URL_GET_CHECKME_PATCH =
//			"http://119.29.77.15:8089/CheckmeUpdate/GetPatchsServlet" ;
	//	"http://198.57.171.197:8080/CheckmeUpdate/GetPatchsServlet";
	public static final String URL_GET_APP_PATCH =
			"http://119.29.77.15:8089/CheckmeMobileUpdate/GetPatchsServlet";
	//	"http://198.57.171.197:8080/CheckmeMobileUpdate/GetPatchsServlet";
	public static final String SERVER_ADDRESS = "http://119.29.77.15:8089"; //"http://198.57.171.197:8080";

	public static final String UPDATE_ADDRESS="https://api.viatomtech.com.cn/update/ce/6632";
	// Resource array
	public final static int[] CHECK_MODE_IMG = { R.drawable.hand_hand,
			R.drawable.hand_chest, R.drawable.external1, R.drawable.external2 };
	public final static int[] RESULT_IMG = { R.drawable.smile, R.drawable.cry,
			R.drawable.none };
	public final static int[] TEMP_CHECK_MODE_IMG = { R.drawable.temp_head, R.drawable.temp_thing,
			R.drawable.none };
	public final static int[] VOICE_IMG = { R.drawable.none, R.drawable.voice };
	public final static int[] MONTH = {R.string.month_1, R.string.month_2, R.string.month_3,
			R.string.month_4, R.string.month_5, R.string.month_6, R.string.month_7,
			R.string.month_8, R.string.month_9, R.string.month_10, R.string.month_11,
			R.string.month_12 };
	public final static byte[] ECG_CHECK_MODE = { ECG_CHECK_MODE_HH,
			ECG_CHECK_MODE_HC, ECG_CHECK_MODE_1L, ECG_CHECK_MODE_12L };
	public final static int[] ICO_IMG = new int[] { R.drawable.ico1,
			R.drawable.ico2, R.drawable.ico3, R.drawable.ico4, R.drawable.ico5,
			R.drawable.ico6, R.drawable.ico7, R.drawable.ico8, R.drawable.ico9,
			R.drawable.ico10 };

	public static int[] ECG_STR_RESULT_LIST = { R.string.ecg_result_0, R.string.ecg_result_1,
			R.string.ecg_result_2, R.string.ecg_result_3, R.string.ecg_result_4,
			R.string.ecg_result_5, R.string.ecg_result_6, R.string.ecg_result_7,
			R.string.ecg_result_8};

	public final static int[] SPO2_STR_REULT_LIST = { R.string.spo2_result_0,
			R.string.spo2_result_1, R.string.spo2_result_2 };

	public final static int[] SLM_STR_REULT_LIST = { R.string.slm_result_0,
			R.string.slm_result_1, R.string.slm_result_2 };

	// Global variable
	public static boolean btConnectFlag = false;
	public static BTBinder binder;
	public static int unit;
	public static int thermometerUnit;
	public static String CKM_MODE = CKM_MODE_HOME;

	public static User[] userList;
	public static User curUser;
	public static User defaultUser;
	//spot
	public static SpotUser[] spotUserList;
	public static SpotUser curSpotUser;
	public static SpotUser defaultSpotUser;

	//PreferenceKey
	public final static String CURRENT_EMAIL="email";
	public final static String CURRENT_PASSWORD="password";
//	public final static String CURRENT_USER_ID = "user_id";
	public final static String CURRENT_USER_NAME = "name";
//	public final static String AUTO_SYNC = "auto_sync";
	public final static String SN = "SN";
	//JsonObjectKey
	public final static String SET_TIME = "SetTIME";
	//Cloud
	public final static String LOGIN_URL = "https://cloud.viatomtech.com/user/login";
	public final static String CLOUD_URL = "https://cloud.viatomtech.com/register";
	public final static String ACCESS_URL = "https://cloud.viatomtech.com";
	public final static String DELETE_URL = "https://cloud.viatomtech.com/setting";
	public final static String PATIENT_RESOURCE_URL = "https://cloud.viatomtech.com/patient";
//	public final static String SYSTEM_URL = "https://cloud.viatomtech.com";
	public final static String OBSERVATION_URL = "https://cloud.viatomtech.com/observation";
	public final static int CONNECT_TIMEOUT=90*1000;
//	public final static int READ_TIMEOUT=8*1000;
	public final static String SSL_NAME="2cloudviatomtechcom.crt";

	//SQLite name
	public final static String DB_ADDRESS = "Checkme.db";
	//cloud state
	public final static int NETWORK_NOT_AVAILABLE=0;
	public final static int UPLOADING=1;
	public final static int UPLOADED=2;
	//upload state
	public static int sUploadState=-1;

//	public static boolean isUpload=false;

	public static User sUploadUser;

	public static List<DailyCheckItem> uploadDLCList;
	public static List<ECGItem> uploadECGList;
	public static List<SPO2Item> uploadSPO2List;
	public static List<SLMItem> uploadSLMList;
	public static List<TempItem> uploadTMPList;

	public static int DLCQue=-1;
	public static int ECGQue=-1;
	public static int SPO2Que=-1;
	public static int SLMQue=-1;
	public static int TMPQue=-1;

	public static int userQue=-1;

	public static ArrayList<Callback.Cancelable> mCancelables=new ArrayList<>();
	public synchronized static void destroyListVal(){
		uploadDLCList=null;
		uploadECGList=null;
		uploadSPO2List=null;
		uploadSLMList=null;
		uploadTMPList=null;
		sUploadUser=null;
		DLCQue=-1;
		ECGQue=-1;
		SPO2Que=-1;
		SLMQue=-1;
		TMPQue=-1;
		userQue=-1;
		mCancelables.clear();
	}
	public synchronized static boolean isRightTimeToUpload(){
		return DLCQue == -1 && ECGQue == -1 && SPO2Que == -1 && SLMQue == -1 && TMPQue == -1 && userQue == -1
				&& uploadDLCList == null && uploadECGList == null
				&& uploadSPO2List == null && uploadSLMList == null && uploadTMPList == null
				&& sUploadUser == null
				;
	}
	public synchronized static void cancelPost(){
		if (Constant.mCancelables!=null&&Constant.mCancelables.size()!=0){
			for (Callback.Cancelable cancelable :
					Constant.mCancelables) {
				if (!cancelable.isCancelled()){
					cancelable.cancel();
				}
			}
		}
		Constant.destroyListVal();
	}
}

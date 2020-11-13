package com.viatom.azur.measurement;

/**
 * Constant of measurement data
 * @author zouhao
 */
public class MeasurementConstant {
	
	// BP Calibration type
	public final static byte BP_TYPE_NONE = 1; // No Calibration
	public final static byte BP_TYPE_RE = 2; // Relative Calibration
	public final static byte BP_TYPE_ABS = 3; // Absolutely Calibration
	
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
	public final static byte CMD_TYPE_ABOUT_CKM = 12;
	public final static byte CMD_TYPE_VOICE_CONVERTED = 20;
	public final static byte CMD_TYPE_SPOT_USER_LIST = 21;
	public final static byte CMD_TYPE_SPOT = 22;
	public final static byte CMD_TYPE_BP = 30;

	// File parsing
	public final static int DAILYCHECK_ITEM_LENGTH = 17;
	public final static int ECGLIST_ITEM_LENGTH = 10;
	public final static int BPCAL_ITEM_LENGHT = 12;
	public final static int SPO2_ITEM_LENGHT = 12;
	public final static int BP_ITEM_LENGTH = 11;
	public final static int TEMP_ITEM_LENGHT = 11;
	public final static int USER_ITEM_LENGTH = 27;
	public final static int SLM_LIST_ITEM_LENGTH = 18;
	public final static int SLM_DETAIL_ITEM_LENGTH = 2;
	public final static int PED_ITEM_LENGTH = 29;
	public final static int CHECKME_ITEM_LENGTH = 27;
	public final static int SPOT_USER_ITEM_LENGTH = 72;
	public final static int SPOT_CHECK_ITEM_LENGTH = 24;
	
	// File name new
	public final static String FILE_NAME_USER_LIST = "usr.dat";
	public final static String FILE_NAME_BPCAL = "bpcal.dat";
	public final static String FILE_NAME_DLC_LIST = "dlc.dat";
	public final static String FILE_NAME_ECG_LIST = "ecg.dat";
	public final static String FILE_NAME_SPO2_LIST = "oxi.dat";
	public final static String FILE_NAME_TEMP_LIST = "tmp.dat";
	public final static String FILE_NAME_SLM_LIST = "slm.dat";
	public final static String FILE_NAME_PED_LIST = "ped.dat";
	public final static String FILE_NAME_SPOT_USER_LIST = "xusr.dat";
	public final static String FILE_NAME_SPOT_LIST = "spc.dat";
	public final static String FILE_NAME_BP_LIST = "nibp.dat";
	public final static String QT_FILE_NAME = "qt.dat";
	
	//File name old
	public final static String FILE_NAME_DLC_LIST_OLD = "dlc_old.dat";
	public final static String FILE_NAME_ECG_LIST_OLD = "ecg_old.dat";
	public final static String FILE_NAME_SPO2_LIST_OLD = "oxi_old.dat";
	public final static String FILE_NAME_TEMP_LIST_OLD = "tmp_old.dat";
	public final static String FILE_NAME_SLM_LIST_OLD = "slm_old.dat";
	public final static String FILE_NAME_PED_LIST_OLD = "ped_old.dat";
	public final static String FILE_NAME_SPOT_LIST_OLD = "spc_old.dat";
	public final static String FILE_NAME_BP_LIST_OLD = "nibp_old.dat";


}

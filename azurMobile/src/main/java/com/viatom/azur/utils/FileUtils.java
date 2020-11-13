package com.viatom.azur.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.viatom.azur.element.Constant;
import com.viatom.azur.measurement.BPCalItem;
import com.viatom.azur.measurement.BPItem;
import com.viatom.azur.measurement.CommonItem;
import com.viatom.azur.measurement.DailyCheckItem;
import com.viatom.azur.measurement.ECGInnerItem;
import com.viatom.azur.measurement.ECGItem;
import com.viatom.azur.measurement.MeasurementConstant;
import com.viatom.azur.measurement.PedItem;
import com.viatom.azur.measurement.SLMInnerItem;
import com.viatom.azur.measurement.SLMItem;
import com.viatom.azur.measurement.SPO2Item;
import com.viatom.azur.measurement.SpotCheckItem;
import com.viatom.azur.measurement.SpotUser;
import com.viatom.azur.measurement.SpotUser.SpotUserInfo;
import com.viatom.azur.measurement.TempItem;
import com.viatom.azur.measurement.User;
import com.viatom.azur.measurement.User.UserInfo;
import com.viatom.azur.tools.PreferenceUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Tools used to save measurement items to local file, or read file and decode
 * it to measurement items.
 * 
 * @author zouhao
 */
public class FileUtils {

	/**
	 * Save list to local file
	 * 
	 * @param dir
	 *            Folders address
	 * @param fileName
	 *            File name
	 * @param list
	 *            Any list that elements implements CommonItem interface
	 */
	public static void saveListToFile(File dir, String fileName, ArrayList<? extends CommonItem> list) {
		if (dir == null || fileName == null || list == null) {
			LogUtils.d("Save DailyCheck list failed");
			return;
		}
		FileDriver.write(dir, fileName, FileCoder.codeListFile(list));
	}

	/**
	 * Save list to file, but filter the no downloaded item
	 * 
	 * @param dir
	 *            Folders address
	 * @param fileName
	 *            File name
	 * @param list
	 *            Any list that elements implements CommonItem interface
	 */
	public static void saveListToFileWithoutUndownloaded(File dir, String fileName,
			ArrayList<? extends CommonItem> list) {
		if (dir == null || fileName == null || list == null) {
			LogUtils.d("Save DailyCheck list failed");
			return;
		}
		List<CommonItem> filteredList = CommonItemFilter.filterDownloadedList(list);
		FileDriver.write(dir, fileName, FileCoder.codeListFile(filteredList));
	}

	/**
	 * Read user list from local file
	 * 
	 * @param dir
	 *            Folders address
	 * @param fileName
	 *            File name
	 * @return User array
	 */
	public static User[] readUserList(File dir, String fileName) {

		if (dir == null || fileName == null) {
			LogUtils.d("Read user list file failed");
			return null;
		}

		byte[] buf = FileDriver.read(dir, fileName);

		if (buf == null || buf.length % MeasurementConstant.USER_ITEM_LENGTH != 0) {
			LogUtils.println("user buff length err!");
			return null;
		}
		int itemNum = buf.length / MeasurementConstant.USER_ITEM_LENGTH;
		User[] userList = new User[itemNum];
		for (int i = 0; i < itemNum; i++) {
			byte[] tempBuf = new byte[MeasurementConstant.USER_ITEM_LENGTH];
			System.arraycopy(buf, i * MeasurementConstant.USER_ITEM_LENGTH, tempBuf, 0,
					MeasurementConstant.USER_ITEM_LENGTH);
			userList[i] = new User(new UserInfo(tempBuf));
		}
		return userList;
	}

	/**
	 * Read spot user list from local file
	 * 
	 * @param dir
	 *            Folders address
	 * @param fileName
	 *            File name
	 * @return SpotUser array
	 */
	public static SpotUser[] readSpotUserList(File dir, String fileName) {

		if (dir == null || fileName == null) {
			LogUtils.d("Read spot user list file failed");
			return null;
		}

		byte[] buf = FileDriver.read(dir, fileName);

		if (buf == null || buf.length % MeasurementConstant.SPOT_USER_ITEM_LENGTH != 0) {
			LogUtils.println("user buff length err!");
			return null;
		}
		int itemNum = buf.length / MeasurementConstant.SPOT_USER_ITEM_LENGTH;
		SpotUser[] userList = new SpotUser[itemNum];
		for (int i = 0; i < itemNum; i++) {
			byte[] tempBuf = new byte[MeasurementConstant.SPOT_USER_ITEM_LENGTH];
			System.arraycopy(buf, i * MeasurementConstant.SPOT_USER_ITEM_LENGTH, tempBuf, 0,
					MeasurementConstant.SPOT_USER_ITEM_LENGTH);
			userList[i] = new SpotUser(new SpotUserInfo(tempBuf));
		}
		return userList;
	}

	/**
	 * Read BP calibration list from local file
	 * 
	 * @param dir
	 *            Folders address
	 * @param fileName
	 *            File name
	 * @return List contains BPCalItem
	 */
	public static List<BPCalItem> readBPCalFile(File dir, String fileName) {
		if (dir == null || fileName == null) {
			LogUtils.d("Read bpcal file failed");
			return null;
		}

		List<BPCalItem> list = new ArrayList<BPCalItem>();
		byte[] buf = FileDriver.read(dir, fileName);
		if (buf != null && buf.length != 0) {
			int itemLength = MeasurementConstant.BPCAL_ITEM_LENGHT;
			for (int i = 0; i < buf.length / itemLength; i++) {
				byte[] itemBuf = new byte[itemLength];
				System.arraycopy(buf, i * itemLength, itemBuf, 0, itemLength);
				list.add(new BPCalItem(itemBuf));
			}
		}
		return list;
	}

	/**
	 * Read DailyCheck list from local file
	 * 
	 * @param dir
	 *            Folders address
	 * @param fileName
	 *            File name
	 * @return List contains DailyCheckItem
	 */
	public static List<DailyCheckItem> readDailyCheckList(File dir, String fileName) {
		if (dir == null || fileName == null) {
			LogUtils.d("Read list file failed");
			return null;
		}
		byte[] buf = FileDriver.read(dir, fileName);
		if (buf == null) {
			LogUtils.d("Read list file failed, buf is null");
			return null;
		}
		if (buf.length % MeasurementConstant.DAILYCHECK_ITEM_LENGTH != 0) {
			LogUtils.d("Read list file failed, buf length error");
			return null;
		}
		List<DailyCheckItem> list = new ArrayList<DailyCheckItem>();
		int itemLength = MeasurementConstant.DAILYCHECK_ITEM_LENGTH;
		for (int i = 0; i < buf.length / itemLength; i++) {
			byte[] itemBuf = new byte[itemLength];
			System.arraycopy(buf, i * itemLength, itemBuf, 0, itemLength);
			// Create an item and add to list
			DailyCheckItem item = new DailyCheckItem(itemBuf);
			boolean downloaded = FileDriver.isFileExist(dir,
					new SimpleDateFormat("yyyyMMddHHmmss").format(item.getDate()));
			item.setDownloaded(downloaded);
			list.add(item);

		}
		return list;
	}

	/**
	 * Read ECG list from local file
	 * 
	 * @param dir
	 *            Folders address
	 * @param fileName
	 *            File name
	 * @return List contains ECGItem
	 */
	public static List<ECGItem> readECGList(File dir, String fileName) {
		if (dir == null || fileName == null) {
			LogUtils.d("Read list file failed");
			return null;
		}
		byte[] buf = FileDriver.read(dir, fileName);
		if (buf == null) {
			LogUtils.d("Read list file failed, buf is null");
			return null;
		}
		if (buf.length % MeasurementConstant.ECGLIST_ITEM_LENGTH != 0) {
			LogUtils.d("Read list file failed, buf length error");
			return null;
		}
		List<ECGItem> list = new ArrayList<ECGItem>();
		int itemLength = MeasurementConstant.ECGLIST_ITEM_LENGTH;
		for (int i = 0; i < buf.length / itemLength; i++) {
			byte[] itemBuf = new byte[itemLength];
			System.arraycopy(buf, i * itemLength, itemBuf, 0, itemLength);
			// Create an item and add to list
			ECGItem item = new ECGItem(itemBuf);
			boolean downloaded = FileDriver.isFileExist(dir,
					new SimpleDateFormat("yyyyMMddHHmmss").format(item.getDate()));
			item.setDownloaded(downloaded);
			list.add(item);
		}
		return list;
	}

	/**
	 * Read Spo2 list from local file
	 * 
	 * @param dir
	 *            Folders address
	 * @param fileName
	 *            File name
	 * @return List contains SPO2Item
	 */
	public static List<SPO2Item> readSPO2List(File dir, String fileName) {
		if (dir == null || fileName == null) {
			LogUtils.d("Read list file failed");
			return null;
		}
		byte[] buf = FileDriver.read(dir, fileName);
		if (buf == null) {
			LogUtils.d("Read list file failed, buf is null");
			return null;
		}
		if (buf.length % MeasurementConstant.SPO2_ITEM_LENGHT != 0) {
			LogUtils.d("Read list file failed, buf length error");
			return null;
		}
		List<SPO2Item> list = new ArrayList<SPO2Item>();
		int itemLength = MeasurementConstant.SPO2_ITEM_LENGHT;
		for (int i = 0; i < buf.length / itemLength; i++) {
			byte[] itemBuf = new byte[itemLength];
			System.arraycopy(buf, i * itemLength, itemBuf, 0, itemLength);
			list.add(new SPO2Item(itemBuf));
		}
		return list;
	}

	public static List<BPItem> readBPList(File dir, String fileName) {
		if (dir == null || fileName == null) {
			return null;
		}

		byte[] buf = FileDriver.read(dir, fileName);
		if (buf == null) {
			return null;
		}

		if (buf.length % MeasurementConstant.BP_ITEM_LENGTH != 0) {
			return null;
		}

		List<BPItem> list = new ArrayList<BPItem>();
		int itemLength = MeasurementConstant.BP_ITEM_LENGTH;
		for (int i = 0; i < buf.length/itemLength; i++) {
			byte[] itemBuf = new byte[itemLength];
			System.arraycopy(buf, i*itemLength, itemBuf, 0, itemLength);
			list.add(new BPItem(itemBuf));
		}
		return list;
	}


	/**
	 * Read temperature list from local file
	 * 
	 * @param dir
	 *            Folders address
	 * @param fileName
	 *            File name
	 * @return List contains TempItem
	 */
	public static List<TempItem> readTempList(File dir, String fileName) {
		if (dir == null || fileName == null) {
			LogUtils.d("Read list file failed");
			return null;
		}
		byte[] buf = FileDriver.read(dir, fileName);
		if (buf == null) {
			LogUtils.d("Read list file failed, buf is null");
			return null;
		}
		if (buf.length % MeasurementConstant.TEMP_ITEM_LENGHT != 0) {
			LogUtils.d("Read list file failed, buf length error");
			return null;
		}
		List<TempItem> list = new ArrayList<TempItem>();
		int itemLength = MeasurementConstant.TEMP_ITEM_LENGHT;
		for (int i = 0; i < buf.length / itemLength; i++) {
			byte[] itemBuf = new byte[itemLength];
			System.arraycopy(buf, i * itemLength, itemBuf, 0, itemLength);
			list.add(new TempItem(itemBuf));
		}
		return list;
	}

	/**
	 * Read sleep monitor list from local file
	 * 
	 * @param dir
	 *            Folders address
	 * @param fileName
	 *            File name
	 * @return List contains SLMItem
	 */
	public static List<SLMItem> readSLMList(File dir, String fileName) {
		if (dir == null || fileName == null) {
			LogUtils.d("Read list file failed");
			return null;
		}
		byte[] buf = FileDriver.read(dir, fileName);
		if (buf == null) {
			LogUtils.d("Read list file failed, buf is null");
			return null;
		}
		if (buf.length % MeasurementConstant.SLM_LIST_ITEM_LENGTH != 0) {
			LogUtils.d("Read list file failed, buf length error");
			return null;
		}
		List<SLMItem> list = new ArrayList<SLMItem>();
		int itemLength = MeasurementConstant.SLM_LIST_ITEM_LENGTH;
		for (int i = 0; i < buf.length / itemLength; i++) {
			byte[] itemBuf = new byte[itemLength];
			System.arraycopy(buf, i * itemLength, itemBuf, 0, itemLength);
			// Create an item and add to list
			SLMItem item = new SLMItem(itemBuf);
			boolean downloaded = FileDriver.isFileExist(dir,
					new SimpleDateFormat("yyyyMMddHHmmss").format(item.getDate()));
			item.setDownloaded(downloaded);
			list.add(item);
		}
		return list;
	}

	/**
	 * Read Ped list from local file
	 * 
	 * @param dir
	 *            Folders address
	 * @param fileName
	 *            File name
	 * @return List contains PedItem
	 */
	public static List<PedItem> readPedList(File dir, String fileName) {
		if (dir == null || fileName == null) {
			LogUtils.d("Read list file failed");
			return null;
		}
		byte[] buf = FileDriver.read(dir, fileName);
		if (buf == null) {
			LogUtils.d("Read list file failed, buf is null");
			return null;
		}
		if (buf.length % MeasurementConstant.PED_ITEM_LENGTH != 0) {
			LogUtils.d("Read list file failed, buf length error");
			return null;
		}
		List<PedItem> list = new ArrayList<PedItem>();
		int itemLength = MeasurementConstant.PED_ITEM_LENGTH;
		for (int i = 0; i < buf.length / itemLength; i++) {
			byte[] itemBuf = new byte[itemLength];
			System.arraycopy(buf, i * itemLength, itemBuf, 0, itemLength);
			list.add(new PedItem(itemBuf));
		}
		return list;
	}

	/**
	 * Read ECG inner item from local file
	 * 
	 * @param dir
	 *            Folders address
	 * @param fileName
	 *            File name
	 * @return List contains ECGInnerItem
	 */
	public static ECGInnerItem readECGInnerItem(File dir, String fileName) {
		if (dir == null || fileName == null) {
			LogUtils.d("Read inner file failed");
			return null;
		}
		byte[] buf = FileDriver.read(dir, fileName);
		if (buf == null || buf.length == 0) {
			LogUtils.d("Read inner file failed, buf is null");
			return null;
		}
		ECGInnerItem innerItem = new ECGInnerItem(buf);
		return innerItem;
	}

	/**
	 * Read ECG inner item from local file
	 * 
	 * @param dir
	 *            Folders address
	 * @param fileName
	 *            File name
	 * @return List contains ECGInnerItem
	 */
	public static ECGInnerItem readECGInnerItem(File dir, String fileName, Context context) {
		if (dir == null || fileName == null) {
			LogUtils.d("Read inner file failed");
			return null;
		}
		byte[] buf = FileDriver.read(dir, fileName);
		if (buf == null || buf.length == 0) {
			LogUtils.d("Read inner file failed, buf is null");
			return null;
		}
		ECGInnerItem innerItem = new ECGInnerItem(buf, context);
		return innerItem;
	}

	/**
	 * Read SLM inner item from local file
	 * 
	 * @param dir
	 *            Folders address
	 * @param fileName
	 *            File name
	 * @return List contains SLMInnerItem
	 */
	public static SLMInnerItem readSLMInnerItem(File dir, String fileName) {
		if (dir == null || fileName == null) {
			LogUtils.d("Read inner file failed");
			return null;
		}
		byte[] buf = FileDriver.read(dir, fileName);
		if (buf == null || buf.length == 0) {
			LogUtils.d("Read inner file failed, buf is null");
			return null;
		}
		SLMInnerItem innerItem = new SLMInnerItem(buf);
		return innerItem;
	}

	/**
	 * Read SpotCheck list from local file
	 * 
	 * @param dir
	 *            Folders address
	 * @param fileName
	 *            File name
	 * @return List contains SpotCheckItem
	 */
	public static List<SpotCheckItem> readSpotCheckList(File dir, String fileName) {
		if (dir == null || fileName == null) {
			LogUtils.d("Read list file failed");
			return null;
		}
		byte[] buf = FileDriver.read(dir, fileName);
		if (buf == null) {
			LogUtils.d("Read list file failed, buf is null");
			return null;
		}
		if (buf.length % MeasurementConstant.SPOT_CHECK_ITEM_LENGTH != 0) {
			LogUtils.d("Read list file failed, buf length error");
			return null;
		}
		List<SpotCheckItem> list = new ArrayList<SpotCheckItem>();
		int itemLength = MeasurementConstant.SPOT_CHECK_ITEM_LENGTH;
		for (int i = 0; i < buf.length / itemLength; i++) {
			byte[] itemBuf = new byte[itemLength];
			System.arraycopy(buf, i * itemLength, itemBuf, 0, itemLength);
			// Create an item and add to list
			SpotCheckItem item = new SpotCheckItem(itemBuf);
			if ((item.getFunc() & SpotCheckItem.MODE_ECG) == 0) {
				// No ECG data, should not to download
				item.setDownloaded(true);
			} else {
				boolean downloaded = FileDriver.isFileExist(dir,
						new SimpleDateFormat("yyyyMMddHHmmss").format(item.getDate()));
				item.setDownloaded(downloaded);
			}
			list.add(item);
		}
		return list;
	}


	/**
	 * Gets the content:// URI from the given corresponding path to a file
	 *
	 * @param context
	 * @param imageFile
	 * @return content Uri
	 */
	public static Uri getImageContentUri(Context context, File imageFile) {
		String filePath = imageFile.getAbsolutePath();
		Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Images.Media._ID }, MediaStore.Images.Media.DATA + "=? ",
				new String[] { filePath }, null);
		if (cursor != null && cursor.moveToFirst()) {
			int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
			Uri baseUri = Uri.parse("content://media/external/images/media");
			return Uri.withAppendedPath(baseUri, "" + id);
		} else {
			if (imageFile.exists()) {
				ContentValues values = new ContentValues();
				values.put(MediaStore.Images.Media.DATA, filePath);
				return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			} else {
				return null;
			}
		}
	}

	/**
	 * Gets the content:// URI from the given corresponding path to a file
	 *
	 * @param context
	 * @param videoFile
	 * @return content Uri
	 */
	public static Uri getVideoContentUri(Context context, File videoFile) {
		String filePath = videoFile.getAbsolutePath();
		Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Video.Media._ID }, MediaStore.Video.Media.DATA + "=? ",
				new String[] { filePath }, null);
		if (cursor != null && cursor.moveToFirst()) {
			int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
			Uri baseUri = Uri.parse("content://media/external/video/media");
			return Uri.withAppendedPath(baseUri, "" + id);
		} else {
			if (videoFile.exists()) {
				ContentValues values = new ContentValues();
				values.put(MediaStore.Video.Media.DATA, filePath);
				return context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
			} else {
				return null;
			}
		}
	}

	/**
	 * Gets the content:// URI from the given corresponding path to a file
	 *
	 * @param context
	 * @param audioFile
	 * @return content Uri
	 */
	public static Uri getAudioContentUri(Context context, File audioFile) {
		String filePath = audioFile.getAbsolutePath();
		Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media._ID }, MediaStore.Audio.Media.DATA + "=? ",
				new String[] { filePath }, null);
		if (cursor != null && cursor.moveToFirst()) {
			int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
			Uri baseUri = Uri.parse("content://media/external/audio/media");
			return Uri.withAppendedPath(baseUri, "" + id);
		} else {
			if (audioFile.exists()) {
				ContentValues values = new ContentValues();
				values.put(MediaStore.Audio.Media.DATA, filePath);
				return context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
			} else {
				return null;
			}
		}
	}
}

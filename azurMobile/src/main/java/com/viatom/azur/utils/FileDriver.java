package com.viatom.azur.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Drive for file basic operations
 * Including write, read, delete...
 * @author zouhao
 */
public class FileDriver {
	
	/**
	 * Write data to local file
	 * @param dir Folders address
	 * @param fileName File name
	 * @param data Data buffer 
	 */
	public static void write(File dir, String fileName, byte[] data) {
		if (dir==null || fileName==null || data==null) {
			LogUtils.d("Write file failed");
			return;
		}
		if (!dir.exists()) {
			dir.mkdir();
		}
		if (!dir.isDirectory()) {
			LogUtils.d(dir + "is not a dir, write file failed");
			return;
		}
		
		File file = new File(dir, fileName);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(data);
			fos.close();
		} catch (Exception e) {
			LogUtils.d( "Write file exception" + e);
		}
	}
	
	/**
	 * Read local file, return bytes
	 * @param dir Folders address
	 * @param fileName File name
	 * @return Data buffer read from the file
	 */
	public static byte[] read(File dir, String fileName) {
		if (dir==null || fileName==null) {
			LogUtils.d("Read file failed");
			return null;
		}
		if (!dir.isDirectory()) {
			LogUtils.d(dir + "is not a dir, read file failed");
			return null;
		}
		
		File file = new File(dir, fileName);
		
		LogUtils.d("read file:" + file);
		
		if (file.exists() == false) {
			LogUtils.d(fileName + "not exists");
			return null;
		}
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] TempBuffer = new byte[(int) file.length()];
			fis.read(TempBuffer);
			fis.close();
			return TempBuffer;
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Check whether the file exists 
	 * @param dir Folder path
	 * @param fileName File name
	 * @return True: file exist, False: file not exist
	 */
	public static boolean isFileExist(File dir, String fileName){
		if (dir == null || fileName == null || !dir.isDirectory()) {
			return false;
		}
		if((new File(dir, fileName)).exists())
			return true;
		else
			return false;
	}

	/**
	 * Delete all file in a folder
	 * @param dir Folders address
	 */
	public static void deleteAllInfo(File dir) {
		if (dir==null || !dir.exists() || !dir.isDirectory()){
			return;
		}
		LogUtils.d("Delete all in folder" + dir);
		File[] files = dir.listFiles();
		if(files==null)
			return;
		for (int i = 0; i < files.length; i++)
			files[i].delete();
	}

	/**
	 * Delete a file
	 * @param dir Folder path
	 * @param fileName File name
	 */
	public static void delFile(File dir, String fileName) {
		if (dir == null || fileName == null || !dir.isDirectory()) {
			LogUtils.d("Del file failed");
			return;
		}
		File file = new File(dir,fileName);
		if(file.exists()){
			LogUtils.d("Delete file:"+fileName);
			file.delete();
		}
	}
	
}

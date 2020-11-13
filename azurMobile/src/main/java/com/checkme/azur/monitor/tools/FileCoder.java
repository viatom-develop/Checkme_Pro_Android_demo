package com.checkme.azur.monitor.tools;

import com.checkme.azur.monitor.utils.LogUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class FileCoder {

	/**
	 * Write data to local file
	 * @param dir Folders address
	 * @param fileName File name
	 * @param data Data buffer
	 */

	static FileWriter fw = null ;
	static BufferedWriter bw = null;
	public static String finame = "first" ;
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

		if(!finame.equals(fileName)){
			if(bw!=null){
				try {
					bw.close();
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			finame = fileName ;
			File file = new File(dir, fileName);
			try {
				fw = new FileWriter(file, true);
				bw = new BufferedWriter(fw) ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}

		String str = null;
		//	byte[] b ;
		try {
			str = new String(data,"iso-8859-1");
			//		str=str.replaceAll("[\r\n]","");

			/*b = str.getBytes("iso-8859-1");

			for(int i = 0 ; i<b.length ;i++){
				System.out.print(b[i]+",");
			}
			System.out.print("一条数据");*/
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		try {
			bw.write(str);
			bw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*try {

	//		FileOutputStream fos = new FileOutputStream(file);

			if(file == null){
				file = new File(dir, fileName);
			}

			if(fw == null){
				fw = new FileWriter(file, true);
			}
			if(bw == null){
				bw = new BufferedWriter(fw) ;
			}

			String str = new String(data,"iso-8859-1");
	//		str = String.copyValueOf(str.toCharArray(), 0, data.length);

			bw.write(str);
			bw.flush();

		//	fos.write(data);
		//	fos.close();
		} catch (Exception e) {
			LogUtils.d( "Write file exception" + e);
		} */
	}

	/**
	 * Read local file, return bytes
	 * @param dir Folders address
	 * @param fileName File name
	 * @return Data buffer read from the file
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] read(File dir, String fileName) throws UnsupportedEncodingException {
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
		StringBuffer sb = new StringBuffer();

		try {
			/*FileInputStream fis = new FileInputStream(file);
			byte[] TempBuffer = new byte[(int) file.length()];
			fis.read(TempBuffer);
			fis.close();
			*/

			BufferedReader reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;


			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				// 显示行号
				//  System.out.println("line " + line + ": " + tempString.getBytes());

            	/*byte[] b = tempString.getBytes("iso-8859-1") ;
                for(int i = 0 ; i < b.length ; i ++){
                	System.out.print(b[i]+",");
                	if((i+1)%44 == 0){
                		System.out.println("读完一条数据***"+i);
                	}
                }*/

				//        System.out.println("读完一行数据***"+line);
				sb.append(tempString+"\n");

				line++;

			}
			reader.close();

			return  sb.toString().getBytes("iso-8859-1");

		} catch (Exception e) {

		}
		return  sb.toString().getBytes("iso-8859-1");
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


	/**
	 * Delete all file in a folder
	 *
	 * @param dir
	 *            Folders address
	 */
	public static void deleteAllInfo(File dir) {
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			return;
		}
		LogUtils.d("Delete all in folder" + dir);
		File[] files = dir.listFiles();
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {

			if (!files[i].toString().contains("usr")) {
				files[i].delete();
			}
		}

	}

	/**
	 * Delete a dir,and the dir is null
	 *
	 * @param dir
	 *            Folder path
	 */
	public static boolean delFileDir(File dir) {
		if (dir == null || !dir.isDirectory()) {
			LogUtils.d("Del file failed");
			return false;
		}
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();

			for (int i = 0; i < files.length; i++) {

				files[i].delete();
			}
		}

		// 目录此时为空，可以删除
		return dir.delete();

	}


}

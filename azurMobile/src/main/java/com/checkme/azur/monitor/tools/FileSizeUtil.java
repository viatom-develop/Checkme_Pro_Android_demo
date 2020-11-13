package com.checkme.azur.monitor.tools;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * �жϴ洢�ռ�Ĺ�����
 * @author tanshiyao
 *  
 */
public class FileSizeUtil {

    private static final int ERROR = -1;

    /**
     * SDCARD�Ƿ��
     */
    public static boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }
    
    /**
     * ��ȡ�ֻ��ڲ�ʣ��洢�ռ�
     * ��λG
     * @return
     */
    
    public static long getmem_UNUSED(Context mContext) {
        long MEM_UNUSED;
	// �õ�ActivityManager
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
	// ����ActivityManager.MemoryInfo����  

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

	// ȡ��ʣ����ڴ�ռ� 

        MEM_UNUSED = mi.availMem / 1024 / 1024;
        return MEM_UNUSED;
    }

    /**
     * ��ȡ�ֻ��ڲ��ܵĴ洢�ռ�
     * 
     * @return
     */
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong(); // ���һ�������Ĵ�С  
        long totalBlocks = stat.getBlockCountLong();// �������������  
        return totalBlocks * blockSize;
    }

    /**
     * ��ȡSDCARDʣ��洢�ռ�
     * 
     * @return
     */
    public static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();// ��ÿ��õ��������� 
            return availableBlocks * blockSize;
        } else {
            return ERROR;
        }
    }

    /**
     * ��ȡSDCARD�ܵĴ洢�ռ�
     * 
     * @return
     */
    public static long getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            return totalBlocks * blockSize;
        } else {
            return ERROR;
        }
    }

    /**
     * ��ȡϵͳ���ڴ�
     * 
     * @param context �ɴ���Ӧ�ó��������ġ�
     * @return ���ڴ��λΪB��
     */
    public static long getTotalMemorySize(Context context) {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            return Integer.parseInt(subMemoryLine.replaceAll("\\D+", "")) * 1024l;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * ��ȡ��ǰ�����ڴ棬�����������ֽ�Ϊ��λ��
     * 
     * @param context �ɴ���Ӧ�ó��������ġ�
     * @return ��ǰ�����ڴ浥λΪB��
     */
    public static long getAvailableMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    private static DecimalFormat fileIntegerFormat = new DecimalFormat("#0");
    private static DecimalFormat fileDecimalFormat = new DecimalFormat("#0.#");

    /**
     * ��λ����
     * 
     * @param size ��λΪB
     * @param isInteger �Ƿ񷵻�ȡ���ĵ�λ
     * @return ת����ĵ�λ
     */
    public static String formatFileSize(long size, boolean isInteger) {
        DecimalFormat df = isInteger ? fileIntegerFormat : fileDecimalFormat;
        String fileSizeString = "0M";
        if (size < 1024 && size > 0) {
            fileSizeString = df.format((double) size) + "B";
        } else if (size < 1024 * 1024) {
            fileSizeString = df.format((double) size / 1024) + "K";
        } else if (size < 1024 * 1024 * 1024) {
            fileSizeString = df.format((double) size / (1024 * 1024)) + "M";
        } else {
            fileSizeString = df.format((double) size / (1024 * 1024 * 1024)) + "G";
        }
        return fileSizeString;
    }
}

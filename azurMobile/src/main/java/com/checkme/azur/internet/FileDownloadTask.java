package com.checkme.azur.internet;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.Handler;

import com.checkme.azur.element.Constant;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.utils.MsgUtils;

public class FileDownloadTask extends Thread {
	private int blockSize, downloadSizeMore;
	private int threadNum = 5;
	String urlStr, threadNo, fileName;
	private int fileSize, downloadedSize;
	Handler handler;
	//下载文件种类，区分多个文件同时下载共用一个进度条的时候
	private int fileType;

	public FileDownloadTask(String urlStr, int threadNum, String fileName,int fileType,
							Handler handler) {
		this.urlStr = urlStr;
		this.threadNum = threadNum < 2 ? 2: threadNum;
		this.fileName = fileName;
		this.fileType = fileType;
		this.handler = handler;
	}

	@Override
	public void run() {
		FileDownloadThread[] fds = new FileDownloadThread[threadNum];
		try {
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
			// 防止返回-1
			InputStream in = conn.getInputStream();
			fileSize = conn.getContentLength();
			blockSize = fileSize / (threadNum - 1);
			downloadSizeMore = (fileSize % (threadNum - 1));// 留一个线程下最后的余数
			File file = new File(fileName);
			for (int i = 0; i < threadNum; i++) {
				// 启动线程，分别下载自己需要下载的部分
				if (i == threadNum - 1) {// 最后一个线程下载余数部分
					FileDownloadThread fdt;
					if (downloadSizeMore==0) {//没有余数则最后一线程不下载
						fdt = new FileDownloadThread(url, file,0, 0);
					}else {
						fdt = new FileDownloadThread(url, file,i * blockSize, fileSize - 1);
					}
					fdt.setName("Thread" + i);
					fdt.start();
					fds[i] = fdt;
				}else {
					FileDownloadThread fdt = new FileDownloadThread(url, file,
							i * blockSize, (i + 1) * blockSize - 1);
					fdt.setName("Thread" + i);
					fdt.start();
					fds[i] = fdt;
				}
			}
			boolean finished = false;
			while (!finished) {
				downloadedSize = downloadSizeMore;
				finished = true;
				for (int i = 0; i < fds.length; i++) {
					downloadedSize += fds[i].getDownloadSize();
					if (!fds[i].isFinished()) {
						finished = false;
					}
				}
				int progress = (Double.valueOf((downloadedSize * 1.0 / fileSize * 100))).intValue();
				MsgUtils.sendMsg(handler, Constant.MSG_DOWNLOAD_PART_FINISH, fileType,progress);
				LogUtils.d("发送下载进度到handler,完成： " + progress);
				sleep(500);
			}
			LogUtils.d("下载进程结束");
		} catch (Exception e) {
			e.printStackTrace();
			MsgUtils.sendMsg(handler, Constant.MSG_DOWNLOAD_FAILED);
		}

	}
}

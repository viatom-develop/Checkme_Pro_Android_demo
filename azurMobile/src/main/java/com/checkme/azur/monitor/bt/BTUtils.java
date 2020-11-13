package com.checkme.azur.monitor.bt;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.checkme.azur.monitor.utils.LogUtils;
import com.checkme.azur.monitor.utils.MedioPlayUtils;
import com.checkme.azur.monitor.utils.NumUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BTUtils extends Service{

	// SPP-UUID Number
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";

	private BTBinder binder = new BTBinder();
	private InputStream is;
	private OutputStream os;
	private BluetoothSocket socket = null;
	private ReadThread readThread;

	@Override
	public IBinder onBind(Intent intent) {
		LogUtils.d("BT service is Binded");
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		LogUtils.d("BT service is Unbinded");
		if (socket != null) {
			try {
				socket.close();
				socket = null;
			} catch (IOException e) {
				LogUtils.d("socket close failed");
				e.printStackTrace();
			}
		}
		return true;
	}

	public BTBinder getBinder() {
		return binder;
	}

	public class BTBinder extends Binder {

		/**
		 * Connect Bluetooth
		 * @param address MAC address
		 * @param listener
		 */
		public void interfaceConnect(String address, BTConnectListener listener) {
			new ConnectThread(address, listener).start();
		}


		public void startMedia(String date){
			MedioPlayUtils.playVoice(date);
		}

	/*	public Bitmap getScreen(Activity mActivity, int width ,int height){
			View view = mActivity.getWindow().getDecorView();
			view.setDrawingCacheEnabled(true);
			view.buildDrawingCache();
			Bitmap b1 = view.getDrawingCache();

			// 获取状态栏高度
			Rect frame = new Rect();
			mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
			int statusBarHeight = frame.top;

			Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height
					- statusBarHeight);
			view.destroyDrawingCache();

			return b ;
		}*/

		/**
		 * Start to read data
		 * @param listener
		 */
		public void interfaceStartRead(ReadThreadListener listener) {
			readThread = new ReadThread(listener);
			readThread.start();
		}

		/**
		 * Start to read data
		 */
		public void interfaceStopRead() {
			if (readThread != null) {
				readThread.bRun = false;
				readThread.interrupt();
				readThread = null;
			}
		}

	}

	/**
	 *	Connect to Checkme
	 * @author zouhao
	 */
	private class ConnectThread extends Thread {
		private String address;
		private BTConnectListener listener;

		public ConnectThread(String address, BTConnectListener listener) {
			this.address = address;
			this.listener = listener;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// Get the local bluetooth adapter
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			BluetoothDevice device = adapter.getRemoteDevice(address);
			Method m;
			try {
				m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
				socket = (BluetoothSocket) m.invoke(device, Integer.valueOf(1));

				//		socket = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
				socket.connect();
				is = socket.getInputStream();
				os = socket.getOutputStream();
				listener.onTraceConnectSuccess();
			} catch (IOException e) {
				// TODO: handle exception
				LogUtils.d("Bluetooth connection exception");
				e.printStackTrace();
				try {
					socket.close();
					socket = null;
				} catch (IOException e2) {
					// TODO: handle exception
					LogUtils.d("socket close exception");
					e.printStackTrace();
				}
				listener.onTraceConnectFailed(BTConnectListener.ERR_CODE_NORMAL);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};



	/**
	 *	Bluetooth socket Readthread
	 *	Created when you need to read data, and terminated after reading
	 * @author zouhao
	 */
	public class ReadThread extends Thread {
		private final int PKG_FRONT_LENGTH = 3;
		private byte[] pkgBuf;
		private ReadThreadListener listener;
		private boolean bRun = true;

		public ReadThread(ReadThreadListener listener) {
			this.listener = listener;
		}

		/**
		 *  Read data from Bluetooth socket
		 */
		@Override
		public void run() {
			int readCount = 0, preNum = 0;
			try {
				LogUtils.d("Readthread started");
				while (bRun) {
					//Read package front and data size at first
					byte[] frontBuf = new byte[PKG_FRONT_LENGTH];

					while (preNum < PKG_FRONT_LENGTH) {
//						LogUtils.d("Reading pkg front");
						//Read first byte
						while (readCount == 0){
							readCount = is.available();
							//	LogUtils.d("问题在这里********1");
						}

						is.read(frontBuf, preNum++, 1);
						if (frontBuf[0] != (byte)0xA5) {
							LogUtils.e("Package front first byte err");
							preNum = 0;
							readCount = 0;
							continue;
						}
						readCount = 0;

						//The first byte is right, continue to read
						while (readCount == 0){
							readCount = is.available();
						}

						is.read(frontBuf, preNum++, 1);
						if (frontBuf[1] != (byte)0x5A) {
							LogUtils.e("Package front second byte err");
							preNum = 0;
							readCount = 0;
							continue;
						}
						readCount = 0;

						//The second byte is right, continue to read
						while (readCount == 0){
							readCount = is.available();
						}

						is.read(frontBuf, preNum++, 1);
						if (frontBuf[2] < 0) {
							LogUtils.e("Package front third byte err");
							preNum = 0;
							readCount = 0;
							continue;
						}
						readCount = 0;
					}
					preNum = 0;

					//Read data chunk
					int dataLength = NumUtils.bToi(frontBuf[2]) - PKG_FRONT_LENGTH;
//					LogUtils.d("Package data length:" + dataLength);
					byte[] dataBuf = new byte[dataLength];

					while (preNum < dataLength) {
						while (readCount == 0){
							readCount = is.available();
						}

						if ((preNum + readCount) > dataBuf.length) {
							is.read(dataBuf, preNum, dataBuf.length - preNum);
//							LogUtils.d("Received: " + (dataBuf.length - preNum)+ ", PreNum" + preNum);
							preNum += dataBuf.length - preNum;
						} else {
							is.read(dataBuf, preNum, readCount);
							preNum += readCount;
//							LogUtils.d("Received: " + (readCount) + ", PreNum"+ preNum);
						}
						readCount = 0;
					}
					preNum = 0;

//					LogUtils.d("A package received");

					pkgBuf = new byte[frontBuf.length + dataBuf.length];
					System.arraycopy(frontBuf, 0, pkgBuf, 0, frontBuf.length);
					System.arraycopy(dataBuf, 0, pkgBuf, frontBuf.length, dataBuf.length);
					listener.onReadThreadFinished(pkgBuf);
				}
				LogUtils.d("Readthread ended");
			} catch (IOException e) {
				// TODO: handle exception
				LogUtils.d("Readthread IOException");
				listener.onReadThreadFailed(ReadThreadListener.ERR_CODE_EXP);
			}
		}
	}

	public static String bytesToHexString(byte[] src){
		StringBuilder stringBuilder = new StringBuilder();
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

}

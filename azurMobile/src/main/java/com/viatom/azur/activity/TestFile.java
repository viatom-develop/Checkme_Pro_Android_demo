package com.viatom.azur.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.viatom.azur.bluetooth.BTBinder;
import com.viatom.azur.bluetooth.BTConnectListener;
import com.viatom.azur.bluetooth.BTUtils;
import com.viatom.azur.bluetooth.GetInfoThreadListener;
import com.viatom.azur.bluetooth.PingThreadListener;
import com.viatom.azur.bluetooth.ReadFileListener;
import com.viatom.azur.element.CheckmeDevice;
import com.viatom.azur.measurement.ECGInnerItem;
import com.viatom.azur.measurement.ECGItem;
import com.viatom.azur.utils.FileUtils;
import com.viatom.azur.utils.LogUtils;

public class TestFile extends Activity implements ReadFileListener
	, PingThreadListener, GetInfoThreadListener, BTConnectListener{
	
	BTBinder mBinder;
	
	/**
	 *  Demo for read file from Checkme
	 */
	public void testRead() {
		//If mBinder is null, you should bind BTUtils at first
		if (mBinder != null) {
			mBinder.interfaceReadFile("ecg.dat", (byte)0, 5000, this);
		}
	}

	@Override
	public void onReadPartFinished(String fileName, byte fileType,
			float percentage) {
		// TODO Auto-generated method stub
		LogUtils.d(fileName + " read " + percentage*100 + "%");
	}

	@Override
	public void onReadSuccess(String fileName, byte fileType, byte[] fileBuf) {
		// TODO Auto-generated method stub
		LogUtils.d(fileName + " read succeeded.");
		//Write buffer to local file...
	}

	@Override
	public void onReadFailed(String fileName, byte fileType, byte errCode) {
		// TODO Auto-generated method stub
		LogUtils.d(fileName + " read failed. ErrCode:" + errCode);
	}
	
	
	/**
	 * Demo for read and decode data from byte array
	 * @param buf Data buffer
	 * @return ECGItem
	 */
	public ECGItem testDecodeECGItem(byte[] buf) {
		if (buf != null) {
			ECGItem ecgItem = new ECGItem(buf);
			return ecgItem;
		}
		return null;
	}
	
	/**
	 * Demo for read and read data from local file
	 */
	public void testReadFile() {
		//Read ECG list
		List<ECGItem> ecgItems = FileUtils.readECGList(
			new File("/sdcard0/CheckmeMobile"), "ecg.dat");
		if (ecgItems != null && ecgItems.size() != 0) {
			ECGItem ecgItem = ecgItems.get(0);
			//Read ECG Inner data
			String fileName = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(ecgItem.getDate());
			ECGInnerItem innerItem = FileUtils.readECGInnerItem(
					new File("/sdcard0/CheckmeMobile"), fileName,getApplicationContext());
			ecgItem.setInnerItem(innerItem);
		}
	}
	
	/**
	 *  Demo for Ping command
	 */
	public void testPing() {
		if(mBinder != null){
			mBinder.interfacePing(1000, this);
		}
	}

	@Override
	public void onPingSuccess() {
		// TODO Auto-generated method stub
		LogUtils.d("Ping succeeded");
	}

	@Override
	public void onPingFailed(byte errCode) {
		// TODO Auto-generated method stub
		LogUtils.d("Ping failed. Error code: " + errCode);
	}
	
	
	/**
	 * Demo for get Checkme information 
	 */
	public void testGetCheckmeInfo() {
		if (mBinder != null) {
			mBinder.interfaceGetInfo(1000, this);
		}
	}

	@Override
	public void onGetInfoSuccess(String checkmeInfo) {
		// TODO Auto-generated method stub
		LogUtils.d("Get Checkme info succeeded");
		try {
			JSONObject jsonObject = new JSONObject(checkmeInfo);
			CheckmeDevice checkmeDevice = new CheckmeDevice(jsonObject);
			//enter your code..
			
		} catch (JSONException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@Override
	public void onGetInfoFailed(byte errCode) {
		// TODO Auto-generated method stub
		LogUtils.d("Get Checkme info failed. Error code: " + errCode);
	}
	
	
	/**
	 * Demo for connect Checkme
	 * @param macAddress Enter Checkme bluetooth mac address
	 */
	public void testConnectCheckme(String macAddress) {
		if (mBinder != null) {
//			mBinder.interfaceConnect(macAddress, this);
		}
	}

	@Override
	public void onConnectSuccess() {
		// TODO Auto-generated method stub
		LogUtils.d("Connect Checkme succeeded.");
	}

	@Override
	public void onConnectFailed(byte errCode) {
		// TODO Auto-generated method stub
		LogUtils.d("Connect Checkme failed.");
	}

	
	/**
	 * Demo for bind BTUtils service
	 * Your can use BTUtils after bind it
	 */
	public void testBindService() {
		bindService(new Intent("com.viatom.azur.BTUtils"), conn,Service.BIND_AUTO_CREATE);
	}
	
	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogUtils.d("BTUtils service connected");
			mBinder = (BTBinder)service;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			LogUtils.d("BTUtils service disconnected");
		}
		
	};
}

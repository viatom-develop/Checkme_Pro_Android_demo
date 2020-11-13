package com.viatom.azur.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.IBinder;

import org.json.JSONObject;

/**
 * Created by gongguopei on 2018/12/27.
 */
public interface BTBinder{

    /**
     * Connect Bluetooth
     * @param bluetoothDevice bluetooth device
     * @param listener
     */
    void interfaceConnect(BluetoothDevice bluetoothDevice, BTConnectListener listener);

    void interfaceParaSync(JSONObject jsonObject, int timeout, ParaSyncThreadListener listener);

    /**
     * For writing files and upgrades Checkme
     * @param fileName
     * @param fileBuf
     * @param cmd
     * @param timeout
     * @param listener
     */
    void interfaceWriteFile(String fileName, byte[] fileBuf, byte cmd, int timeout, WriteFileListener listener);

    /**
     * Read file from Checkme
     * @param fileName
     * @param fileType Enter the file type you define,
     * or enter '0' if you don't want to use it.
     * @param timeOut
     * @param listener
     */
    void interfaceReadFile(String fileName, byte fileType, int timeOut, ReadFileListener listener);

    /**
     * Get Checkme information
     * @param timeOut
     * @param listener
     */
    void interfaceGetInfo(int timeOut, GetInfoThreadListener listener);

    /**
     * Send Ping command
     * @param timeOut
     * @param listener
     */
    void interfacePing(int timeOut, PingThreadListener listener);

    void interfaceInterruptAllThread();

    boolean isAnyThreadRunning();
}

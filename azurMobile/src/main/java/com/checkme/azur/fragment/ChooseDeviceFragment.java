package com.checkme.azur.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.checkme.azur.utils.MsgUtils;
import com.checkme.newazur.R;
import com.checkme.azur.element.CheckmeDevice;
import com.checkme.azur.element.Constant;
import com.checkme.azur.measurement.MeasurementConstant;
import com.checkme.azur.tools.ChooseDeviceAdapter;
import com.checkme.azur.tools.PreferenceUtils;
import com.checkme.azur.tools.ToastUtils;
import com.checkme.azur.utils.FileUtils;
import com.checkme.azur.utils.LogUtils;

public class ChooseDeviceFragment extends Fragment implements OnMenuItemClickListener, OnItemClickListener {

    private View rootView;
    ChooseDeviceAdapter adapter;
    Handler callHandler;
    private Context mContext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mContext = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        rootView = inflater.inflate(R.layout.fragment_choose_device, container, false);
        List<CheckmeDevice> deviceList = getDeviceList();
        initListView(deviceList);

        return rootView;
    }

    public void setCallHandler(Handler callHandler) {
        this.callHandler = callHandler;
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    /**
     * 初始化设备列表
     *
     * @return
     */
    private List<CheckmeDevice> getDeviceList() {

        List<CheckmeDevice> deviceList = new ArrayList<CheckmeDevice>();
        File[] files = new File(Constant.root, "CheckmeMobile/").listFiles();
        if (files == null) {
            return null;
        }
        //获取上次使用设备名称
        String preDeviceName = PreferenceUtils.readStrPreferences(mContext.getApplicationContext()
                , "PreDeviceName");

        //生成列表
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            //只有当file是文件夹且里面有文件
            if (file.isDirectory() && file.listFiles().length != 0) {
                //只有包含usr.dat且文件大小不为零
                File userFile = new File(Constant.root, "CheckmeMobile/" + file.getName() + "/usr.dat");
                File spotUserFile = new File(Constant.root, "CheckmeMobile/" + file.getName() + "/xusr.dat");
                //普通模式和医院模式下，分别判断各自的usr文件是否存在
                String mode = PreferenceUtils.readStrPreferences(mContext.getApplicationContext(), file.getName() + "CKM_MODE");
                if (mode == null || mode.equals(Constant.CKM_MODE_HOME)) {
                    //普通模式
                    if (userFile.exists() && userFile.length() != 0) {
                        String fileName = file.getName();
                        if (fileName.contains("Checkme")) {
                            if(fileName.startsWith("CheckmeLE")) {
                                fileName = fileName.replace("CheckmeLE", "Checkme Lite");
                            }
                            deviceList.add(new CheckmeDevice(fileName, fileName
                                    .equals(preDeviceName) ? true : false));
                        }
                    }
                } else if (mode.equals(Constant.CKM_MODE_HOSPITAL)) {
                    //医院模式
                    if (spotUserFile.exists() && spotUserFile.length() != 0) {
                        String fileName = file.getName();
                        if (fileName.contains("Checkme")) {
                            deviceList.add(new CheckmeDevice(fileName, fileName
                                    .equals(preDeviceName) ? true : false));
                        }
                    }
                }
            }
        }

        return deviceList;
    }

    /**
     * 初始化列表视图
     */
    private void initListView(List<CheckmeDevice> deviceList) {
        if (deviceList == null) {
            return;
        }
        adapter = new ChooseDeviceAdapter(
                mContext.getApplicationContext(), deviceList);
        SwipeMenuListView listView = (SwipeMenuListView) rootView.findViewById(R.id.lv_device);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> group, View view, int pos, long id) {
        // TODO Auto-generated method stub
        //在线不能选择切换设备
        if (Constant.btConnectFlag) {
            ToastUtils.show(mContext.getApplicationContext()
                    , Constant.getString(R.string.change_device_offline));
        } else {
            refreshSelectedView(pos);
            saveDeviceToLocal(getCurSelectedDevice());
            resetCurDevice(getCurSelectedDevice());
        }
    }

    /**
     * 刷新列表选择项
     *
     * @param pos
     */
    private void refreshSelectedView(int pos) {
        if (adapter == null) {
            return;
        }
        //设置选中项为可用设备
        for (int i = 0; i < adapter.getCount(); i++) {
            CheckmeDevice device = (CheckmeDevice) adapter.getItem(i);
            if (i == pos) {
                device.setAvailable(true);
            } else {
                device.setAvailable(false);
            }
        }
        //重新设置listView
        adapter.notifyDataSetChanged();
    }

    /**
     * 保存设备到本地
     *
     * @param device
     */
    private void saveDeviceToLocal(CheckmeDevice device) {
        if (device == null) {
            return;
        }
        PreferenceUtils.savePreferences(mContext.getApplicationContext()
                , "PreDeviceName", device.getName());

        String deviceInfoStr = PreferenceUtils
                .readStrPreferences(mContext.getApplicationContext(),
                        device.getName() + "CKM_DeviceInfo");
        PreferenceUtils.savePreferences(mContext.getApplicationContext(),"PreDeviceInfo", deviceInfoStr);
    }

    /**
     * 获取当前设备在列表中的位置
     *
     * @return
     */
    private CheckmeDevice getCurSelectedDevice() {
        if (adapter == null) {
            return null;
        }
        for (int i = 0; i < adapter.getCount(); i++) {
            CheckmeDevice tempDevice = (CheckmeDevice) adapter.getItem(i);
            if (tempDevice.isAvailable()) {
                return tempDevice;
            }
        }
        return null;
    }


    /**
     * 重新初始化程序数据
     *
     * @param device
     */
    private void resetCurDevice(CheckmeDevice device) {
        if (device == null) {
            return;
        }
        if(!TextUtils.isEmpty(device.getName()) && device.getName().startsWith("Checkme Lite")) {
            String fileName = device.getName();
            fileName = fileName.replace("Checkme Lite", "CheckmeLE");
            Constant.initDir(mContext.getApplicationContext(), fileName);
            readLocalUserList(fileName);
        } else {
            Constant.initDir(mContext.getApplicationContext(), device.getName());
            readLocalUserList(device.getName());
        }

    }

    /**
     * 重读本地用户列表
     */
    private void readLocalUserList(String checkmeName) {
        if (checkmeName == null) {
            return;
        }
        String mode = PreferenceUtils.readStrPreferences(mContext.getApplicationContext(), checkmeName + "CKM_MODE");
        if (mode == null || mode.equals("") || mode.equals("MODE_HOME")) {
            LogUtils.d("普通模式");
            Constant.CKM_MODE = Constant.CKM_MODE_HOME;
            Constant.userList = FileUtils.readUserList(Constant.dir
                    , MeasurementConstant.FILE_NAME_USER_LIST);
            if (Constant.userList != null) {// 存在用户列表文件
                Constant.defaultUser = Constant.userList[0];
                Constant.curUser = Constant.defaultUser;

                MsgUtils.sendMsg(callHandler, Constant.MSG_UPLOAD_TO_CLOUD);
            }
        } else {
            LogUtils.d("医院模式");
            Constant.CKM_MODE = Constant.CKM_MODE_HOSPITAL;
            Constant.spotUserList = FileUtils.readSpotUserList(Constant.dir
                    , MeasurementConstant.FILE_NAME_SPOT_USER_LIST);
            if (Constant.spotUserList != null) {// 存在用户列表文件
                Constant.defaultSpotUser = Constant.spotUserList[0];
                Constant.curSpotUser = Constant.defaultSpotUser;

                MsgUtils.sendMsg(callHandler, Constant.MSG_UPLOAD_TO_CLOUD);
            }
        }
    }

    /**
     * 由mac地址获取已存设备名
     *
     * @param device
     * @return 空为未存设备，有则为设备名
     */
    private String getSavedDeviceName(BluetoothDevice device) {

        if (device == null) {
            return null;
        }
        String preDeviceName = PreferenceUtils.readStrPreferences(
                mContext.getApplicationContext(), device.getAddress());
        LogUtils.d("" + preDeviceName);
        return preDeviceName;
    }


    @Override
    public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
        // TODO Auto-generated method stub
        if (index == 0) {//如果是第0个按键按下

        }

        return false;
    }
}

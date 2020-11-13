package com.checkme.azur.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.checkme.azur.bluetooth.BleUtils;
import com.checkme.azur.bluetooth.ParaSyncThreadListener;
import com.checkme.azur.monitor.ui.MonitorActivity;
import com.checkme.azur.utils.StringUtils;
import com.checkme.bluetooth.Logger;
import com.checkme.newazur.R;
import com.checkme.azur.bluetooth.BTConnectListener;
import com.checkme.azur.bluetooth.BTUtils;
import com.checkme.azur.bluetooth.GetInfoThreadListener;
import com.checkme.azur.bluetooth.PingThreadListener;
import com.checkme.azur.element.CheckmeDevice;
import com.checkme.azur.element.CheckmeMobilePatch;
import com.checkme.azur.element.Constant;
import com.checkme.azur.tools.ClsUtils;
import com.checkme.azur.tools.PreferenceUtils;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.utils.MsgUtils;
import com.checkme.update.*;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends com.checkme.update.BaseActivity implements OnCancelListener
        , PingThreadListener, GetInfoThreadListener, BTConnectListener, ParaSyncThreadListener {
//        , com.viatom.azur.monitor.bt.BTConnectListener {

    private static final int MSG_DIALOG_INIT_PARAMETER = 0;
    private static final int MSG_DIALOG_INIT_SCANING = 1;
    private static final int MSG_DIALOG_INIT_CONNECTING = 2;
    private static final int MSG_DIALOG_DISMISS = 3;
    private static final int MSG_DIALOG_SHOW = 4;
    public Timer timer;
    private static final int SCREEN_SHOT = 1079;

    private static final String CHECKME_DEVICE_NAME = "Checkme"; // 默认设备名，同名设备才加入列表

    private static boolean progressDialogInited = false;
    private static boolean progressDialogShowing = false;
//    private static boolean airTraceStatus = false;
    public static MainActivity instance = null;
    private BluetoothAdapter mBtAdapter;
    public static BluetoothDevice mDevice;
    private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    public String[] tempDeviceList;
    public ViatomBaseAdapter mAdapter;
    public ProgressDialog progressDialog;
    private boolean interruptDiscover = false; //区分是否是自行中断搜索
    private CheckmeMobilePatch patch;//升级补丁（如果有更新得话）
//    private AlertDialog exitDia;
    private AlertDialog deviceChooseDia;
    private CheckmeDevice device;

    RelativeLayout rl_progress_indicator;
    ImageView iv_indicator;
    TextView tv_indicator;

    private DialogHelper attentionDialog;
//    private boolean isShowAttentionDialog = true;
//    private boolean isAttentionDialogVisible = true;

    DialogHelper exitDia;

    private boolean isActivityShow = false;


    DialogHelper locationDialog;
    LocationManager mLocationManager;
    private boolean needShowLocationDialog = true;

    DialogHelper btDialog;
    private boolean needShowBtDialog = true;

    DialogHelper chooseDeviceDialog;
    private boolean needShowChooseDeviceDialog = true;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            //如果连接了不同的机器
            if (!mDevice.getName().equals(PreferenceUtils.readStrPreferences(
                    getApplicationContext(), "PreDeviceName"))) {
                //将当前用户index写成0
                PreferenceUtils.savePreferences(getApplicationContext(), "PreUserIndex", 0);
                // 存储本次连接机器的名字，没名字的存以前记录的名字
                if (mDevice.getName() == null || mDevice.getName().equals("")) {
                    PreferenceUtils.savePreferences(getApplicationContext(),
                            "PreDeviceName", getSavedDeviceName(mDevice));
                } else {
                    PreferenceUtils.savePreferences(getApplicationContext(),
                            "PreDeviceName", mDevice.getName());
                }
            }
            BTUtils.LocalBinder localBind = (BTUtils.LocalBinder) service;
            Constant.binder = localBind.getService();
            // 调用service链接BT，并在handler中接收
            Constant.binder.interfaceConnect(mDevice, MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.println("--Service Disconnected--");
            Constant.btConnectFlag = false;
            Constant.binder = null;
        }
    };

    private ServiceConnection aconn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            //如果连接了不同的机器
            if (!mDevice.getName().equals(PreferenceUtils.readStrPreferences(
                    getApplicationContext(), "PreDeviceName"))) {
                //将当前用户index写成0
                PreferenceUtils.savePreferences(getApplicationContext(), "PreUserIndex", 0);
                // 存储本次连接机器的名字，没名字的存以前记录的名字
                if (mDevice.getName() == null || mDevice.getName().equals("")) {
                    PreferenceUtils.savePreferences(getApplicationContext(),
                            "PreDeviceName", getSavedDeviceName(mDevice));
                } else {
                    PreferenceUtils.savePreferences(getApplicationContext(),
                            "PreDeviceName", mDevice.getName());
                }
            }
            BleUtils.LocalBinder localBind = (BleUtils.LocalBinder) service;
            Constant.binder = localBind.getService();
            // 调用service链接BT，并在handler中接收
            Constant.binder.interfaceConnect(mDevice, MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.println("--Service Disconnected--");
            Constant.btConnectFlag = false;
            Constant.binder = null;
        }
    };



    @Override
    public void onConnectSuccess() {
        MsgUtils.sendMsg(handler, Constant.MSG_BT_CONNECTED);
    }

    @Override
    public void onConnectFailed(byte errCode) {
        MsgUtils.sendMsg(handler, Constant.MSG_BT_CONNECT_TIMEOUT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initReceiver();

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    protected void setWindowFeatureAndFlag() {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    protected void loadAnimator() {
        ImageView iv_logo = (ImageView) findViewById(R.id.MainImgViatomLogo);
        ObjectAnimator animator = ObjectAnimator.ofFloat(iv_logo, "alpha", 0.0f, 1.0f);
        animator.setDuration(1300);
        animator.start();

        rl_progress_indicator
                = (RelativeLayout) findViewById(R.id.rl_progress_indicator);

        iv_indicator = (ImageView) findViewById(R.id.iv_indicator);
        iv_indicator.setBackgroundResource(R.drawable.spinner);

        tv_indicator = (TextView) findViewById(R.id.tv_indicator);
    }

    public void initReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        isActivityShow = true;
//        isShowAttentionDialog
//                = PreferenceUtils.readBoolPreferences(getApplicationContext(),
//                    "isShowAttentionDialog", true);
//        if(isShowAttentionDialog && isAttentionDialogVisible) {
//            attentionDialogInit();
//            attentionShow();
//        } else {
//            isAttentionDialogVisible = false;
//            checkPermissions();
//        }

//        isAttentionDialogVisible = false;
        checkPermissions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        attentionDialogDismiss();
        isActivityShow = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        isActivityShow = false;


    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isActivityShow = true;
    }

    private void attentionDialogInit() {
        attentionDialog = DialogHelper.newInstance(getApplicationContext(), R.layout.dialog_privacy);
//        WebView mWebView = (WebView) attentionDialog.getView(R.id.dialog_webView);
//        mWebView.loadUrl("file:///android_asset/attention.html");
        final CheckBox cb_agree = (CheckBox) attentionDialog.getView(R.id.cb_agree);
        attentionDialog.setStyle(R.style.CustomDialogTheme)
                .setDialogCancelable(false)
                .addListener(R.id.tv_btn_ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(cb_agree.isChecked()) {
                            PreferenceUtils.savePreferences(getApplicationContext(), "isShowAttentionDialog", false);
                        } else {
                            PreferenceUtils.savePreferences(getApplicationContext(), "isShowAttentionDialog", true);
                        }
                        attentionDialog.closeDialog();
//                        isAttentionDialogVisible = false;
                        checkPermissions();
                    }
                });
    }

    private void attentionShow() {
        attentionDialogDismiss();
        if (attentionDialog != null && !attentionDialog.isShowing() && isActivityShow) {
            attentionDialog.show(getSupportFragmentManager(), "privacyDialog");
        }
    }

    private void attentionDialogDismiss() {
        if (attentionDialog != null && attentionDialog.isShowing()) {
            attentionDialog.dismiss();
        }
    }

    private static final int REQUEST_CODE_WRITE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 2;

    private void checkPermissions() {
        if (isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if(isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                boolean isGpsEnable = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isNetLocationEnable = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if(needShowLocationDialog) {
                    if(!isGpsEnable && !isNetLocationEnable) {
                        locationDialogInit();
                        locationDialogShow();
                        return;
                    }
                }
                prepareSearchBT(1000);
                needShowLocationDialog = false;
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION);
                }
            }
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_WRITE_STORAGE_PERMISSION);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_WRITE_STORAGE_PERMISSION);
            }
        }
    }

    private boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void locationDialogInit() {
        locationDialogDismiss();

        locationDialog
                = DialogHelper.newInstance(getApplicationContext(), R.layout.dialog_option);
        locationDialog
                .setStyle(R.style.CustomDialogTheme)
                .setVisible(R.id.tv_dialog_title, View.GONE)
                .setText(R.id.tv_dialog_msg, R.string.tv_dialog_msg_location_service)
                .setText(R.id.tv_btn_positive, R.string.yes)
                .setText(R.id.tv_btn_negative, R.string.no)
                .setDialogCancelable(false)
                .addListener(R.id.tv_btn_negative, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        locationDialogDismiss();
                        needShowLocationDialog = false;
                        prepareSearchBT(1000);
                    }
                })
                .addListener(R.id.tv_btn_positive, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        locationDialogDismiss();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, Constant.REQUEST_LOCATION_SETTINGS);
                    }
                });
    }

    private void locationDialogShow() {
        if (locationDialog != null && !locationDialog.isShowing()
                && isActivityShow && needShowLocationDialog) {
            LogUtils.d("dialog show");
            locationDialog.show(getSupportFragmentManager(), "LocationDialog");
        }
    }

    private void locationDialogDismiss() {
        if (locationDialog != null && locationDialog.isShowing()) {
            locationDialog.closeDialog();
        }
    }

    private void prepareSearchBT(int delay) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                searchBT();
            }
        }, delay);
    }

    public void searchBT() {
        if (!mBtAdapter.isEnabled() && needShowBtDialog) { // 如果蓝牙没开
            LogUtils.d("Bluetooth Adapter disable");
            btDialogInit();
            btDialogShow();
        } else {
            LogUtils.d("Bluetooth Adapter enable");

            // show progress dialog
            progressDialogShow();

            // show device dialog
            chooseDeviceDialogInit();
            chooseDeviceDialogShow();

            // start device discover service
            if (mBtAdapter.isDiscovering()) {
                interruptDiscover = true;
                mBtAdapter.cancelDiscovery();
            }

            mBtAdapter.startDiscovery();
            interruptDiscover = false;
        }
    }

    private void progressDialogShow() {
        rl_progress_indicator.setVisibility(View.VISIBLE);
        AnimationDrawable spinner = (AnimationDrawable) iv_indicator.getBackground();
        spinner.start();
        progressDialogState(R.string.searching);
    }

    private void progressDialogState(@StringRes int stringId) {
        tv_indicator.setText(stringId);
    }

    public void progressDialogDismiss() {
        rl_progress_indicator.setVisibility(View.GONE);
    }

    private void btDialogInit() {
        btDialogDismiss();
        btDialog
                = DialogHelper.newInstance(
                getApplicationContext(),
                R.layout.dialog_bt_turner);
        btDialog
                .setStyle(R.style.CustomDialogTheme)
                .setDialogCancelable(false)
                .setGravity(Gravity.CENTER)
//                .addListener(R.id.tv_btn_bt_enable, turnOnBTDialogListener)
                .addListener(R.id.tv_btn_bt_enable, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btDialogDismiss();
                        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                , Constant.REQUEST_TURN_ON_BT);
                    }
                })
//                .addListener(R.id.tv_btn_offline, turnOnBTDialogListener)
                .addListener(R.id.tv_btn_offline, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtils.d("选择进入offline");
                        needShowBtDialog = false;
                        btDialogDismiss();

                        Constant.btConnectFlag = false;
                        Logger.d("MainActivity", "btDialogInit getPreDeviceName == " + getPreDeviceName());
                        String fileName = getPreDeviceName();
                        if(!TextUtils.isEmpty(fileName) && fileName.startsWith("Checkme Lite")) {
                            fileName = fileName.replace("Checkme Lite", "CheckmeLE");
                            Constant.initDir(fileName);
                        } else {
                            Constant.initDir(getPreDeviceName());
                        }
                        comeIntoNextAct(getPreDeviceName());
                        PreferenceUtils.savePreferences(getApplicationContext(), "BT_MODE", false);
                    }
                });
    }

    private void btDialogShow() {
        btDialogDismiss();
        if (btDialog != null && !btDialog.isShowing()
                && isActivityShow && needShowBtDialog) {
            LogUtils.d("dialog show");
            btDialog.show(getSupportFragmentManager(), "DialogHelper");
        }
    }

    private void btDialogDismiss() {
        if (btDialog != null && btDialog.isShowing()) {
            btDialog.closeDialog();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.REQUEST_TURN_ON_BT) {

            if(resultCode == Activity.RESULT_OK) {
                btDialogDismiss();
                needShowBtDialog = false;
                prepareSearchBT(500);
            } else {
                needShowBtDialog = true;
            }
        } else if (requestCode == Constant.REQUEST_COME_INTO_DLC) {
            LogUtils.d("mainActivity finished");
            //从ChoseUser回来后直接退出
//            if(mDevice != null) {
//                try {
//                    ClsUtils.removeBond(BluetoothDevice.class, mDevice);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            finish();
            System.exit(0);

        } else if(requestCode == Constant.REQUEST_LOCATION_SETTINGS) {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean isGpsEnable = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetLocationEnable = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(!isGpsEnable || !isNetLocationEnable) {
                locationDialogInit();
                locationDialogShow();
            } else {
                needShowLocationDialog = false;
//                prepareSearchBT(1000);
            }
        } else if(requestCode == com.checkme.azur.monitor.element.Constant.REQUEST_COME_INTO_MONITOR) {
            finish();
        }
    }


    private void chooseDeviceDialogInit() {
        chooseDeviceDialogDismiss();
        if(!needShowChooseDeviceDialog) {
            return;
        }
        chooseDeviceDialog = DialogHelper.newInstance(getApplicationContext(),
                R.layout.dialog_choose_device);
        chooseDeviceDialog.setStyle(R.style.CustomDialogTheme);
        chooseDeviceDialog.setCancelable(false);
        chooseDeviceDialog.setGravity(Gravity.BOTTOM);
        chooseDeviceDialog.setBottomMargin(50);
        final List<String> deviceStrList = new ArrayList<>();
        deviceStrList.add(Constant.getString(R.string.view_offline_data));
        for (int i = 0; i < deviceList.size(); i++) {
            BluetoothDevice device = deviceList.get(i);
            if (TextUtils.isEmpty(device.getName())) {
                deviceStrList.add(getSavedDeviceName(device));
//                tempDeviceList[i] = getSavedDeviceName(device) + "\n" + device.getAddress();
            } else {
                if (deviceList.get(i).getName().contains("%")) {
                    deviceStrList.add(deviceList.get(i).getName().split("%")[0]);
//                    tempDeviceList[i] = deviceList.get(i).getName().split("%")[0]
//                            + "\n" + deviceList.get(i).getAddress();
                } else {
                    deviceStrList.add(deviceList.get(i).getName());
//                    tempDeviceList[i] = deviceList.get(i).getName()
//                            + "\n" + deviceList.get(i).getAddress();
                }
            }
        }
        TextView dialogTitle = (TextView) chooseDeviceDialog.getView(R.id.tv_dialog_title_choose_device);
        if(deviceList.size() > 0) {
            dialogTitle.setText(R.string.tv_dialog_title_choose_device);
        } else {
            dialogTitle.setText(R.string.tv_dialog_title_no_device_found);
        }
        ListView listView = (ListView) chooseDeviceDialog.getView(R.id.lv_list_bt_devices);
        listView.setVisibility(View.VISIBLE);
        mAdapter = new ViatomBaseAdapter<String>(listView, deviceStrList, R.layout.item_device_found){
            @Override
            public void convert(AdapterHolder helper, String item, boolean isScrolling, int position) {
                TextView view = helper.getView(R.id.tv_item_device);
                if(!TextUtils.isEmpty(item) && item.startsWith("CheckmeLE")) {
                    item = item.replace("CheckmeLE", "Checkme Lite");
                }
                if(position == (getCount() - 1) ) {
                    view.setText(item);
                    view.setBackgroundResource(R.drawable.dialog_item_bottom);
                } else {
                    view.setText(item);
                    view.setBackgroundResource(R.drawable.dialog_item_normal);
                }
            }
        };
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chooseDeviceDialogDismiss();
                needShowChooseDeviceDialog = false;
                interruptDiscover = true;
                if (mBtAdapter.isDiscovering()) {
                    mBtAdapter.cancelDiscovery();
                }
//                progressDialogManager(MSG_DIALOG_INIT_CONNECTING);
                progressDialogState(R.string.connecting);
                if(position == 0) {
                    Constant.btConnectFlag = false;
                    Logger.d("MainActivity", "chooseDeviceDialogInit getPreDeviceName == " + getPreDeviceName());
                    String fileName = getPreDeviceName();
                    if(!TextUtils.isEmpty(fileName) && fileName.startsWith("Checkme Lite")) {
                        fileName = fileName.replace("Checkme Lite", "CheckmeLE");
                        Constant.initDir(fileName);
                    } else {
                        Constant.initDir(getPreDeviceName());
                    }
                    comeIntoNextAct(getPreDeviceName());
                    PreferenceUtils.savePreferences(getApplicationContext(), "BT_MODE", false);
                } else {
                    if(position > deviceList.size() ) {
                        LogUtils.d("该设备已不在列表中");
                        exitDialogInit();
                        exitDialogShow();
                    } else {
                        String deviceAddress = deviceList.get(position - 1).getAddress();
                        mDevice = mBtAdapter.getRemoteDevice(deviceAddress);

                        String deviceName = mDevice.getName();
                        if(!TextUtils.isEmpty(deviceName) && deviceName.startsWith("CheckmeLE")) {
                            Intent mIntent = new Intent();
                            mIntent.setAction("com.viatom.azur.BleUtils");
                            mIntent.setPackage(getPackageName());
                            bindService(mIntent, aconn, Service.BIND_AUTO_CREATE);
                        } else {
                            if (mDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                                LogUtils.d("未配对，启动配对");
                                try {
                                    ClsUtils.cancelPairingUserInput(mDevice.getClass(), mDevice);
                                    ClsUtils.createBond(mDevice.getClass(), mDevice);
                                } catch (Exception e) {
                                    LogUtils.d("反射启动配对失败");
                                    e.printStackTrace();
                                }
                            } else {
                                LogUtils.d("已配对，开始连接");
                                Intent mIntent = new Intent();
                                mIntent.setAction("com.viatom.azur.BTUtils");
                                mIntent.setPackage(getPackageName());
                                bindService(mIntent, conn, Service.BIND_AUTO_CREATE);
                            }
                        }


                        PreferenceUtils.savePreferences(getApplicationContext(), "BT_MODE", true);
                    }

                }
            }
        });
    }

    private void chooseDeviceDialogShow() {
        if(chooseDeviceDialog != null && !chooseDeviceDialog.isShowing()
                && isActivityShow && needShowChooseDeviceDialog) {
            chooseDeviceDialog.show(getSupportFragmentManager(), "DeviceChoose");
        }
    }

    public void chooseDeviceDialogDismiss() {
        if(chooseDeviceDialog != null) {
            chooseDeviceDialog.closeDialog();
        }
    }

    private void exitDialogInit() {
        exitDialogDismiss();
        exitDia = DialogHelper.newInstance(getApplicationContext(), R.layout.dialog_option);
        exitDia.setStyle(R.style.CustomDialogTheme)
                .setText(R.id.tv_dialog_title, R.string.warning)
                .setText(R.id.tv_dialog_msg, R.string.connect_failed)
                .setText(R.id.tv_btn_positive, R.string.exit)
                .setVisible(R.id.tv_btn_negative, View.GONE)
                .setDialogCancelable(false)
                .addListener(R.id.tv_btn_positive, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        exitDialogDismiss();
                        if(mDevice != null) {
                            try {
                                ClsUtils.removeBond(mDevice.getClass(), mDevice);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        finish();
                        System.exit(0);
                    }
                });
    }

    private void exitDialogShow() {
        if (exitDia != null && !exitDia.isShowing() && isActivityShow) {
            LogUtils.d("dialog show");
            exitDia.show(getSupportFragmentManager(), "ExitDilog");
        }
    }
    private void exitDialogDismiss() {
        if (exitDia != null && exitDia.isShowing()) {
            LogUtils.d("dialog show");
            exitDia.closeDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE_WRITE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                prepareSearchBT();
                checkPermissions();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    checkPermissions();
                } else {
                    // user deny permission request and check "not ask again"
                    Toast.makeText(getApplicationContext(),
                            "We need write external storage permission to save data.",
                            Toast.LENGTH_LONG).show();
                }
            }
        } else if(requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                prepareSearchBT();
                checkPermissions();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    checkPermissions();
                } else {
                    // user deny permission request and check "not ask again"
                    Toast.makeText(getApplicationContext(),
                            "We need location permission to discover your device.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    /**
     * 准备开始搜索蓝牙
     *
     * @param patch
     */
    private void showUpdateView(CheckmeMobilePatch patch) {
        if (patch == null) {
            return;
        }

        this.patch = patch;

        new AlertDialog.Builder(MainActivity.this)
                .setTitle(Constant.getString(R.string.notice))
                .setMessage(Constant.getString(R.string.new_app_available))
                .setPositiveButton(Constant.getString(R.string.update), updateDialogListener)
                .setNegativeButton(Constant.getString(R.string.cancel), updateDialogListener)
                .setCancelable(false)
                .setOnCancelListener(this).show();
    }


    /**
     * 蓝牙没开对话框监听
     */
    private OnClickListener updateDialogListener = new OnClickListener() {

        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            // TODO Auto-generated method stub
            if (arg1 == AlertDialog.BUTTON_POSITIVE) {
                //依然先初始化目录
                Constant.initDir(getApplicationContext(), getPreDeviceName());
                //直接进入aboutApp界面升级
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, AboutApp.class);
                if (patch != null) {
                    intent.putExtra("PatchAddress", patch.getAddress());
                }
                startActivityForResult(intent, Constant.REQUEST_COME_INTO_DLC);
            } else if (arg1 == AlertDialog.BUTTON_NEGATIVE) {
                LogUtils.d("选锟斤拷锟斤拷");
                prepareSearchBT(0);
            }
        }
    };

    /**
     * 蓝牙没开对话框监听
     */
    private OnClickListener turnOnBTDialogListener = new OnClickListener() {

        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            if (arg1 == AlertDialog.BUTTON_POSITIVE) {
                LogUtils.d("选择打开蓝牙");
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        , Constant.REQUEST_TURN_ON_BT);
            } else if (arg1 == AlertDialog.BUTTON_NEGATIVE) {
                LogUtils.d("选择进入offline");
                Constant.btConnectFlag = false;
                Constant.initDir(getApplicationContext(), getPreDeviceName());
                comeIntoNextAct(getPreDeviceName());
                PreferenceUtils.savePreferences(getApplicationContext(), "BT_MODE", false);
            }
        }
    };

    /**
     * 没设备对话框监听
     */
    private OnClickListener NoDeviceDialogListener = new OnClickListener() {

        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            // TODO Auto-generated method stub
            if (arg1 == AlertDialog.BUTTON_POSITIVE) {
                LogUtils.d("选择重搜");
                deviceList.removeAll(deviceList);
                searchBT();
            } else if (arg1 == AlertDialog.BUTTON_NEGATIVE) {
                LogUtils.d("选择退出");
                finish();
            } else if (arg1 == AlertDialog.BUTTON_NEUTRAL) {
                Constant.btConnectFlag = false;
                Constant.initDir(getApplicationContext(), getPreDeviceName());
                comeIntoNextAct(getPreDeviceName());
            }
        }
    };

//    private void bindAirTraceService() {
//        Intent mIntent = new Intent();
//        mIntent.setAction("com.viatom.azur.monitor.BTUtils");
//        mIntent.setPackage(getPackageName());
//        bindService(mIntent, aconn, Service.BIND_AUTO_CREATE);
//    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogUtils.d("come into handler,msg.what=" + msg.what);
            switch (msg.what) {
                case Constant.MSG_BT_FIND:
                    exitDialogDismiss();
                    processDeviceFound();
                    break;
                case Constant.MSG_BT_CONNECTED:
                    chooseDeviceDialogDismiss();
                    Constant.btConnectFlag = true;
                    Constant.binder.interfacePing(8000, MainActivity.this);
                    break;
                case Constant.MSG_BT_CONNECT_TIMEOUT:
                    exitDialogInit();
                    exitDialogShow();
                    break;
                case Constant.MSG_PING_FAILED:
                    LogUtils.d("PING FAILED");
                    exitDialogInit();
                    exitDialogShow();
                    break;
//                case Constant.MSG_GO_TO_MONITOR:
//                    LogUtils.d("enter monitor");
//                    Constant.binder.interfaceInterruptAllThread();
//                    unbindService(conn);
//                    Intent intent = new Intent();
//                    intent.setPackage(getPackageName());
//                    intent.setAction("com.viatom.azur.BTUtils");
//                    stopService(intent);
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            bindAirTraceService();
//                        }
//                    }, 100);
//                    break;
                case Constant.MSG_PING_SUCCESS:
                    LogUtils.d("PING SUCCESS");
                    Constant.binder.interfaceGetInfo(1000, MainActivity.this);
                    break;
                case Constant.CMD_TYPE_CKM_INFO:
                    String deviceInfoStr = (String) msg.obj;
                    LogUtils.d("deviceInfoStr == " + deviceInfoStr);

                    device = CheckmeDevice.decodeCheckmeDevice((String) msg.obj);
                    PreferenceUtils.savePreferences(getApplicationContext(), Constant.FILE_VER_KEY, device.getFileVer());
                    PreferenceUtils.savePreferences(getApplicationContext()
                            , getSavedDeviceName(mDevice) + "CKM_MODE", device.getMode());
                    String name = PreferenceUtils.readStrPreferences(getApplicationContext(), "PreDeviceName");
                    PreferenceUtils.savePreferences(getApplicationContext(), name + Constant.SN, device.getSn());

                    PreferenceUtils.savePreferences(getApplicationContext()
                            , getSavedDeviceName(mDevice) + "CKM_DeviceInfo", deviceInfoStr);
                    PreferenceUtils.savePreferences(getApplicationContext(),"PreDeviceInfo", deviceInfoStr);

//				    processCheckmeInfo(device);
                    JSONObject objectSetTime = new JSONObject();
                    try {
                        objectSetTime.put(Constant.SET_TIME, StringUtils.makeSetTimeString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Constant.binder.interfaceParaSync(objectSetTime, 5000, MainActivity.this);
                    break;
                case Constant.MSG_CHECKME_MOBILE_PATCH_EXIST:
                    showUpdateView((CheckmeMobilePatch) msg.obj);
                    break;
                case Constant.MSG_CHECKME_MOBILE_PATCH_NOT_EXIST:
                    prepareSearchBT(500);
                    break;
                case Constant.MSG_PARA_SYNC_SUCCESS:
                    if (device != null)
                        processCheckmeInfo(device);
                    break;
                case Constant.MSG_PARA_SYNC_FAILED:
                    LogUtils.d("PARASYNC失败");
                    if (device != null)
                        processCheckmeInfo(device);
                    break;
//                case Constant.MSG_TRACE_CONNECT_SUCCESS:
//                    chooseDeviceDialogDismiss();
//                    Constant.btConnectFlag = true;
//                    comeIntoNextAct();
//                    break;
//                case Constant.MSG_TRACE_CONNECT_FAILED:
//                    exitDialogInit();
//                    exitDialogShow();
//                    break;
            }
        }
    };

    /**
     * 找到device后处理
     */
    protected void processDeviceFound() {
//        if (deviceList.size() == 0) {
//        progressDialogManager(MSG_DIALOG_DISMISS);
//			new AlertDialog.Builder(MainActivity.this)
//					.setTitle(Constant.getString(R.string.loading))
//					.setMessage(Constant.getString(R.string.no_device))
//					.setPositiveButton(Constant.getString(R.string.yes),NoDeviceDialogListener)
//					.setNegativeButton(Constant.getString(R.string.no),NoDeviceDialogListener)
//					.setNeutralButton(Constant.getString(R.string.offline),NoDeviceDialogListener)
//					.setCancelable(false).show();
//        } else {//锟斤拷锟借备
//            progressDialogManager(MSG_DIALOG_DISMISS);
//            tempDeviceList = new String[deviceList.size()];
//            for (int i = 0; i < deviceList.size(); i++) {
//                BluetoothDevice device = deviceList.get(i);
//                //没锟斤拷锟街碉拷锟借备锟斤拷锟芥储锟斤拷锟斤拷示
//                if (device.getName() == null || device.getName().equals("")) {
//                    tempDeviceList[i] = getSavedDeviceName(device) + "\n" + device.getAddress();
//                } else {
//                    if (deviceList.get(i).getName().contains("%")) {
//                        tempDeviceList[i] = deviceList.get(i).getName().split("%")[0]
//                                + "\n" + deviceList.get(i).getAddress();
//                    } else {
//                        tempDeviceList[i] = deviceList.get(i).getName()
//                                + "\n" + deviceList.get(i).getAddress();
//                    }
//
//                }
//            }
//
//            deviceChooseDia = new AlertDialog.Builder(MainActivity.this)
//                    .setTitle(Constant.getString(R.string.choose_device))
//                    .setItems(tempDeviceList, HaveDeviceDialogListener)
//                    .setNeutralButton(Constant.getString(R.string.exit), HaveDeviceDialogListener)
//                    .setNegativeButton(Constant.getString(R.string.offline), HaveDeviceDialogListener)
//                    .setCancelable(false)
//                    .show();
//        }

//        if(!isAttentionDialogVisible) {
//            chooseDeviceDialogInit();
//            chooseDeviceDialogShow();
//        }

        chooseDeviceDialogInit();
        chooseDeviceDialogShow();
    }

    /**
     *
     *
     * @param device
     */
    private void processCheckmeInfo(CheckmeDevice device) {
        Constant.initDir(getSavedDeviceName(mDevice));
        comeIntoNextAct(getSavedDeviceName(mDevice));
    }


    /**
     *
     */
    public void comeIntoNextAct(String checkmeName) {

//        if (checkmeName == null) {
//            comeIntoDailyCheck(checkmeName);
//            return;
//        }
//        String mode = PreferenceUtils.readStrPreferences(getApplicationContext(), checkmeName + "CKM_MODE");
//        if (mode == null || mode.equals("") || mode.equals("MODE_HOME")) {
//            LogUtils.d("Mode Home");
//            comeIntoDailyCheck(checkmeName);
//        } else {
//            LogUtils.d("Mode Hospital");
//            comeIntoSpotCheck(checkmeName);
//        }

        if (mBtAdapter.isDiscovering()) {
            interruptDiscover = true;
            mBtAdapter.cancelDiscovery();
        }

        if (TextUtils.isEmpty(checkmeName)) {
            comeIntoDailyCheck(checkmeName);
            return;
        }

        String mode = PreferenceUtils.readStrPreferences(getApplicationContext(), checkmeName + "CKM_MODE");
        if (TextUtils.isEmpty(mode) || mode.equals("MODE_HOME")) {
            LogUtils.d("Mode Home");
            comeIntoDailyCheck(checkmeName);
        } else {
            LogUtils.d("Mode Hospital");
            comeIntoSpotCheck(checkmeName);
        }
    }

    /**
     * 进入dlc界面
     */
    public void comeIntoDailyCheck(String checkmeName) {
        if (mBtAdapter.isDiscovering()) {
            interruptDiscover = true;
            mBtAdapter.cancelDiscovery();
        }

        LogUtils.d("进入DLC界面");
        PreferenceUtils.savePreferences(getApplicationContext()
                , checkmeName + "CKM_MODE", Constant.CKM_MODE_HOME);
        //		FileDriver.delFile(Constant.dir, MeasurementConstant.FILE_NAME_SPOT_USER_LIST);

        Constant.CKM_MODE = Constant.CKM_MODE_HOME;
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, DailyCheck.class);
        intent.putExtra("modeType", 1);
        startActivityForResult(intent, Constant.REQUEST_COME_INTO_DLC);

        exitDialogDismiss();
        chooseDeviceDialogDismiss();

    }


    public void comeIntoSpotCheck(String checkmeName) {


        if (mBtAdapter.isDiscovering()) {
            interruptDiscover = true;
            mBtAdapter.cancelDiscovery();
        }

        PreferenceUtils.savePreferences(getApplicationContext()
                , checkmeName + "CKM_MODE", Constant.CKM_MODE_HOSPITAL);


        Constant.CKM_MODE = Constant.CKM_MODE_HOSPITAL;
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SpotCheck.class);
        intent.putExtra("modeType", 2);
        startActivityForResult(intent, Constant.REQUEST_COME_INTO_DLC);

        exitDialogDismiss();

        chooseDeviceDialogDismiss();
    }

    @Override
    public void onPingSuccess() {
        MsgUtils.sendMsg(handler, Constant.MSG_PING_SUCCESS);
    }

    @Override
    public void onPingFailed(byte errCode) {
//        if (errCode != PingThreadListener.ERR_CODE_MONITOR)
//            MsgUtils.sendMsg(handler, Constant.MSG_PING_FAILED);
//        else
//            MsgUtils.sendMsg(handler, Constant.MSG_GO_TO_MONITOR);

        MsgUtils.sendMsg(handler, Constant.MSG_PING_FAILED);
    }

    @Override
    public void onGetInfoSuccess(String checkmeInfo) {
        MsgUtils.sendMsg(handler, checkmeInfo, Constant.CMD_TYPE_CKM_INFO);
    }

    @Override
    public void onGetInfoFailed(byte errCode) {
        //后续处理
        MsgUtils.sendMsg(handler, Constant.MSG_GET_INFO_FAILED);
    }

    /**
     *
     *
     * @param device
     * @return
     */
    private boolean isRightDeviceType(BluetoothDevice device) {
        if (device == null) {
            return false;
        }

        if(TextUtils.isEmpty(device.getName())) {
            return false;
        }

        if(TextUtils.isEmpty(device.getName().trim())) {
            return false;
        }

        //获得设备类型（只有Android 4.4以上才有用）
        try {
            int deviceType = ClsUtils.getType(device.getClass(), device);
            String deviceName = device.getName();
            LogUtils.d("deviceType" + deviceType);
            if (deviceType == BluetoothDevice.DEVICE_TYPE_LE
                    && device.getName().startsWith("CheckmeLE")) {
                return true;
            }

            if (device.getName().startsWith("Checkme")) {
                if(deviceType == BluetoothDevice.DEVICE_TYPE_CLASSIC ||
                        deviceType == BluetoothDevice.DEVICE_TYPE_DUAL)
                return true;
            }
        } catch (Exception e) {
            e.getMessage();
        }
        return false;
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
                getApplicationContext(), device.getAddress());
        LogUtils.d("" + preDeviceName);
        return preDeviceName;
    }

    /**
     * 获取上次连接的设备名
     *
     * @return
     */
    private String getPreDeviceName() {
        String name = PreferenceUtils.readStrPreferences(getApplicationContext(), "PreDeviceName");
        LogUtils.d("上次使用设备名：" + name);
        return name;
    }

//    /**
//     *
//     *
//     * @param device
//     * @return
//     */
//    private boolean isRightDeviceName(BluetoothDevice device) {
//        if (device.getName() == null || device.getName().equals("")) {
//            return false;
//        }
//
//        if (!device.getName().contains(CHECKME_DEVICE_NAME)) {
//            LogUtils.d("不是checkme");
//            return false;
//        }
//
//        LogUtils.d("是正确设备名");
//        return true;
//    }

    // 查找到设备和搜索完成action监听器
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                LogUtils.d("device found " + device.getName() + " ****" + device.getAddress());
                Logger.d(MainActivity.class, "isRightDeviceType(device) == " + isRightDeviceType(device));
                //  是蓝牙2.0且不存在列表内
                if (isRightDeviceType(device) && !deviceList.contains(device)) {
//                    if (isRightDeviceName(device)) {
//                        PreferenceUtils.savePreferences(getApplicationContext(),
//                                device.getAddress(), device.getName());
//                        deviceList.add(device);
//                        MsgUtils.sendMsg(handler, Constant.MSG_BT_FIND);
//                    }
                    PreferenceUtils.savePreferences(getApplicationContext(),
                            device.getAddress(), device.getName());
                    deviceList.add(device);
                    MsgUtils.sendMsg(handler, Constant.MSG_BT_FIND);
                }

            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
//                    if (!airTraceStatus) {
//                        LogUtils.d("完成配对");
//                        Intent mIntent = new Intent("com.viatom.azur.BTUtils");
//                        mIntent.setPackage(getPackageName());
//                        bindService(mIntent, conn, Service.BIND_AUTO_CREATE);
//                    } else {
//                        LogUtils.d("完成配对");
//                        Intent mIntent = new Intent();
//                        mIntent.setAction("com.viatom.azur.monitor.BTUtils");
//                        mIntent.setPackage(getPackageName());
//                        bindService(mIntent, aconn, Service.BIND_AUTO_CREATE);
//                    }
                    LogUtils.d("完成配对");
                    Intent mIntent = new Intent("com.viatom.azur.BTUtils");
                    mIntent.setPackage(getPackageName());
                    bindService(mIntent, conn, Service.BIND_AUTO_CREATE);
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {

                if (Constant.btConnectFlag) {
                    Constant.btConnectFlag = false;
                    unbindService(conn);//debug warning
                    onConnectFailed(BTConnectListener.ERR_CODE_NORMAL);
//                    if (!airTraceStatus) {
//                        unbindService(conn);//debug warning
//                        onConnectFailed(BTConnectListener.ERR_CODE_NORMAL);
//                    } else {
//                        unbindService(aconn);// debug warning
//
//                        Message msg = new Message();
//                        msg.what = SCREEN_SHOT;
//                        LogUtils.d("断开蓝牙调用");
//                        MonitorActivity.instance.mHandler.sendMessage(msg);
//                        TimerTask timerTask = new TimerTask() {
//                            @Override
//                            public void run() {
//                                // TODO Auto-generated method stub
//                                if (mBtAdapter.isDiscovering()) {
//                                    mBtAdapter.cancelDiscovery();
//                                }
//                                mBtAdapter.startDiscovery();
//                            }
//                        };
//                        timer = new Timer();
//                        timer.schedule(timerTask, 0, 2000);
//                    }
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                LogUtils.d("扫描完成");
                if (deviceList != null && deviceList.size() != 0) {
                    deviceList.clear();
                }
                if (!interruptDiscover) {
                    mBtAdapter.startDiscovery();
                }
                if (deviceList.size() == 0 && !interruptDiscover) {
                    MsgUtils.sendMsg(handler, Constant.MSG_BT_FIND);
                }

//                if (!airTraceStatus){
//                    if (deviceList != null && deviceList.size() != 0) {
//                        deviceList.clear();
//                    }
//                    if (!interruptDiscover) {
//                        mBtAdapter.startDiscovery();
//                    }
//
//                }else {
//                    LogUtils.d("搜索完成");
//                    // 如果没有设备，且不是自行中断搜搜
//                    if (deviceList.size() == 0 && !interruptDiscover) {
//                        MsgUtils.sendMsg(handler, Constant.MSG_BT_FIND);
//                    }
//                }

            }
        }
    };

    private void comeIntoNextAct() {
//        airTraceStatus = true;
        instance = MainActivity.this;
        com.checkme.azur.monitor.element.Constant.initDir();
        com.checkme.azur.monitor.element.Constant.init(MainActivity.this);
        if (MonitorActivity.instance != null) {
            if (MonitorActivity.instance.progressDialog != null) {
                MonitorActivity.instance.progressDialog.dismiss();
            }

            MonitorActivity.instance.finish();
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        Intent intent = new Intent();
        intent.setClass(MainActivity.this, MonitorActivity.class);

        startActivityForResult(intent, com.checkme.azur.monitor.element.Constant.REQUEST_COME_INTO_MONITOR);
    }


    public void onDestroy() {
        super.onDestroy();
        if (mBtAdapter != null && mBtAdapter.isDiscovering()) {
            interruptDiscover = true;
            mBtAdapter.cancelDiscovery();

        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);//离线模式下已注销接收器
        }
        mBtAdapter = null;
        progressDialogDismiss();
        //清除数据缓存
        Constant.destroyVariable();
        com.checkme.azur.monitor.element.Constant.destroyVariable();
        //关闭蓝牙服务
        if (Constant.btConnectFlag) {
            LogUtils.d("destory main act, unbind service===");
            LogUtils.d("BM88:unbind service");
            unbindService(conn);
            Intent intent = new Intent();
            intent.setPackage(getPackageName());
            intent.setAction("com.viatom.azur.BTUtils");
            stopService(intent);
//            if (!airTraceStatus) {
//                LogUtils.d("BM88:unbind service");
//                unbindService(conn);
//                Intent intent = new Intent();
//                intent.setPackage(getPackageName());
//                intent.setAction("com.viatom.azur.BTUtils");
//                stopService(intent);
//            } else {
//                LogUtils.d("AirTrace:unbind service");
//                unbindService(aconn);
//                Intent airIntent = new Intent();
//                airIntent.setAction("com.viatom.azur.monitor.BTUtils");
//                airIntent.setPackage(getPackageName());
//                stopService(airIntent);
//            }
        }
//        airTraceStatus = false;
        Process.killProcess(Process.myPid());
        System.exit(0);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onCancel(DialogInterface arg0) {
        // TODO Auto-generated method stub
        finish();
    }

    @Override
    public void onParaSyncSuccess() {
        MsgUtils.sendMsg(handler, Constant.MSG_PARA_SYNC_SUCCESS);

    }

    @Override
    public void onParaSyncFailed(byte errCode) {
        MsgUtils.sendMsg(handler, Constant.MSG_PARA_SYNC_FAILED);

    }

//    @Override
//    public void onTraceConnectSuccess() {
//        MsgUtils.sendMsg(handler, Constant.MSG_TRACE_CONNECT_SUCCESS);
//
//    }
//
//    @Override
//    public void onTraceConnectFailed(byte errCode) {
//        if (MonitorActivity.instance == null) {
//            MsgUtils.sendMsg(handler, Constant.MSG_TRACE_CONNECT_FAILED);
//        } else {
//            MsgUtils.sendMsg(MonitorActivity.instance.mHandler,
//                    Constant.MSG_TRACE_CONNECT_FAILED);
//        }
//    }
}


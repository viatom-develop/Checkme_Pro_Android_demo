package com.viatom.azur.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.viatom.azur.EventBusEvent.FlushIvEvent;
import com.viatom.azur.EventBusEvent.NetWorkEvent;
import com.viatom.azur.element.CEApplication;
import com.viatom.azur.element.CheckmeDevice;
import com.viatom.azur.tools.NetWorkUtils;
import com.viatom.azur.tools.PreferenceUtils;
import com.viatom.azur.utils.InternetUtils;
import com.viatom.azur.utils.LogUtils;
import com.viatom.azur.utils.MsgUtils;
import com.viatom.azur.utils.UploadListUtils;
import com.viatom.azur.utils.UploadQueUtils;
import com.viatom.bluetooth.Logger;
import com.viatom.newazur.R;
import com.viatom.azur.element.Constant;
import com.viatom.azur.fragment.LeftMenuFragment;
import com.viatom.azur.measurement.SpotUser.SpotUserInfo;
import com.viatom.azur.tools.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.image.ImageOptions;
import org.xutils.x;

public class BaseActivity extends SlidingFragmentActivity implements OnClickListener {

    protected ListFragment mFrag;
    protected long exitTime = 0;
    protected DbManager db;

    private Handler baseHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.MSG_BNDLC_CLICKED:
                    getSlidingMenu().showContent();
                    if(isTheSameIntent(msg.what)) {
                        return;
                    }
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent().setClass(BaseActivity.this, DailyCheck.class));
                            overridePendingTransition(R.anim.hold, R.anim.hold);
                            finish();
                        }
                    }, 150);
                    break;
                case Constant.MSG_BNSPOT_CLICKED:
                    getSlidingMenu().showContent();
                    if(isTheSameIntent(msg.what)) {
                        return;
                    }
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent().setClass(BaseActivity.this, SpotCheck.class));
                            overridePendingTransition(R.anim.hold, R.anim.hold);
                            finish();
                        }
                    }, 150);
                    break;
                case Constant.MSG_BNECG_CLICKED:
                    getSlidingMenu().showContent();
                    if(isTheSameIntent(msg.what)) {
                        return;
                    }
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent().setClass(BaseActivity.this, ECGMain.class));
                            overridePendingTransition(R.anim.hold, R.anim.hold);
                            finish();
                        }
                    }, 150);
                    break;
                case Constant.MSG_BNSPO2_CLICKED:
                    getSlidingMenu().showContent();
                    if(isTheSameIntent(msg.what)) {
                        return;
                    }
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent().setClass(BaseActivity.this, SPO2Main.class));
                            overridePendingTransition(R.anim.hold, R.anim.hold);
                            finish();
                        }
                    }, 150);
                    break;
                case Constant.MSG_BNTEMP_CLICKED:
                    getSlidingMenu().showContent();
                    if(isTheSameIntent(msg.what)) {
                        return;
                    }
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent().setClass(BaseActivity.this, TempMain.class));
                            overridePendingTransition(R.anim.hold, R.anim.hold);
                            finish();
                        }
                    }, 150);
                    break;
                case Constant.MSG_BNBP_CLICKED:
                    getSlidingMenu().showContent();
                    if(isTheSameIntent(msg.what)) {
                        return;
                    }
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent().setClass(BaseActivity.this, BPActivity.class));
                            overridePendingTransition(R.anim.hold, R.anim.hold);
                            finish();
                        }
                    }, 150);
                    break;
                case Constant.MSG_BNSLM_CLICKED:
                    getSlidingMenu().showContent();
                    if(isTheSameIntent(msg.what)) {
                        return;
                    }
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent().setClass(BaseActivity.this, SLMMain.class));
                            overridePendingTransition(R.anim.hold, R.anim.hold);
                            finish();
                        }
                    }, 150);
                    break;
                case Constant.MSG_BNPED_CLICKED:
                    getSlidingMenu().showContent();
                    if(isTheSameIntent(msg.what)) {
                        return;
                    }
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent().setClass(BaseActivity.this, PedMain.class));
                            overridePendingTransition(R.anim.hold, R.anim.hold);
                            finish();
                        }
                    }, 150);
                    break;
                case Constant.MSG_BNABOUT_CKM_CLICKED:
                    getSlidingMenu().showContent();
                    if(isTheSameIntent(msg.what)) {
                        return;
                    }
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent().setClass(BaseActivity.this, AboutCheckme.class));
                            overridePendingTransition(R.anim.hold, R.anim.hold);
                            finish();
                        }
                    }, 150);
                    break;
                case Constant.MSG_BNSETTINGS_CLICKED:
                    getSlidingMenu().showContent();
                    if(isTheSameIntent(msg.what)) {
                        return;
                    }
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent().setClass(BaseActivity.this, SettingsActivity.class));
                            overridePendingTransition(R.anim.hold, R.anim.hold);
                            finish();
                        }
                    }, 150);
                    break;
                case Constant.MSG_BNDOWNLOAD_CLICKED:
                    getSlidingMenu().showContent();
                    if(isTheSameIntent(msg.what)) {
                        return;
                    }
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent().setClass(BaseActivity.this, DownloadActivity.class));
                            overridePendingTransition(R.anim.hold, R.anim.hold);
                            finish();
                        }
                    }, 150);
                    break;
                case Constant.MSG_BNABOUT_APP_CLICKED:
                    getSlidingMenu().showContent();
                    if(isTheSameIntent(msg.what)) {
                        return;
                    }
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            startActivity(new Intent().setClass(BaseActivity.this, AboutApp.class));
                            overridePendingTransition(R.anim.hold, R.anim.hold);
                            finish();
                        }
                    }, 150);
                    break;
                case Constant.MSG_PATIENT_SUCCESS:
                    final JSONObject result = (JSONObject) msg.obj;
                    x.task().run(new Runnable() {
                        @Override
                        public void run() {
                            InternetUtils.savePatientId(result, getApplicationContext());
                            UploadQueUtils.makeDLCUploadQue(getApplicationContext(), db, baseHandler);
                            LogUtils.d("save patient==");
                        }
                    });
                    Constant.sUploadState = Constant.UPLOADING;
                    EventBus.getDefault().post(new FlushIvEvent(Constant.UPLOADING));
                    break;
                case Constant.MSG_PATIENT_ERROR:
                    Constant.sUploadState = Constant.UPLOADING;
                    EventBus.getDefault().post(new FlushIvEvent(Constant.UPLOADING));
                    Constant.cancelPost();
                    uploadToCloud();
                    break;
                case Constant.MSG_OBSERVATION_ERROR:
                    Constant.sUploadState = Constant.UPLOADING;
                    EventBus.getDefault().post(new FlushIvEvent(Constant.UPLOADING));
                    Constant.cancelPost();
                    uploadToCloud();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    public boolean isTheSameIntent(int menuTag) {
        return false;
    }

    public void uploadToCloud() {
        initCloudSectionSettings();
        PreferenceUtils.savePreferences(getApplicationContext(),"showCloudSection", false);
        //UPLOAD DATA
        initBtnCloud(Constant.sUploadState);
//        if (PreferenceUtils.readBoolPreferences(getApplicationContext(), Constant.AUTO_SYNC)) {
        uploadingData();
//        }
    }

    public void initBtnCloud(int state) {

        ImageView bnCloud = (ImageView) findViewById(R.id.WidgetActionbarBnCloud);
        if (!NetWorkUtils.isNetWorkAvailable(getApplicationContext()) || state == Constant.NETWORK_NOT_AVAILABLE) {
            bnCloud.setImageResource(R.drawable.cloud_offline);
        } else if (state == Constant.UPLOADING) {
            ImageOptions imageOptions = new ImageOptions.Builder()
                    .setIgnoreGif(false)
                    .setUseMemCache(true).build();
            x.image().bind(bnCloud, "assets://cloud_uploading.gif", imageOptions);
        } else if (state == Constant.UPLOADED) {
            bnCloud.setImageResource(R.drawable.cloud_uploaded);
        } else if (state == -1 && PreferenceUtils.readStrPreferences(getApplicationContext(), Constant.CURRENT_EMAIL) != null
                && PreferenceUtils.readStrPreferences(getApplicationContext(), Constant.CURRENT_PASSWORD) != null) {
            bnCloud.setImageResource(R.drawable.cloud_uploaded);
        }

        boolean isCloudAvailable = PreferenceUtils.readBoolPreferences(getApplicationContext(), "showCloudSection");
        if(isCloudAvailable) {
            bnCloud.setVisibility(View.VISIBLE);
        } else {
            bnCloud.setVisibility(View.GONE);
        }
    }

    public void uploadingData() {
        //TODO
        if (Constant.isRightTimeToUpload()) {
            x.task().run(new Runnable() {
                @Override
                public void run() {
                    String deviceName = PreferenceUtils.readStrPreferences(getApplicationContext(), "PreDeviceName");
                    if (PreferenceUtils.readStrPreferences(getApplicationContext(), deviceName + Constant.SN) != null &&
                            PreferenceUtils.readStrPreferences(getApplicationContext(), Constant.CURRENT_EMAIL) != null
                            && PreferenceUtils.readStrPreferences(getApplicationContext(), Constant.CURRENT_PASSWORD) != null
                            && NetWorkUtils.isNetWorkAvailable(getApplicationContext())
                            && Constant.userList != null) {
                        //post patient
                        UploadListUtils.postPatient(getApplicationContext(), baseHandler, db);
                    }
                }
            });
        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = x.getDb(((CEApplication) getApplicationContext()).getDaoConfig());
        setBehindView(savedInstanceState);

        initCloudSectionSettings();
        PreferenceUtils.savePreferences(getApplicationContext(),"showCloudSection", false);

        initSlidingMenu();
        initActionBar();
    }

    protected void initCloudSectionSettings() {

        // 离线 + 未连接过设备
        // 离线 + 旧APP连接过旧设备
        // 离线 + 新APP连接过旧设备
        // 离线 + 旧APP连接过新设备
        // 离线 + 新APP连接过新设备

        // 在线 + 连接的是旧设备
        // 在线 + 连接的是新设备

        String deviceName = PreferenceUtils.readStrPreferences(getApplicationContext(), "PreDeviceName");
        if(TextUtils.isEmpty(deviceName)) { // 离线 + 未连接过设备
            PreferenceUtils.savePreferences(getApplicationContext(),"showCloudSection", false);
            return;
        } else {
            if(deviceName.startsWith("CheckmeLE")) {
                PreferenceUtils.savePreferences(getApplicationContext(),"showCloudSection", false);
                return;
            }
        }

        String deviceInfoStr = PreferenceUtils.readStrPreferences(getApplicationContext(), "PreDeviceInfo");
        if(TextUtils.isEmpty(deviceInfoStr)) { // 旧APP连接过
            // 离线 + 旧APP连接过旧设备
            // 离线 + 旧APP连接过新设备
            PreferenceUtils.savePreferences(getApplicationContext(),"showCloudSection", true);
            return;
        } else { // 新（当前）APP连接过
            // 离线 + 新APP连接过旧设备
            // 离线 + 新APP连接过新设备
            // 在线 + 连接的是旧设备
            // 在线 + 连接的是新设备
            CheckmeDevice device = CheckmeDevice.decodeCheckmeDevice(deviceInfoStr);
            if(!device.isBranchCodeAvailable()) { //
                PreferenceUtils.savePreferences(getApplicationContext(),"showCloudSection", true);
            } else {
                String code = device.getBranchCode();
                if(!TextUtils.isEmpty(code) && "10010001".equals(code)) {
                    PreferenceUtils.savePreferences(getApplicationContext(),"showCloudSection", false);
                } else if(!TextUtils.isEmpty(code) && "10010002".equals(code)) {
                    PreferenceUtils.savePreferences(getApplicationContext(),"showCloudSection", false);
                } else {
                    PreferenceUtils.savePreferences(getApplicationContext(),"showCloudSection", false);
                }
            }
        }

        PreferenceUtils.savePreferences(getApplicationContext(),"showCloudSection", false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetWorkEvent(NetWorkEvent netWorkEvent) {
        LogUtils.d("NetWorkEvent====");
        initBtnCloud(Constant.sUploadState);
        if (netWorkEvent.isConnected()) {
            uploadToCloud();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBtnCloud(Constant.sUploadState);
    }

    public void setBehindView(Bundle savedInstanceState) {
        setBehindContentView(R.layout.menu_frame);
        FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
        mFrag = new LeftMenuFragment();
        ((LeftMenuFragment) mFrag).setmHandler(baseHandler);
        t.replace(R.id.menu_frame, mFrag);
        t.commit();
//        if (savedInstanceState == null) {
//            FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
//            mFrag = new LeftMenuFragment();
//            ((LeftMenuFragment) mFrag).setmHandler(baseHandler);
//            t.replace(R.id.menu_frame, mFrag);
//            t.commit();
//        } else {
//            mFrag = (ListFragment) this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
//        }

    }

    private void initSlidingMenu() {
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
    }

    public void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.widget_actionbar);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        // menu
        actionBar.setDisplayHomeAsUpEnabled(false);
        Button bnMenu = (Button) findViewById(R.id.WidgetActionbarBnMenu);
        bnMenu.setOnClickListener(this);
        // share
        // ButtonManager buttonManager = new ButtonManager(this, baseHandler);
        // buttonManager.SetBnShareFunc((Button)
        // findViewById(R.id.WidgetActionbarBnShare));
        Button bnShare = (Button) findViewById(R.id.WidgetActionbarBnShare);
        bnShare.setOnClickListener(this);
        // user
        Button bnUser = (Button) findViewById(R.id.WidgetActionbarBnUser);
        bnUser.setOnClickListener(this);
        //cloud
        ImageView bnCloud = (ImageView) findViewById(R.id.WidgetActionbarBnCloud);
        bnCloud.setOnClickListener(this);

        boolean isCloudAvailable = PreferenceUtils.readBoolPreferences(getApplicationContext(), "showCloudSection");
        if(isCloudAvailable) {
            bnCloud.setVisibility(View.VISIBLE);
        } else {
            bnCloud.setVisibility(View.GONE);
        }
    }

    public void reFreshActionBarBn(int fragmentNum, Boolean showUser, Boolean showShare) {

        Button bnShare = ((Button) findViewById(R.id.WidgetActionbarBnShare));
        Button bnUser = ((Button) findViewById(R.id.WidgetActionbarBnUser));
        Button bnLeft = ((Button) findViewById(R.id.WidgetActionbarBnMenu));
        ImageView bnCloud = (ImageView) findViewById(R.id.WidgetActionbarBnCloud);

        if (fragmentNum == 1) {// 第二页的时候右按钮显示分享
            bnLeft.setBackgroundResource(R.drawable.button_back);
            bnUser.setVisibility(View.GONE);
            if (showShare) {
                bnShare.setVisibility(View.GONE);
            } else {
                bnShare.setVisibility(View.GONE);
            }
        } else if (fragmentNum == 0) {// 第一页的时候右按钮显示用户或隐藏
            bnLeft.setBackgroundResource(R.drawable.button_left_menu);
            bnShare.setVisibility(View.GONE);
            if (Constant.CKM_MODE.equals(Constant.CKM_MODE_HOME)) {
                Logger.d(BaseActivity.class, "Constant.curUser.getUserInfo().getICO() == " + Constant.curUser.getUserInfo().getICO());
                if(Constant.curUser.getUserInfo().getICO() > 0) {
                    bnUser.setBackgroundResource(Constant.ICO_IMG[Constant.curUser.getUserInfo().getICO() - 1]);
                } else {
                    bnUser.setBackgroundResource(Constant.ICO_IMG[Constant.curUser.getUserInfo().getICO()]);
                }

            } else {
                SpotUserInfo info = Constant.curSpotUser.getUserInfo();
                int ico;
                if (info.getGender() == 0) {
                    ico = Constant.ICO_IMG[0];
                } else if (info.getGender() == 1) {
                    ico = Constant.ICO_IMG[3];
                } else {
                    ico = Constant.ICO_IMG[8];
                }
                bnUser.setBackgroundResource(ico);
            }
            if (showUser) {
                bnUser.setVisibility(View.VISIBLE);
            } else {
                bnUser.setVisibility(View.GONE);
            }

        }
        boolean isCloudAvailable = PreferenceUtils.readBoolPreferences(getApplicationContext(), "showCloudSection");
        if(isCloudAvailable) {
            bnCloud.setVisibility(View.VISIBLE);
        } else {
            bnCloud.setVisibility(View.GONE);
        }
        initBtnCloud(Constant.sUploadState);
    }

    public void reFreshActionBarBn(Boolean showUser, Boolean showShare) {

        Button bnShare = ((Button) findViewById(R.id.WidgetActionbarBnShare));
        Button bnUser = ((Button) findViewById(R.id.WidgetActionbarBnUser));
        Button bnLeft = ((Button) findViewById(R.id.WidgetActionbarBnMenu));
        ImageView bnCloud = (ImageView) findViewById(R.id.WidgetActionbarBnCloud);

        bnLeft.setBackgroundResource(R.drawable.button_left_menu);
        if (showShare) {
            bnShare.setVisibility(View.GONE);
        } else {
            bnShare.setVisibility(View.GONE);
        }
        if (Constant.CKM_MODE.equals(Constant.CKM_MODE_HOME)) {
            if(Constant.curUser.getUserInfo().getICO() > 0) {
                bnUser.setBackgroundResource(Constant.ICO_IMG[Constant.curUser.getUserInfo().getICO() - 1]);
            } else {
                bnUser.setBackgroundResource(Constant.ICO_IMG[Constant.curUser.getUserInfo().getICO()]);
            }

        } else {
            SpotUserInfo info = Constant.curSpotUser.getUserInfo();
            int ico;
            if (info.getGender() == 0) {
                ico = Constant.ICO_IMG[0];
            } else if (info.getGender() == 1) {
                ico = Constant.ICO_IMG[3];
            } else {
                ico = Constant.ICO_IMG[8];
            }
            bnUser.setBackgroundResource(ico);
        }
        if (showUser) {
            bnUser.setVisibility(View.VISIBLE);
        } else {
            bnUser.setVisibility(View.GONE);
        }
        boolean isCloudAvailable = PreferenceUtils.readBoolPreferences(getApplicationContext(), "showCloudSection");
        if(isCloudAvailable) {
            bnCloud.setVisibility(View.VISIBLE);
        } else {
            bnCloud.setVisibility(View.GONE);
        }
        initBtnCloud(Constant.sUploadState);
    }

    public void reFreshTitle(String titleStr) {
        TextView textTitle = (TextView) findViewById(R.id.WidgetActionbarTextTitle);
        textTitle.setText(titleStr);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggle();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.WidgetActionbarBnMenu:
                toggle();
                break;
            default:
                break;
        }
    }

    public void showSettingActivity() {
        MsgUtils.sendMsg(baseHandler, Constant.MSG_BNSETTINGS_CLICKED);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                ToastUtils.show(getApplicationContext(), Constant.getString(R.string.press_again_exit));
                exitTime = System.currentTimeMillis();
            } else {
                try {
                    Constant.cancelPost();
                    Constant.sUploadState = -1;
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}

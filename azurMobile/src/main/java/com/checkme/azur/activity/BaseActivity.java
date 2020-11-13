package com.checkme.azur.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.checkme.azur.element.CEApplication;
import com.checkme.azur.utils.MsgUtils;
import com.checkme.bluetooth.Logger;
import com.checkme.newazur.R;
import com.checkme.azur.element.Constant;
import com.checkme.azur.fragment.LeftMenuFragment;
import com.checkme.azur.measurement.SpotUser.SpotUserInfo;
import com.checkme.azur.tools.ToastUtils;

import org.xutils.DbManager;
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
                default:
                    break;
            }
        }

        ;
    };

    public boolean isTheSameIntent(int menuTag) {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = x.getDb(((CEApplication) getApplicationContext()).getDaoConfig());
        setBehindView(savedInstanceState);

        initSlidingMenu();
        initActionBar();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    public void setBehindView(Bundle savedInstanceState) {
        setBehindContentView(R.layout.menu_frame);
        FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
        mFrag = new LeftMenuFragment();
        ((LeftMenuFragment) mFrag).setmHandler(baseHandler);
        t.replace(R.id.menu_frame, mFrag);
        t.commit();
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

        Button bnShare = (Button) findViewById(R.id.WidgetActionbarBnShare);
        bnShare.setOnClickListener(this);
        // user
        Button bnUser = (Button) findViewById(R.id.WidgetActionbarBnUser);
        bnUser.setOnClickListener(this);
    }

    public void reFreshActionBarBn(int fragmentNum, Boolean showUser, Boolean showShare) {

        Button bnShare = ((Button) findViewById(R.id.WidgetActionbarBnShare));
        Button bnUser = ((Button) findViewById(R.id.WidgetActionbarBnUser));
        Button bnLeft = ((Button) findViewById(R.id.WidgetActionbarBnMenu));

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
    }

    public void reFreshActionBarBn(Boolean showUser, Boolean showShare) {

        Button bnShare = ((Button) findViewById(R.id.WidgetActionbarBnShare));
        Button bnUser = ((Button) findViewById(R.id.WidgetActionbarBnUser));
        Button bnLeft = ((Button) findViewById(R.id.WidgetActionbarBnMenu));

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

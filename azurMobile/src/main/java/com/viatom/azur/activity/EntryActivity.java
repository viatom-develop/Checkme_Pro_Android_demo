package com.viatom.azur.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.viatom.azur.element.Constant;
import com.viatom.azur.utils.LogUtils;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class EntryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d("EntryActivity onNewIntent");
        Intent intent = getIntent();
        int modeType = intent.getIntExtra("modeType", 0);
        if(modeType == 1) {
            comeIntoDailyCheck();
        } else if(modeType == 2){
            comeIntoSpotCheck();
        } else {
            int shareType = intent.getIntExtra("shareType", Constant.SHARE_TYPE_CLOUD);
            if(shareType == Constant.SHARE_TYPE_CLOUD) {
                Intent shareIntent = new Intent();
                shareIntent.setClass(EntryActivity.this, SettingsActivity.class);
                shareIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(shareIntent);
                LogUtils.d("EntryActivity onCreate");
            } else {
                finish();
            }
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtils.d("EntryActivity onNewIntent");
        int shareType = intent.getIntExtra("shareType", 0);
        if(shareType == Constant.SHARE_TYPE_CLOUD) {
            Intent shareIntent = new Intent();
            shareIntent.setClass(EntryActivity.this, SettingsActivity.class);
            shareIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(shareIntent);
        } else {
            finish();
            LogUtils.d("EntryActivity onNewIntent finish()");
        }
    }

    private void comeIntoDailyCheck() {
        Constant.CKM_MODE = Constant.CKM_MODE_HOME;
        Intent intent = new Intent();
        intent.setClass(EntryActivity.this, DailyCheck.class);
        startActivity(intent);
    }

    private void comeIntoSpotCheck() {
        Constant.CKM_MODE = Constant.CKM_MODE_HOSPITAL;
        Intent intent = new Intent();
        intent.setClass(EntryActivity.this, SpotCheck.class);
        startActivity(intent);
    }
}

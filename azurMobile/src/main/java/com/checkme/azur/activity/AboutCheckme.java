package com.checkme.azur.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.checkme.azur.element.UpdatePatch;
import com.checkme.azur.tools.PreferenceUtils;
import com.checkme.azur.utils.JsonRequest;
import com.checkme.azur.utils.JsonUtils;
import com.checkme.azur.utils.StringUtils;
import com.checkme.newazur.R;
import com.checkme.azur.bluetooth.GetInfoThreadListener;
import com.checkme.azur.element.CheckmeDevice;
import com.checkme.azur.element.Constant;
import com.checkme.azur.fragment.CheckmeInfoFragment;
import com.checkme.azur.fragment.CheckmeUpdateFragment;
import com.checkme.azur.tools.NetWorkUtils;
import com.checkme.azur.tools.NoInfoViewUtils;
import com.checkme.azur.tools.ToastUtils;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.utils.MsgUtils;
import com.checkme.azur.widget.JProgressDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AboutCheckme extends BaseActivity implements OnClickListener, GetInfoThreadListener {

    RequestQueue queue;
    private CheckmeDevice checkmeInfo;
    private String[] languageList;
    private UpdatePatch updatePatch;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Constant.CMD_TYPE_CKM_INFO:
                    checkmeInfo = CheckmeDevice.decodeCheckmeDevice((String) msg.obj);
                    initInfoFragment(checkmeInfo);
                    break;
                case Constant.MSG_SHOW_UPDATE_WARNING:
                    showUpdateWarning();
                    break;
                case Constant.MSG_SHOW_UPDATE_SUCCESS:
                    showUpdateSuccess();
                    break;
                case Constant.MSG_SHOW_UPDATE_FAILED:
                    showUpdateFailed();
                    break;
                case Constant.MSG_SHOW_UNNECESSARY_UPDATE:
                    showUnnecessaryUpdate();
                    break;
                case Constant.MSG_SHOW_CANT_UPDATE_IN_OFFLINE:
                    showCantUpdateInOffline();
                    break;
                case Constant.MSG_SHOW_SWITCH_LANGUAGE:
                    showSwitchLanguage();
                    break;
                case Constant.MSG_BNUPDATE_CLICKED:
                    onBnUpdateClicked();
                    break;
                case Constant.MSG_BACK_TO_LAST_FRAG:
                    backToLastFragment();
                    break;
                case Constant.MSG_SHOW_CHANGE_LANGUAGE_SUCCESS:
                    showChangeLanguageSuccess();
                    break;
                default:
                    break;
            }

        }

        ;
    };

    @Override
    public boolean isTheSameIntent(int menuTag) {
        return (menuTag == Constant.MSG_BNABOUT_CKM_CLICKED);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_checkme);
        queue = Volley.newRequestQueue(this);
        initUI();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    public void initUI() {
        reFreshTitle(Constant.getString(R.string.title_device));
        reFreshActionBarBn(0, false, false);
        //有蓝牙，获取信息，无蓝牙显示空
        if (Constant.btConnectFlag) {
            Constant.binder.interfaceGetInfo(1000, this);
        } else {
            NoInfoViewUtils.showNoInfoView(AboutCheckme.this, findViewById(R.id.ImgNoInfo));
        }
    }

    @Override
    public void onGetInfoSuccess(String checkmeInfo) {
        // TODO Auto-generated method stub
        MsgUtils.sendMsg(handler, checkmeInfo, Constant.CMD_TYPE_CKM_INFO);
    }

    @Override
    public void onGetInfoFailed(byte errCode) {
        // TODO Auto-generated method stub
        //后续处理
//        MsgUtils.sendMsg(handler, checkmeInfo, Constant.CMD_TYPE_CKM_INFO);
        handler.post(new Runnable() {
            @Override
            public void run() {
                String deviceInfo = PreferenceUtils.readStrPreferences(getApplicationContext(), "PreDeviceInfo");
                if(Constant.btConnectFlag && !TextUtils.isEmpty(deviceInfo)) {
                    MsgUtils.sendMsg(handler, deviceInfo, Constant.CMD_TYPE_CKM_INFO);
                } else {
                    NoInfoViewUtils.showNoInfoView(AboutCheckme.this, findViewById(R.id.ImgNoInfo));
                }


            }
        });

    }

    /**
     * 初始化checkme信息frag
     * 获取信息成功后调用
     *
     * @param checkmeInfo
     */
    private void initInfoFragment(CheckmeDevice checkmeInfo) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        CheckmeInfoFragment fragment = new CheckmeInfoFragment();
        fragment.setArguments(checkmeInfo,handler);
        fragmentTransaction.add(R.id.AboutCheckmeFrameMain, fragment);
        fragmentTransaction.commit();
    }

    /**
     * 初始化升级frag
     * 点击升级并选择语言后调用
     */
    private void initUpdateFragment(CheckmeDevice checkmeInfo, String wantLanguage,UpdatePatch updatePatch) {
        if (checkmeInfo == null || wantLanguage == null) {
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.push_left_in,
                R.anim.push_left_out, R.anim.push_right_in, R.anim.push_right_out);
        CheckmeUpdateFragment fragment = new CheckmeUpdateFragment();
        fragment.setArguments(checkmeInfo, wantLanguage, handler,updatePatch);
        fragmentTransaction.replace(R.id.AboutCheckmeFrameMain, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /**
     * 获取可升级语言列表
     */
    private void getLanguageList() {
        if (!NetWorkUtils.isNetWorkAvailable(AboutCheckme.this)) {
            JProgressDialog.cancel();
            ToastUtils.show(AboutCheckme.this, Constant.getString(
                    R.string.network_not_available));
            LogUtils.d("网络不可用");
            return;
        }
        if (checkmeInfo == null) {
            LogUtils.d("主机信息为空，无法获取升级包");
            return;
        }
        LogUtils.d(checkmeInfo.getRegion() + "***************getRegion");
        LogUtils.d(checkmeInfo.getModel() + "***************getModel");
        LogUtils.d(checkmeInfo.getHardware() + "***************getHardware");
        LogUtils.d(checkmeInfo.getSoftware() + "***************getSoftware");


        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> headers =new HashMap<String, String>();
                headers.put("Content-Type", "application/json+fhir");
                try {
                    String deviceInfo = JsonUtils.makeDeviceJson(checkmeInfo).toString();
                    JsonRequest request = new JsonRequest(Request.Method.POST,
                            Constant.UPDATE_ADDRESS, headers, deviceInfo,
                            new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                updatePatch = new UpdatePatch(response);
                            } catch (JSONException e) {
                                LogUtils.d(e.toString());
                            }
                            if (StringUtils.isUpdateAvailable(updatePatch.getVersion()
                                    , makeVersion(checkmeInfo.getSoftware()))) {
                                List<String> keyList=updatePatch.getKeyList();
                                languageList = new String[keyList.size()];
                                for (int i=0;i<languageList.length;i++){
                                    languageList[i]= keyList.get(i);
                                }
                                MsgUtils.sendMsg(handler, Constant.MSG_SHOW_UPDATE_WARNING);
                            } else {
                                MsgUtils.sendMsg(handler, Constant.MSG_SHOW_UNNECESSARY_UPDATE);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            MsgUtils.sendMsg(handler, Constant.MSG_SHOW_UPDATE_FAILED);
                        }
                    });
                    queue.add(request);
                } catch (JSONException e) {
                    LogUtils.d(e.toString());
                }
//                try {
//                    String url = Constant.UPDATE_ADDRESS + "123";
//                    JSONObject jsonObject = PostUtils.doPost(url, PostInfoMaker.makeGetLanguageListInfo(checkmeInfo));
//                    if (jsonObject == null) {
//                        //获取错误
//                        MsgUtils.sendMsg(handler, Constant.MSG_SHOW_UPDATE_FAILED);
//                    } else {
//                        LogUtils.d(jsonObject.toString());
//                        updatePatch = new UpdatePatch(jsonObject);
//                        if (StringUtils.isUpdateAvailable(updatePatch.getVersion()
//                                , makeVersion(checkmeInfo.getSoftware()))) {
//                            List<String> keyList=updatePatch.getKeyList();
//                            languageList = new String[keyList.size()];
//                            for (int i=0;i<languageList.length;i++){
//                                languageList[i]= keyList.get(i);
//                            }
//                            MsgUtils.sendMsg(handler, Constant.MSG_SHOW_UPDATE_WARNING);
//                        } else {
//                            MsgUtils.sendMsg(handler, Constant.MSG_SHOW_UNNECESSARY_UPDATE);
//                        }
//                    }
//                } catch (Exception e) {
//                    // TODO: handle exception
//                    e.printStackTrace();
//                    LogUtils.d("服务器交互异常:" + e);
//                    MsgUtils.sendMsg(handler, Constant.MSG_SHOW_UPDATE_FAILED);
//                }
            }
        }).start();
    }

    /**
     * 生成版本号
     *
     * @param software
     * @return
     */
    private String makeVersion(int software) {
        if (software <= 0) {
            return "--";
        }
        String version = new String();
        version += software / 10000 + ".";
        version += (software % 10000) / 100 + ".";
        version += software % 100;
        LogUtils.d(version);
        return version;
    }

    /**
     * 解析从服务器获取的语言列表
     *
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    private String[] decodeLanguageList(JSONObject jsonObject) throws JSONException {

        if (jsonObject == null) {
            //获取信息错误
            return null;
        }

        JSONArray jsonArray = jsonObject.getJSONArray("LanguageList");
        String[] outList = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            outList[i] = jsonArray.getString(i);
        }
        return outList;
    }

    /**
     * 显示语言列表
     *
     * @param languages
     */
    private void showLanguageList(String[] languages) {
        if (languages == null || languages.length == 0) {
            return;
        }
        new AlertDialog.Builder(AboutCheckme.this)
                .setTitle(Constant.getString(R.string.select_language))
                .setItems(languages, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        reFreshActionBarBn(1, false, false);
                        reFreshTitle(Constant.getString(R.string.update));
                        initUpdateFragment(checkmeInfo, languageList[arg1],updatePatch );
                    }
                })
                .setNegativeButton(Constant.getString(R.string.cancel), null).show();
    }

    /**
     * 显示下载警告
     */
    private void showUpdateWarning() {
        JProgressDialog.cancel();
        new AlertDialog.Builder(AboutCheckme.this)
                .setTitle(Constant.getString(R.string.warning))
                .setMessage(Constant.getString(R.string.erase_datas))
                .setNegativeButton(Constant.getString(R.string.cancel), null)
                .setNeutralButton(Constant.getString(R.string.update), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        showLanguageList(languageList);
                    }
                })
                .show();
    }

    /**
     * 当不需要升级时，显示可以切换语言
     */
    private void showSwitchLanguage() {
        JProgressDialog.cancel();
        new AlertDialog.Builder(AboutCheckme.this)
                .setTitle(Constant.getString(R.string.version_up_to_date))
                .setNegativeButton(Constant.getString(R.string.cancel), null)
                .setNeutralButton(Constant.getString(R.string.change_language)
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // TODO Auto-generated method stub
//				showUpdateWarning();
                                showLanguageList(languageList);
                            }
                        }).show();
    }

    /**
     * 不需要升级时显示
     */
    private void showUnnecessaryUpdate() {
        JProgressDialog.cancel();
        ToastUtils.show(AboutCheckme.this, Constant.getString(R.string.version_up_to_date));
    }

    /**
     * 显示无法在离线模式下下载
     */
    private void showCantUpdateInOffline() {
        ToastUtils.show(AboutCheckme.this, Constant.getString(R.string.update_in_offline));
    }

    /**
     * 显示下载失败
     */
    private void showUpdateFailed() {
        JProgressDialog.cancel();
        ToastUtils.show(AboutCheckme.this, Constant.getString(R.string.update_failed));
    }

    /**
     * 显示升级成功
     */
    private void showUpdateSuccess() {
        new AlertDialog.Builder(AboutCheckme.this)
                .setTitle(Constant.getString(R.string.notice))
                .setMessage(Constant.getString(R.string.update_restart))
                .setPositiveButton(Constant.getString(R.string.restart)
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // TODO Auto-generated method stub
                                restartApplication();
                            }
                        })
                .setCancelable(false)
                .show();
    }

    /**
     * 显示升级语言包成功
     */
    private void showChangeLanguageSuccess() {
        new AlertDialog.Builder(AboutCheckme.this)
                .setTitle(Constant.getString(R.string.notice))
                .setMessage(Constant.getString(R.string.update_successfully))
                .setPositiveButton(Constant.getString(R.string.yes)
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // TODO Auto-generated method stub
                                backToLastFragment();
                            }
                        })
                .setCancelable(false)
                .show();
    }

    /**
     * 返回到上一个frag
     */
    private void backToLastFragment() {
        getSupportFragmentManager().popBackStackImmediate();
        reFreshActionBarBn(0, false, false);
        reFreshTitle(Constant.getString(R.string.title_device));
    }

    /**
     * 重启APP
     */
    private void restartApplication() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * 升级按键处理函数
     */
    private void onBnUpdateClicked() {
        if (Constant.btConnectFlag && checkmeInfo != null) {
            JProgressDialog.show(AboutCheckme.this);
            getLanguageList();
        } else {
            LogUtils.d("未连接蓝牙或Checkme信息为空");
            showUpdateFailed();
        }
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch (view.getId()) {
            case R.id.WidgetActionbarBnMenu:
                if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                    //在升级界面
                    new AlertDialog.Builder(AboutCheckme.this)
                            .setTitle(Constant.getString(R.string.warning))
                            .setMessage(Constant.getString(R.string.stop_update))
                            .setNegativeButton(Constant.getString(R.string.cancel), null)
                            .setNeutralButton(Constant.getString(R.string.yes)
                                    , new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            // TODO Auto-generated method stub
                                            getSupportFragmentManager().popBackStackImmediate();
                                            reFreshActionBarBn(0, false, false);
                                            reFreshTitle(Constant.getString(R.string.title_device));
                                        }
                                    }).show();
                } else {
                    //在首页
                    super.onClick(view);
                }

                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                //在升级界面
                new AlertDialog.Builder(AboutCheckme.this)
                        .setTitle(Constant.getString(R.string.warning))
                        .setMessage(Constant.getString(R.string.stop_update))
                        .setNegativeButton(Constant.getString(R.string.cancel), null)
                        .setNeutralButton(Constant.getString(R.string.yes)
                                , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        // TODO Auto-generated method stub
                                        getSupportFragmentManager().popBackStackImmediate();
                                        reFreshActionBarBn(0, false, false);
                                        reFreshTitle(Constant.getString(R.string.title_device));
                                    }
                                }).show();
            } else {
                //在首页
                return super.onKeyDown(keyCode, event);
            }
        }
        return false;
    }

}

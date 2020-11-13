package com.viatom.azur.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.viatom.azur.EventBusEvent.AgreeClickedEvent;
import com.viatom.azur.EventBusEvent.FlushIvEvent;
import com.viatom.azur.element.CEApplication;
import com.viatom.azur.measurement.User;
import com.viatom.azur.utils.FileUtils;
import com.viatom.azur.utils.InternetUtils;
import com.viatom.azur.widget.JProgressDialog;
import com.viatom.bluetooth.Logger;
import com.viatom.newazur.BuildConfig;
import com.viatom.newazur.R;
import com.viatom.azur.element.Constant;
import com.viatom.azur.tools.PreferenceUtils;
import com.viatom.azur.utils.LogUtils;
import com.viatom.azur.utils.MsgUtils;
import com.viatom.azur.widget.SegmentedRadioGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.util.KeyValue;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.http.body.UrlEncodedParamsBody;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

public class SettingsFragment extends Fragment implements
        OnCheckedChangeListener, OnClickListener
//		,CompoundButton.OnCheckedChangeListener
{
    private View rootView;
    private Handler callHandler;
    private RelativeLayout re;

    private String email;
    private String password;


    private LinearLayout lin_email;
    private EditText et_email;
    private LinearLayout lin_password;
    private EditText et_password;
    private Button btn_sign_in;
    private LinearLayout lin_creat_account;
    private TextView tv_creat_account;
    private TextView tv_delete_account;

    private LinearLayout lin_name;
    private TextView tv_user_name;
    //	private RelativeLayout rl_auto_sync;
//	private Switch st_auto_sync;
//	private Button btn_manual_sync;
    private Button btn_sign_out;
    private PolicyDialogFragment policyDialogFragment;

    private Button btn_access_cloud;
    private Context mContext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
        this.mContext = null;
    }

    public SettingsFragment() {
        super();
        // TODO Auto-generated constructor stub
        LogUtils.d("调用settingFrag默认构造函数");
    }

    public void setArguments(Handler callHandler) {
        this.callHandler = callHandler;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        rootView = inflater.inflate(R.layout.fragment_settings_list, container, false);
        initViews();
        initSegmentsListener();
        clearAlldate();
        initUI();
        initSignUp();
        initListener();

        return rootView;
    }

    private void initViews() {
        lin_email = (LinearLayout) rootView.findViewById(R.id.lin_email);
        et_email = (EditText) rootView.findViewById(R.id.et_email);
        lin_password = (LinearLayout) rootView.findViewById(R.id.lin_password);
        et_password = (EditText) rootView.findViewById(R.id.et_password);
        btn_sign_in = (Button) rootView.findViewById(R.id.btn_sign_in);
        lin_creat_account = (LinearLayout) rootView.findViewById(R.id.lin_creat_account);
        tv_creat_account = (TextView) rootView.findViewById(R.id.tv_creat_account);
        tv_delete_account = (TextView) rootView.findViewById(R.id.tv_delete_account);
        lin_name = (LinearLayout) rootView.findViewById(R.id.lin_name);
        tv_user_name = (TextView) rootView.findViewById(R.id.tv_user_name);
//		rl_auto_sync= (RelativeLayout) rootView.findViewById(R.id.rl_auto_sync);
//		st_auto_sync= (Switch) rootView.findViewById(R.id.st_auto_sync);
//		btn_manual_sync= (Button) rootView.findViewById(R.id.btn_manual_sync);
        btn_sign_out = (Button) rootView.findViewById(R.id.btn_sign_out);
        btn_access_cloud = (Button) rootView.findViewById(R.id.btn_access_cloud);
    }

    private void initListener() {
        btn_sign_in.setOnClickListener(this);
        btn_access_cloud.setOnClickListener(this);
//		btn_manual_sync.setOnClickListener(this);
        btn_sign_out.setOnClickListener(this);
        tv_creat_account.setOnClickListener(this);
        tv_delete_account.setOnClickListener(this);
//		st_auto_sync.setOnCheckedChangeListener(this);
    }

    private void initSignUp() {
        LinearLayout lin_cloud = rootView.findViewById(R.id.lin_cloud);
        boolean isCloudAvailable = PreferenceUtils.readBoolPreferences(Constant.mContext, "showCloudSection");
        if(isCloudAvailable) {
            lin_cloud.setVisibility(View.VISIBLE);
        } else {
            lin_cloud.setVisibility(View.GONE);
        }

        if (PreferenceUtils.readStrPreferences(mContext.getApplicationContext(), Constant.CURRENT_EMAIL) != null
                && PreferenceUtils.readStrPreferences(mContext.getApplicationContext(), Constant.CURRENT_PASSWORD) != null) {
            showSignOut();
        } else {
            showSignIn();
        }
    }

    public boolean validate() {
        boolean valid = true;

        String email = et_email.getText().toString();
        String password = et_password.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            et_email.setError(getResources().getString(R.string.error_invalid_email));
            valid = false;
        } else {
            et_email.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            et_password.setError(getResources().getString(R.string.error_invalid_password));
            valid = false;
        } else {
            et_password.setError(null);
        }

        return valid;
    }

    private void showSignIn() {
        lin_email.setVisibility(View.VISIBLE);
        lin_password.setVisibility(View.VISIBLE);
        btn_sign_in.setVisibility(View.VISIBLE);
        lin_creat_account.setVisibility(View.VISIBLE);

        lin_name.setVisibility(View.GONE);
        tv_delete_account.setVisibility(View.GONE);
//		rl_auto_sync.setVisibility(View.GONE);
//		btn_manual_sync.setVisibility(View.GONE);
        btn_sign_out.setVisibility(View.GONE);
    }

    private void showSignOut() {
        lin_email.setVisibility(View.GONE);
        lin_password.setVisibility(View.GONE);
        btn_sign_in.setVisibility(View.GONE);
        lin_creat_account.setVisibility(View.GONE);

        lin_name.setVisibility(View.VISIBLE);
        tv_delete_account.setVisibility(View.VISIBLE);
//		rl_auto_sync.setVisibility(View.VISIBLE);
//		btn_manual_sync.setVisibility(View.VISIBLE);
        btn_sign_out.setVisibility(View.VISIBLE);
        if (PreferenceUtils.readStrPreferences(mContext.getApplicationContext(), Constant.CURRENT_USER_NAME) != null)
            setUserPara(PreferenceUtils.readStrPreferences(mContext.getApplicationContext(), Constant.CURRENT_USER_NAME));
//		if (PreferenceUtils.readBoolPreferences(getActivity().getApplicationContext(), Constant.AUTO_SYNC)) {
//			st_auto_sync.setChecked(true);
//			btn_manual_sync.setClickable(false);
//			btn_manual_sync.setBackgroundColor(getResources().getColor(R.color.Gray));
//		} else {
//			st_auto_sync.setChecked(false);
//			btn_manual_sync.setClickable(true);
//			btn_manual_sync.setBackgroundColor(getResources().getColor(R.color.default_bkg));
//		}
    }

    private void setUserPara(String name) {
        tv_user_name.setText(name);
    }

    public void clearAlldate() {
        re = (RelativeLayout) rootView.findViewById(R.id.Settingsclear);
        re.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                MsgUtils.sendMsg(callHandler, Constant.MSG_GOTO_DIALOG);
            }

        });
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    protected void initUI() {
        initUnitSegment(PreferenceUtils.readIntPreferences(mContext, "UNIT"));
        initThermometerSegment(PreferenceUtils.readIntPreferences(mContext, "THERMOMETER"));

        initDeviceView();
    }

    /**
     * 初始化各选择租监测函数
     */
    protected void initSegmentsListener() {
        //单位
        SegmentedRadioGroup unitGroup = (SegmentedRadioGroup) rootView
                .findViewById(R.id.SettingsListUnitSegGroup);
        unitGroup.setOnCheckedChangeListener(this);

        //温度
        SegmentedRadioGroup thermometerGroup = (SegmentedRadioGroup) rootView
                .findViewById(R.id.SettingsListThermometerSegGroup);
        thermometerGroup.setOnCheckedChangeListener(this);
    }

    /**
     * 初始化单位选择组
     *
     * @param value
     */
    protected void initUnitSegment(int value) {
        SegmentedRadioGroup group = (SegmentedRadioGroup) rootView
                .findViewById(R.id.SettingsListUnitSegGroup);
        if (value == Constant.UNIT_METRIC) {
            group.check(R.id.SettingsListBnMetric);
        } else if (value == Constant.UNIT_BRITISH) {
            group.check(R.id.SettingsListBnBritsh);
        }
    }

    /**
     * 初始化温度单位选择组
     *
     * @param value
     */
    protected void initThermometerSegment(int value) {
        SegmentedRadioGroup group = (SegmentedRadioGroup) rootView
                .findViewById(R.id.SettingsListThermometerSegGroup);
        if (value == Constant.THERMOMETER_C) {
            group.check(R.id.SettingsListBnC);
        } else if (value == Constant.THERMOMETER_F) {
            group.check(R.id.SettingsListBnF);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // TODO Auto-generated method stub
        //通用单位
        if (group == rootView.findViewById(R.id.SettingsListUnitSegGroup)) {
            if (checkedId == R.id.SettingsListBnMetric) {
                Constant.unit = Constant.UNIT_METRIC;
                PreferenceUtils.savePreferences(mContext
                        , "UNIT", Constant.UNIT_METRIC);
            } else if (checkedId == R.id.SettingsListBnBritsh) {
                Constant.unit = Constant.UNIT_BRITISH;
                PreferenceUtils.savePreferences(mContext
                        , "UNIT", Constant.UNIT_BRITISH);
            }
        }
        //温度单位
        else if (group == rootView.findViewById(R.id.SettingsListThermometerSegGroup)) {
            if (checkedId == R.id.SettingsListBnC) {
                Constant.thermometerUnit = Constant.THERMOMETER_C;
                PreferenceUtils.savePreferences(mContext
                        , "THERMOMETER", Constant.THERMOMETER_C);
            } else if (checkedId == R.id.SettingsListBnF) {
                Constant.thermometerUnit = Constant.THERMOMETER_F;
                PreferenceUtils.savePreferences(mContext
                        , "THERMOMETER", Constant.THERMOMETER_F);
            }
        }
    }

    /**
     * 初始化设备信息框
     */
    private void initDeviceView() {
        View deviceView = rootView.findViewById(R.id.SettingsListReDevice);
        ImageView ivDevice = (ImageView) rootView.findViewById(R.id.SettingsListImgDevice);
        TextView tvDevice = (TextView) rootView.findViewById(R.id.SettingsListTextDeviceName);
        String deviceName = PreferenceUtils.readStrPreferences(mContext.getApplicationContext(), "PreDeviceName");
        Logger.d("SettingsFragment", "initDeviceView deviceName == " + deviceName);
        if(!TextUtils.isEmpty(deviceName) && deviceName.startsWith("Checkme Lite")) {
            ivDevice.setImageResource(R.drawable.checkmel);
            tvDevice.setText(deviceName);
        } else {
            ivDevice.setImageResource(R.drawable.checkme);
            tvDevice.setText(deviceName);
        }

        deviceView.setOnClickListener(this);
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        switch (arg0.getId()) {
            case R.id.SettingsListReDevice:
                MsgUtils.sendMsg(callHandler, Constant.MSG_GOTO_CHOOSE_DEVICE);
                break;
            case R.id.btn_sign_in:
                policyDialogFragment = new PolicyDialogFragment();
                policyDialogFragment.show(getActivity().getFragmentManager(), "PolicyDialogFragment");
                policyDialogFragment.setCancelable(false);
//                signIn();
                break;
            case R.id.btn_sign_out:
                signOut();
                break;
//			case R.id.btn_manual_sync:
//				manualSync();
//				break;
            case R.id.btn_access_cloud:
                accessCloud();
                break;
            case R.id.tv_creat_account:
                createAccount();
                break;
            case R.id.tv_delete_account:
                jump_delete_account();
            default:
                break;
        }
    }


    //	private void manualSync() {
//		//TODO uploadingDataEvent
//		MsgUtils.sendMsg(callHandler, Constant.MSG_UPLOAD_DAT);
//	}
    private void onLoginFailed() {
        Toast.makeText(mContext.getApplicationContext(), R.string.login_failed, Toast.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAgreeClickedEvent(AgreeClickedEvent event) {
        LogUtils.d("eventbus received");
        if (policyDialogFragment != null && policyDialogFragment.isVisible()) {
            policyDialogFragment.dismiss();
        }
//
        if (event.isClicked()) {
            signIn();
        }
    }

    private void signIn() {



        if (!validate()) {
            onLoginFailed();
            return;
        }



        email = et_email.getText().toString();
        password = et_password.getText().toString();
        LogUtils.d(email);
        LogUtils.d(password);
        RequestParams requestParams = new RequestParams(Constant.LOGIN_URL);
        List<KeyValue> params = new ArrayList<>();
        params.add(new KeyValue("email", email));
        params.add(new KeyValue("password", password));
        try {
            requestParams.setRequestBody(new UrlEncodedParamsBody(params, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestParams.setConnectTimeout(Constant.CONNECT_TIMEOUT);
        /** 判断https证书是否成功验证 */
        /*SSLContext sslContext = InternetUtils.getSSLContext(mContext.getApplicationContext());
        if (null == sslContext) {
            if (BuildConfig.DEBUG)
                LogUtils.d("Error:Can't Get SSLContext!");
            return;
        }
        requestParams.setSslSocketFactory(sslContext.getSocketFactory()); //绑定SSL证书(https请求)*/

        x.http().request(HttpMethod.POST, requestParams, new Callback.ProgressCallback<JSONObject>() {
            @Override
            public void onWaiting() {
                LogUtils.d("onWaiting");
            }

            @Override
            public void onStarted() {
                LogUtils.d("onStarted");
                JProgressDialog.show(mContext);
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                LogUtils.d("onLoading");

            }

            @Override
            public void onSuccess(JSONObject result) {
                LogUtils.d("onSuccess");
                LogUtils.d(result.toString());
                JProgressDialog.cancel();
                /*CEApplication policy_flag =(CEApplication) getActivity().getApplication();
                boolean policy_flag_key = policy_flag.getPolicyFlag();*/

                //MsgUtils.sendMsg(callHandler, Constant.MSG_SHOW_POLICY_DIALOG);}

                dealResult(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtils.d("onError");
                LogUtils.d(ex.toString());
                JProgressDialog.cancel();
                onLoginFailed();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtils.d("cancelled");
            }

            @Override
            public void onFinished() {
                LogUtils.d("finished");
            }
        });
    }

    private void dealResult(JSONObject result) {
        if (password != null && !password.equals(""))
            PreferenceUtils.savePreferences(mContext.getApplicationContext(), Constant.CURRENT_PASSWORD, password);
        try {
            final String name = result.getString(Constant.CURRENT_USER_NAME);
            final String email = result.getString(Constant.CURRENT_EMAIL);
            x.task().post(new Runnable() {
                @Override
                public void run() {
                    PreferenceUtils.savePreferences(mContext.getApplicationContext(), Constant.CURRENT_USER_NAME, name);
                    PreferenceUtils.savePreferences(mContext.getApplicationContext(), Constant.CURRENT_EMAIL, email);
                }
            });
            Constant.sUploadState = Constant.UPLOADED;
            MsgUtils.sendMsg(callHandler, Constant.MSG_UPLOAD_TO_CLOUD);
            showSignOut();
            setUserPara(name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void signOut() {
        CEApplication policy_flag =(CEApplication) getActivity().getApplication();
        policy_flag.setPolicyFlag(false);
        // TODO clean all setting
        PreferenceUtils.removeStrPreferences(mContext.getApplicationContext(), Constant.CURRENT_EMAIL);
        PreferenceUtils.removeStrPreferences(mContext.getApplicationContext(), Constant.CURRENT_PASSWORD);
        PreferenceUtils.removeStrPreferences(mContext.getApplicationContext(), Constant.CURRENT_USER_NAME);
        File[] files = new File(Constant.root, "CheckmeMobile/").listFiles();
        if (files != null && files.length != 0) {
            for (File file :
                    files) {
                if (file.isDirectory() && file.listFiles().length != 0 && file.getName().contains("Checkme")) {
                    File userFile = new File(Constant.root, "CheckmeMobile/" + file.getName() + "/usr.dat");
                    if (userFile.exists() && userFile.length() != 0) {
                        String fileName = file.getName();
                        //UserID
                        for (int i = 1; i < 13; i++) {
                            PreferenceUtils.removeStrPreferences(mContext.getApplicationContext(), fileName + i);
                        }
                    }
                }

            }
        }
//        if (Constant.userList != null && Constant.userList.length != 0) {
//            String deviceName = PreferenceUtils.readStrPreferences(mContext.getApplicationContext(), "PreDeviceName");
//            for (User user : Constant.userList) {
//                PreferenceUtils.removeStrPreferences(mContext.getApplicationContext(), deviceName + user.getUserInfo().getID());
//            }
//        }
        showSignIn();
        Constant.sUploadState = Constant.NETWORK_NOT_AVAILABLE;
        EventBus.getDefault().post(new FlushIvEvent(Constant.NETWORK_NOT_AVAILABLE));
    }

    private void accessCloud() {
        Uri uri = Uri.parse(Constant.ACCESS_URL);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.choose_a_browser)));
        }
    }

    private void createAccount() {
        Uri uri = Uri.parse(Constant.CLOUD_URL);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.choose_a_browser)));
        }
    }

    private void jump_delete_account() {
    Uri uri = Uri.parse(Constant.DELETE_URL);
    Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            if(intent.resolveActivity(mContext.getPackageManager())!=null) {
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.choose_a_browser)));
        }
    }


//    @Override
//    public void onAgreeClicked(boolean b) {
//        if (policyDialogFragment != null && policyDialogFragment.isVisible()) {
//            policyDialogFragment.dismiss();
//        }
//
//        if(b){
//            signIn();
//        }
//    }


//	@Override
//	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//		if (isChecked) {
//			PreferenceUtils.savePreferences(getActivity().getApplicationContext(), Constant.AUTO_SYNC, true);
//			btn_manual_sync.setClickable(false);
//			btn_manual_sync.setBackgroundColor(getResources().getColor(R.color.Gray));
//			MsgUtils.sendMsg(callHandler, Constant.MSG_UPLOAD_DAT);
//			//TODO uploadingDataEvent
//		} else {
//			PreferenceUtils.savePreferences(getActivity().getApplicationContext(), Constant.AUTO_SYNC, false);
//			btn_manual_sync.setClickable(true);
//			btn_manual_sync.setBackgroundColor(getResources().getColor(R.color.default_bkg));
//		}
//	}
}

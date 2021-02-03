package com.checkme.azur.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.checkme.bluetooth.Logger;
import com.checkme.newazur.R;
import com.checkme.azur.element.Constant;
import com.checkme.azur.tools.PreferenceUtils;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.utils.MsgUtils;
import com.checkme.azur.widget.SegmentedRadioGroup;

import org.greenrobot.eventbus.EventBus;

public class SettingsFragment extends Fragment implements
        OnCheckedChangeListener, OnClickListener
//		,CompoundButton.OnCheckedChangeListener
{
    private View rootView;
    private Handler callHandler;
    private RelativeLayout rlSettingClearAllData;

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
        initSegmentsListener();
        clearAllDate();
        initUI();

        return rootView;
    }

    public void clearAllDate() {
        rlSettingClearAllData = (RelativeLayout) rootView.findViewById(R.id.Settingsclear);
        rlSettingClearAllData.setOnClickListener(new OnClickListener() {

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
        switch (arg0.getId()) {
            case R.id.SettingsListReDevice:
                MsgUtils.sendMsg(callHandler, Constant.MSG_GOTO_CHOOSE_DEVICE);
                break;
            default:
                break;
        }
    }
}

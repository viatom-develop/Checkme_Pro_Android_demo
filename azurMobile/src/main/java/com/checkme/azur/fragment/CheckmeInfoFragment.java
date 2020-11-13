package com.checkme.azur.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.checkme.newazur.R;
import com.checkme.azur.element.CheckmeDevice;
import com.checkme.azur.element.Constant;
import com.checkme.azur.utils.MsgUtils;

public class CheckmeInfoFragment extends Fragment implements OnClickListener {
    private View layout;
    private CheckmeDevice checkmeInfo;
    private Handler callerHandler;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        layout = inflater.inflate(R.layout.fragment_checkme_info, container, false);
        return layout;
    }

    public void setArguments(CheckmeDevice checkmeInfo, Handler handler){
        this.checkmeInfo = checkmeInfo;
        this.callerHandler = handler;
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        initUI(checkmeInfo);
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    /**
     * 初始化设备信息View
     *
     * @param checkmeInfo
     */
    private void initUI(CheckmeDevice checkmeInfo) {
        if (checkmeInfo == null) {
            return;
        }
        ImageView deviceImg = (ImageView) layout.findViewById(R.id.FragmentCheckmeInfoImgCheckme);
        TextView textModel = (TextView) layout.findViewById(R.id.FragmentCheckmeInfoTextModel);
        TextView textSoftware = (TextView) layout.findViewById(R.id.FragmentCheckmeInfoTextSoftware);
        textSoftware.setVisibility(View.GONE);
        TextView textSN = (TextView) layout.findViewById(R.id.FragmentCheckmeInfoTextSN);
        Button bnUpdate = (Button) layout.findViewById(R.id.FragmentCheckmeInfoBnUpdate);

        textModel.setText(Constant.getString(R.string.series) + " "
                + makeSeries(checkmeInfo.getModel()));

//        textSoftware.setText(Constant.getString(R.string.version) + " "
//                + makeVersion(checkmeInfo.getSoftware()));
        textSN.setText(Constant.getString(R.string.sn) + " " + checkmeInfo.getSn());

        if(!TextUtils.isEmpty(checkmeInfo.getModel()) && checkmeInfo.getModel().equals("6621")) {
            deviceImg.setImageResource(R.drawable.checkmel);
            bnUpdate.setVisibility(View.GONE);
        }

        bnUpdate.setOnClickListener(this);
    }


    /**
     * 根据model号生成系列名称
     *
     * @param model
     * @return
     */
    private String makeSeries(String model) {
        if (model.equals("6632")) {
            return Constant.getString(R.string.checkme_pro);
        } else if (model.equals("6631")) {
            return Constant.getString(R.string.checkme_plus);
        } else if (model.equals("6621")) {
            return Constant.getString(R.string.checkme_lite);
        } else if (model.equals("6611")) {
            return Constant.getString(R.string.checkme_pod);
        } else {
            return Constant.getString(R.string.unknow_device);
        }
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
        return version;
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        switch (arg0.getId()) {
            case R.id.FragmentCheckmeInfoBnUpdate:
                MsgUtils.sendMsg(callerHandler, Constant.MSG_BNUPDATE_CLICKED);
                break;

            default:
                break;
        }
    }
}

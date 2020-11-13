package com.viatom.azur.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.viatom.azur.bluetooth.BTBinder;
import com.viatom.newazur.R;
import com.viatom.azur.activity.ECGAnalyze;
import com.viatom.azur.bluetooth.BTUtils;
import com.viatom.azur.bluetooth.ReadFileListener;
import com.viatom.azur.element.Constant;
import com.viatom.azur.measurement.ECGItem;
import com.viatom.azur.measurement.MeasurementConstant;
import com.viatom.azur.tools.ECGReportUtils;
import com.viatom.azur.tools.PreferenceUtils;
import com.viatom.azur.tools.ShareUtils;
import com.viatom.azur.tools.StringMaker;
import com.viatom.azur.tools.VoiceManager;
import com.viatom.azur.utils.FileDriver;
import com.viatom.azur.utils.LogUtils;
import com.viatom.azur.utils.MsgUtils;
import com.viatom.azur.utils.UIUtils;
import com.viatom.azur.widget.ECGView;
import com.viatom.azur.widget.ECGView.ECGViewDelegate;
import com.viatom.azur.widget.JProgressDialog;

public class ECGDetailFragment extends Fragment implements ECGViewDelegate, ReadFileListener {
    private View rootView;
    private BTBinder mBinder;
    private ECGView ecgView;

    private ECGItem curItem;
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
    Handler detailHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.CMD_TYPE_VOICE:
                    // 下载好后转换格式
                    VoiceManager.convertVoice(curItem.getDate());
                    // 声音&进度条两个图标显示切换
                    rootView.findViewById(R.id.ECGDetailImgVoice).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.ECGDetailImgPro).setVisibility(View.INVISIBLE);
                    break;
                case Constant.MSG_REPORT_BITMAP_CONVERTED:
                    ECGReportUtils.removeScrollView((RelativeLayout) rootView);
                    Bitmap bitmap = (Bitmap) msg.obj;
                    if (msg.arg1 == Constant.SHARE_TYPE_LOCAL) {// 本地存储
                        ShareUtils.shareToLocal(mContext, bitmap,
                                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()));
                    } else if (msg.arg1 == Constant.SHARE_TYPE_NET) {// 网络分享
                        ShareUtils.shareToNet(mContext, bitmap);
                    }
                    JProgressDialog.cancel();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    public void setArguments(Bundle mBundle) {
        this.mBinder = Constant.binder;
        curItem = (ECGItem) mBundle.getSerializable("CurItem");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        rootView = inflater.inflate(R.layout.fragment_ecg_detail, container, false);

        // 只在竖屏时加载结果界面
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            initResultView();
            setBnVoiceFuncs();
        }

        // 延迟添加ecg-wave为获得layout宽度
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                addECGWave();
            }
        }, 20);

        return rootView;
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    private void initResultView() {
        TextView TextHR, TextST, TextQRS, TextResult;
//                TextQT, TextQTC;
        ImageView IMGCheckMode, IMGResult;
        // 获得用户ID&名字，设置标题
        TextHR = (TextView) rootView.findViewById(R.id.ECGDetailTextHR);
        TextQRS = (TextView) rootView.findViewById(R.id.ECGDetailTextQRS);
        TextST = (TextView) rootView.findViewById(R.id.ECGDetailTextST);
        TextResult = (TextView) rootView.findViewById(R.id.ECGDetailTextResult);
        TextResult.setVisibility(View.GONE);
//        TextQT = (TextView) rootView.findViewById(R.id.ECGDetailTextQT);
//        TextQTC = (TextView) rootView.findViewById(R.id.ECGDetailTextQTC);
        IMGCheckMode = (ImageView) rootView.findViewById(R.id.ECGDetailImgCheckMode);
        IMGResult = (ImageView) rootView.findViewById(R.id.ECGDetailRelImgResult);
        ImageView IMGVoice = (ImageView) rootView.findViewById(R.id.ECGDetailImgVoice);
        int width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        TextHR.setText("HR " + (curItem.getInnerItem().getHR() == 0 ? "--" : curItem.getInnerItem().getHR()) + "/min");
        TextQRS.setText("QRS " + (curItem.getInnerItem().getQRS() == 0 ? "--" : curItem.getInnerItem().getQRS()) + "ms");
        TextST.setText(StringMaker.makeSTValueStr(curItem.getMeasuringMode(), curItem.getInnerItem().getST()));
        String fileName = StringMaker.makeDateFileName(curItem.getDate(), Constant.CMD_TYPE_ECG_NUM);
        if (FileDriver.isFileExist(Constant.dir, fileName + MeasurementConstant.QT_FILE_NAME)) {
            byte[] buf = FileDriver.read(Constant.dir, fileName + MeasurementConstant.QT_FILE_NAME);
//            if (buf[0] == 1) {
//                TextQT.setVisibility(View.INVISIBLE);
//                TextQTC.setVisibility(View.INVISIBLE);
//            } else {
//
//                TextQT.setVisibility(View.VISIBLE);
//                TextQTC.setVisibility(View.VISIBLE);
//                TextQT.setText(
//                        "QT " + (curItem.getInnerItem().getQT() == 0 ? "--" : curItem.getInnerItem().getQT()) + "ms");
//                TextQTC.setText(
//                        "QTc " + (curItem.getInnerItem().getQTc() == 0 ? "--" : curItem.getInnerItem().getQTc()) + "ms");
//            }

        } else {
//            TextQT.setVisibility(View.INVISIBLE);
//            TextQTC.setVisibility(View.INVISIBLE);
        }

        TextResult.setText(StringMaker.makeECGResult(curItem.getInnerItem().getStrResultIndex(), 2, true));
        // String checkDate = StringMaker.makeDateString(curItem.getDate())
        // + " " + StringMaker.makeTimeString(curItem.getDate());
        // TextDate.setText(checkDate);
        // TextBand.setText(Constant.ECG_BAND_LIST[curItem.getCheckMode() - 1]);
        // //带宽信息
        IMGCheckMode.setImageResource(Constant.CHECK_MODE_IMG[curItem.getMeasuringMode() - 1]);
        IMGResult.setImageResource(Constant.RESULT_IMG[curItem.getImgResult()]);
        IMGVoice.setImageResource(Constant.VOICE_IMG[curItem.getVoiceFlag()]);
        if (width <= UIUtils.dip2px(mContext, 480)) {
            TextHR.setTextSize(13);
            TextQRS.setTextSize(13);
//            TextQT.setTextSize(13);
//            TextQTC.setTextSize(13);
            TextST.setTextSize(13);
            TextResult.setTextSize(13);
            LayoutParams paramsCheckMode = (LayoutParams) IMGCheckMode.getLayoutParams();
            paramsCheckMode.height = UIUtils.dip2px(mContext, 40);
            paramsCheckMode.width = UIUtils.dip2px(mContext, 40);
            IMGCheckMode.setLayoutParams(paramsCheckMode);
            LayoutParams paramsResult = (LayoutParams) IMGResult.getLayoutParams();
            paramsResult.height = UIUtils.dip2px(mContext, 40);
            paramsResult.width = UIUtils.dip2px(mContext, 40);
            IMGResult.setLayoutParams(paramsResult);
        }
    }

    public void addECGWave() {
        RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.ECGDetailReChart);
        int ecgLineHeight = 150;// 每行固定150高
        int ecgLineNum = curItem.getInnerItem().getTimeLength() / 5;// 5s一行
        ecgView = new ECGView(mContext, curItem.getInnerItem().getECGData(), relativeLayout.getMeasuredWidth(),
                ecgLineHeight * ecgLineNum, ecgLineNum);
        ecgView.setDelegate(this);
        relativeLayout.removeAllViews();
        RelativeLayout.LayoutParams params = new LayoutParams(relativeLayout.getMeasuredWidth(),
                ecgLineHeight * ecgLineNum);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        ecgView.setLayoutParams(params);
        relativeLayout.addView(ecgView);

        // 重设layout参数（高度）
        ViewGroup.LayoutParams layoutParams = relativeLayout.getLayoutParams();
        layoutParams.height = ecgLineHeight * ecgLineNum;
        relativeLayout.setLayoutParams(layoutParams);

        // 时间，带宽
        TextView tvDate = (TextView) rootView.findViewById(R.id.ECGDetailTextDate);
        String checkDate = StringMaker.makeDateString(curItem.getDate()) + " "
                + StringMaker.makeTimeString(curItem.getDate());
        tvDate.setText(checkDate);
    }

    @Override
    public void onReadPartFinished(String fileName, byte fileType, float percentage) {
        // TODO Auto-generated method stub
        // 没有进度条
        // MsgUtils.SendMSG(detailHandler, Constant.MSG_PART_FINISHED,
        // (int)(percentage * 100));
    }

    @Override
    public void onReadSuccess(String fileName, byte fileType, byte[] fileBuf) {
        // TODO Auto-generated method stub
        // 删除旧文件，保存新数据表到本地文件
        FileDriver.delFile(Constant.dir, fileName);
        FileDriver.write(Constant.dir, fileName, fileBuf);
        MsgUtils.sendMsg(detailHandler, fileType);
    }

    @Override
    public void onReadFailed(String fileName, byte fileType, byte errCode) {
        // TODO Auto-generated method stub
        MsgUtils.sendMsg(detailHandler, fileType, errCode);
    }

    @Override
    public void onRectSelected(float range) {
        // TODO Auto-generated method stub
        LogUtils.d("handler set canselect flag to false");
//        reFreshECGViewSelectable();
//        Intent intent = makeECGAnalyzeInfo(range);
//        mContext.startActivity(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        // 横屏 跳转到分析界面
        // if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        // Intent intent = makeECGAnalyzeInfo(0);
        // getActivity().startActivity(intent);
        // }
    }

    /**
     * 显示分享对话框
     */
    public void showShareAlertView() {
        String[] ShareList = {Constant.getString(R.string.save_to_album), Constant.getString(R.string.share)};
        AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setItems(ShareList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (whichButton == 0) {// Local
                            makeECGReport(Constant.SHARE_TYPE_LOCAL);
                        } else {// share
                            makeECGReport(Constant.SHARE_TYPE_NET);
                        }
                        JProgressDialog.show(mContext);
                    }
                }).show();
        alertDialog.setCanceledOnTouchOutside(true);
    }

    /**
     * 生成ecg报告
     *
     * @param shareType 分享种类，存本地或网络
     */
    protected void makeECGReport(int shareType) {
        LogUtils.d("开始生成ecg报告");
        ECGReportUtils.makeECGReportPicture(mContext, detailHandler, (RelativeLayout) rootView, curItem,
                shareType);
    }

    private void setBnVoiceFuncs() {
        ImageView IMGVoice = (ImageView) rootView.findViewById(R.id.ECGDetailImgVoice);
        IMGVoice.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                makeVoice(curItem.getDate());
            }
        });
    }

    public void makeVoice(Date date) {
        if (FileDriver.isFileExist(Constant.dir,
                StringMaker.makeDateFileName(date, Constant.CMD_TYPE_VOICE_CONVERTED))) {// 文件存在
            VoiceManager.playVoice(date);
        } else {// 不存在
            if (Constant.btConnectFlag) {// 在线
                String fileName = StringMaker.makeDateFileName(date, Constant.CMD_TYPE_VOICE);
                mBinder.interfaceReadFile(fileName, Constant.CMD_TYPE_VOICE, 5000, this);
                // 声音&进度条两个图标显示切换
                rootView.findViewById(R.id.ECGDetailImgVoice).setVisibility(View.INVISIBLE);
                rootView.findViewById(R.id.ECGDetailImgPro).setVisibility(View.VISIBLE);
            } else {// 离线
                Toast.makeText(mContext, Constant.getString(R.string.down_in_offline), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Intent makeECGAnalyzeInfo(float recRange) {

        int FirstSample = (int) (curItem.getInnerItem().getECGData().length * recRange); // 起始点
        Intent intent = new Intent();
        intent.setClass(mContext, ECGAnalyze.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Date", curItem.getDate());
        bundle.putSerializable("CurItem", curItem.getInnerItem());
        bundle.putInt("FirstSample", FirstSample);
        intent.putExtras(bundle);
        return intent;
    }

    private void reFreshECGViewSelectable() {
        // 3秒后重新允许点击
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    Thread.sleep(3);
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
                ecgView.setCanSelect(true);
            }
        }).start();
    }
}

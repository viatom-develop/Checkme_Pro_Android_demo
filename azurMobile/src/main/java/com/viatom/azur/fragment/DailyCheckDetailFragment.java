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
import android.widget.TextView;
import android.widget.Toast;

import com.viatom.azur.bluetooth.BTBinder;
import com.viatom.newazur.R;
import com.viatom.azur.activity.ECGAnalyze;
import com.viatom.azur.bluetooth.BTUtils;
import com.viatom.azur.bluetooth.ReadFileListener;
import com.viatom.azur.element.Constant;
import com.viatom.azur.measurement.BPCalItem;
import com.viatom.azur.measurement.DailyCheckItem;
import com.viatom.azur.measurement.MeasurementConstant;
import com.viatom.azur.measurement.User;
import com.viatom.azur.tools.DailyCheckReportUtils;
import com.viatom.azur.tools.ECGReportUtils;
import com.viatom.azur.tools.PreferenceUtils;
import com.viatom.azur.tools.ShareUtils;
import com.viatom.azur.tools.StringMaker;
import com.viatom.azur.tools.VoiceManager;
import com.viatom.azur.utils.FileDriver;
import com.viatom.azur.utils.LogUtils;
import com.viatom.azur.utils.MsgUtils;
import com.viatom.azur.widget.ECGView;
import com.viatom.azur.widget.ECGView.ECGViewDelegate;
import com.viatom.azur.widget.JProgressDialog;
import com.viatom.azur.widget.LeftRightDialog;

public class DailyCheckDetailFragment extends Fragment implements ECGViewDelegate, ReadFileListener {

    private View rootView;
    private BTBinder mBinder;
    private ECGView ecgView;
    private DailyCheckItem curItem;
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

    private Handler detailHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.CMD_TYPE_VOICE:
                    // 下载好后转换格式
                    VoiceManager.convertVoice(curItem.getDate());
                    // 声音&进度条两个图标显示切换
                    rootView.findViewById(R.id.DailyCheckDetailImgVoice).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.DailyCheckDetailPro).setVisibility(View.INVISIBLE);
                    break;
                case Constant.MSG_REPORT_BITMAP_CONVERTED:
                    DailyCheckReportUtils.removeScrollView((RelativeLayout) rootView);
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
        curItem = (DailyCheckItem) mBundle.getSerializable("CurItem");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        rootView = inflater.inflate(R.layout.fragment_daily_check_detail, container, false);

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
        if (curItem == null)
            return;

        TextView TextHR, TextQRS, TextECGResult, TextSPO2, TextPI, TextSPO2Result, TextBP, TextTime, TextQT, TextQTC;
        TextHR = (TextView) rootView.findViewById(R.id.DailyCheckDetailTextHR);
        TextQRS = (TextView) rootView.findViewById(R.id.DailyCheckDetailTextQRS);
//        TextQT = (TextView) rootView.findViewById(R.id.DailyCheckDetailTextQT);
//        TextQTC = (TextView) rootView.findViewById(R.id.DailyCheckDetailTextQTC);
        TextECGResult = (TextView) rootView.findViewById(R.id.DailyCheckDetailTextECGResult);
        TextECGResult.setVisibility(View.GONE);
        TextSPO2 = (TextView) rootView.findViewById(R.id.DailyCheckDetailTextSPO2);
        TextPI = (TextView) rootView.findViewById(R.id.DailyCheckDetailTextPI);
        TextPI.setVisibility(View.GONE);
        TextSPO2Result = (TextView) rootView.findViewById(R.id.DailyCheckDetailTextSPO2Result);
        TextSPO2Result.setVisibility(View.GONE);
        TextBP = (TextView) rootView.findViewById(R.id.DailyCheckDetailTextBP);
        TextTime = (TextView) rootView.findViewById(R.id.DailyCheckDetailTextTime);
        ImageView ECGIMGResult = (ImageView) rootView.findViewById(R.id.DailyCheckDetailImgECG);
        ImageView SPO2IMGResult = (ImageView) rootView.findViewById(R.id.DailyCheckDetailImgSPO2);
        ImageView IMGVoice = (ImageView) rootView.findViewById(R.id.DailyCheckDetailImgVoice);

        if (curItem.getHR() != 0)
            TextHR.setText("HR " + curItem.getHR() + "/min");
        else
            TextHR.setText("HR --/min");
        if (curItem.getInnerItem().getQRS() != 0)
            TextQRS.setText("QRS " + curItem.getInnerItem().getQRS() + "ms");
        else
            TextQRS.setText("QRS --ms");
        String fileName = StringMaker.makeDateFileName(curItem.getDate(), Constant.CMD_TYPE_ECG_NUM);
        if (FileDriver.isFileExist(Constant.dir, fileName + MeasurementConstant.QT_FILE_NAME)) {
            byte[] buf = FileDriver.read(Constant.dir, fileName + MeasurementConstant.QT_FILE_NAME);
//            if (buf[0] == 1) {
//                TextQT.setVisibility(View.INVISIBLE);
//                TextQTC.setVisibility(View.INVISIBLE);
//            } else {
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

        String strResult = StringMaker.makeECGResult(curItem.getInnerItem().getStrResultIndex(), 2, true);
        TextECGResult.setText(strResult);
        // //过长时设置小字体
        // if (strResult.length() >= 30) {
        // TextECGResult.setTextSize(12);
        // }

        // SPO2
        TextSPO2.setText(StringMaker.makeSPO2Str(curItem.getSPO2()));
        TextPI.setText("PI " + (curItem.getPI() == 0 ? "--" : curItem.getPI()));
        TextSPO2Result.setText(StringMaker.makeSPO2StrResult(curItem.getSPO2()));

        // BP
        TextBP.setText(StringMaker.makeBPValueStr(curItem));
        TextTime.setText(StringMaker.makeBPCalDate(curItem));

        // 笑脸
        ECGIMGResult.setImageResource(Constant.RESULT_IMG[curItem.getECGIMGResult()]);
        int i = curItem.getSPO2IMGResult();
        if (i == 0 || i == 1) {
            SPO2IMGResult.setImageResource(Constant.RESULT_IMG[i]);
        } else {
            SPO2IMGResult.setImageResource(Constant.RESULT_IMG[2]);
        }

        IMGVoice.setImageResource(Constant.VOICE_IMG[curItem.getVoiceFlag()]);

    }

    private void addECGWave() {
        RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.DailyCheckDetailReChart);
        ecgView = new ECGView(mContext, curItem.getInnerItem().getECGData(), layout.getWidth(), layout.getHeight(),
                4);
        ecgView.setDelegate(this);
        layout.removeAllViews();
        layout.addView(ecgView);

        // 带宽信息&时间
        TextView tvDate = (TextView) rootView.findViewById(R.id.DailyCheckDetailTextDate);
        tvDate.setText(
                StringMaker.makeDateString(curItem.getDate()) + " " + StringMaker.makeTimeString(curItem.getDate()));
    }

    @Override
    public void onRectSelected(float range) {
        // TODO Auto-generated method stub
        LogUtils.d("handler set canselect flag to false");
//        reFreshECGViewSelectable();
//        Intent intent = makeECGAnalyzeInfo(range);
//        mContext.startActivity(intent);
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
                            makeDLCReport(Constant.SHARE_TYPE_LOCAL);
                        } else {// share
                            makeDLCReport(Constant.SHARE_TYPE_NET);
                        }
                        JProgressDialog.show(mContext);
                    }
                }).show();
        alertDialog.setCanceledOnTouchOutside(true);
    }

    /**
     * 生成DLC报告
     *
     * @param shareType 分享种类，存本地或网络
     */
    private void makeDLCReport(int shareType) {
        LogUtils.d("开始生成ecg报告");
        DailyCheckReportUtils.makeDLCReportPicture(mContext, detailHandler, (RelativeLayout) rootView,
                Constant.curUser.getUserInfo(), curItem, shareType);
    }

    private void setBnVoiceFuncs() {
        ImageView IMGVoice = (ImageView) rootView.findViewById(R.id.DailyCheckDetailImgVoice);
        IMGVoice.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                makeVoice((Date) curItem.getDate());
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
                rootView.findViewById(R.id.DailyCheckDetailImgVoice).setVisibility(View.INVISIBLE);
                rootView.findViewById(R.id.DailyCheckDetailPro).setVisibility(View.VISIBLE);
            } else {// 离线
                Toast.makeText(mContext, Constant.getString(R.string.down_in_offline), Toast.LENGTH_SHORT).show();
            }
        }
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

    private Intent makeECGAnalyzeInfo(float recRange) {
        int FirstSample = (int) (curItem.getInnerItem().getECGData().length * recRange); // 起始点
        Intent intent = new Intent();
        intent.setClass(mContext, ECGAnalyze.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Date", curItem.getDate());
        bundle.putSerializable("CurItem", curItem.getInnerItem());
        bundle.putString("UserName", Constant.curUser.getUserInfo().getName());
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
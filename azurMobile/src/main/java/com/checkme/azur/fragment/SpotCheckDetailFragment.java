package com.checkme.azur.fragment;

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
import androidx.fragment.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.SubscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.checkme.azur.bluetooth.BTBinder;
import com.checkme.newazur.R;
import com.checkme.azur.activity.ECGAnalyze;
import com.checkme.azur.bluetooth.ReadFileListener;
import com.checkme.azur.element.Constant;
import com.checkme.azur.measurement.MeasurementConstant;
import com.checkme.azur.measurement.SpotCheckItem;
import com.checkme.azur.tools.ECGReportUtils;
import com.checkme.azur.tools.PreferenceUtils;
import com.checkme.azur.tools.ShareUtils;
import com.checkme.azur.tools.SpotCheckReportUtils;
import com.checkme.azur.tools.StringMaker;
import com.checkme.azur.tools.VoiceManager;
import com.checkme.azur.utils.FileDriver;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.utils.MsgUtils;
import com.checkme.azur.widget.ECGView;
import com.checkme.azur.widget.ECGView.ECGViewDelegate;
import com.checkme.azur.widget.JProgressDialog;

public class SpotCheckDetailFragment extends Fragment implements ReadFileListener, ECGViewDelegate {
    private View rootView;
    private BTBinder mBinder;
    private ECGView ecgView;
    private SpotCheckItem curItem;
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
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.CMD_TYPE_VOICE:
                    // 下载好后转换格式
                    VoiceManager.convertVoice(curItem.getDate());
                    // 声音&进度条两个图标显示切换
                    rootView.findViewById(R.id.iv_voice).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.pro_voice).setVisibility(View.INVISIBLE);
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

    public void setArguments(Bundle bundle) {
        this.mBinder = Constant.binder;
        curItem = (SpotCheckItem) bundle.getSerializable("CurItem");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        rootView = inflater.inflate(R.layout.fragment_spot_check_detail, container, false);

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
                if ((curItem.getFunc() & SpotCheckItem.MODE_ECG) == 0) {
                    // 没有ECG
                    addNoECGWave();
                } else {
                    // 有ECG
                    addECGWave();
                }
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
        if (curItem == null) {
            return;
        }
        TextView TextHR, TextQRS, TextECGResult, TextSPO2, TextPI, TextSPO2Result, TextTemp, TextQT, TextQTC;
        TextHR = (TextView) rootView.findViewById(R.id.tv_hr);
        TextQRS = (TextView) rootView.findViewById(R.id.tv_qrs);
        TextECGResult = (TextView) rootView.findViewById(R.id.tv_ecg_result);
        TextECGResult.setVisibility(View.GONE);
        TextSPO2 = (TextView) rootView.findViewById(R.id.tv_spo2);
        TextPI = (TextView) rootView.findViewById(R.id.tv_pi);
        TextPI.setVisibility(View.GONE);
        TextSPO2Result = (TextView) rootView.findViewById(R.id.tv_spo2_result);
        TextTemp = (TextView) rootView.findViewById(R.id.tv_temp);
        TextQT = (TextView) rootView.findViewById(R.id.tv_qt);
        TextQTC = (TextView) rootView.findViewById(R.id.tv_qtc);
        ImageView ECGIMGResult = (ImageView) rootView.findViewById(R.id.iv_ecg);
        ImageView SPO2IMGResult = (ImageView) rootView.findViewById(R.id.iv_spo2);
        ImageView TempIMGResult = (ImageView) rootView.findViewById(R.id.iv_temp);
        ImageView IMGVoice = (ImageView) rootView.findViewById(R.id.iv_voice);

        // ECG
        if ((curItem.getFunc() & SpotCheckItem.MODE_ECG) != 0) {
            // 有ECG
            if (curItem.getHr() != 0)
                TextHR.setText("HR " + curItem.getHr() + "/min");
            else
                TextHR.setText("HR --/min");
            if (curItem.getInnerItem().getQRS() != 0)
                TextQRS.setText("QRS " + curItem.getInnerItem().getQRS() + "ms");
            else
                TextQRS.setText("QRS --ms");

            String strResult = StringMaker.makeECGResult(curItem.getInnerItem().getStrResultIndex(), 2, false);
            TextECGResult.setText(strResult);
            // 过长时设置小字体
            if (strResult.length() >= 30) {
                TextECGResult.setTextSize(12);
            }
            ECGIMGResult.setImageResource(Constant.RESULT_IMG[curItem.getEcgImgResult()]);
            // QT&&QTC
            String fileName = StringMaker.makeDateFileName(curItem.getDate(), Constant.CMD_TYPE_ECG_NUM);
            if (FileDriver.isFileExist(Constant.dir, fileName + MeasurementConstant.QT_FILE_NAME)) {

                byte[] buf = FileDriver.read(Constant.dir, fileName + MeasurementConstant.QT_FILE_NAME);
                if (buf[0] == 1) {
                    TextQT.setVisibility(View.INVISIBLE);
                    TextQTC.setVisibility(View.INVISIBLE);
                } else {

                    TextQT.setVisibility(View.VISIBLE);
                    TextQTC.setVisibility(View.VISIBLE);
                    TextQT.setText(
                            "QT " + (curItem.getInnerItem().getQT() == 0 ? "--" : curItem.getInnerItem().getQT()) + "ms");
                    TextQTC.setText("QTc " + (curItem.getInnerItem().getQTc() == 0 ? "--" : curItem.getInnerItem().getQTc())
                            + "ms");
                }
            } else {
                TextQT.setVisibility(View.INVISIBLE);
                TextQTC.setVisibility(View.INVISIBLE);
            }

        } else {
            // 没有ECG
            TextHR.setText("HR --/min");
            TextQRS.setText("QRS --ms");
            // QT&&QTC
            if (PreferenceUtils.readStrPreferences(mContext, Constant.FILE_VER_KEY)
                    .equals(Constant.FILE_VER_NEW)) {

                TextQT.setVisibility(View.VISIBLE);
                TextQTC.setVisibility(View.VISIBLE);
                TextQT.setText("QT --ms");
                TextQTC.setText("QTc --ms");
            } else {
                TextQT.setVisibility(View.INVISIBLE);
                TextQTC.setVisibility(View.INVISIBLE);
            }
            TextECGResult.setText(getString(R.string.no_ecg));
        }

        // SPO2
        if ((curItem.getFunc() & SpotCheckItem.MODE_SPO2) != 0) {
            // 有血氧
            TextSPO2.setText(StringMaker.makeSPO2Str(curItem.getSpo2()));
            TextPI.setText("PI " + (curItem.getPi() == 0 ? "--" : curItem.getPi()));
            TextSPO2Result.setText(StringMaker.makeSPO2StrResult(curItem.getSpo2()));
            SPO2IMGResult.setImageResource(Constant.RESULT_IMG[curItem.getSpo2ImgResult()]);
        } else {
            // 无血氧
            TextSPO2.setText(StringMaker.makeSPO2Str(0));
            TextPI.setText("PI --");
            SpannableString spannableString;
            spannableString = new SpannableString("SpO2 " + getString(R.string.no_spo2));
            spannableString.setSpan(new SubscriptSpan(), 3, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new RelativeSizeSpan(0.5f), 3, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            TextSPO2Result.setText(spannableString);
        }

        // Temp
        if ((curItem.getFunc() & SpotCheckItem.MODE_TEMP) != 0) {
            // 有体温
            TextTemp.setText(getString(R.string.temp) + " " + StringMaker.makeTemperatureStr(curItem.getTemp(),
                    Constant.thermometerUnit == Constant.THERMOMETER_C));

            TempIMGResult.setImageResource(Constant.RESULT_IMG[curItem.getTempImgResult()]);
        } else {
            // 没有体温
            TextTemp.setText(getString(R.string.no_temp));
        }

        // 笑脸
        IMGVoice.setImageResource(Constant.VOICE_IMG[curItem.getVoiceFlag()]);
    }

    private void addECGWave() {
        RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.rl_chart);
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
        TextView tvDate = (TextView) rootView.findViewById(R.id.tv_date);
        String checkDate = StringMaker.makeDateString(curItem.getDate()) + " "
                + StringMaker.makeTimeString(curItem.getDate());
        tvDate.setText(checkDate);
    }

    private void addNoECGWave() {
        RelativeLayout relativeLayout = (RelativeLayout) rootView.findViewById(R.id.rl_chart_main);
        TextView tvNoData = new TextView(mContext);
        tvNoData.setText(getString(R.string.no_data));
        tvNoData.setTextSize(30);
        tvNoData.setTextColor(getResources().getColor(R.color.DarkGray));
        tvNoData.setBackgroundColor(getResources().getColor(R.color.White));
        tvNoData.getPaint().setFakeBoldText(true);

        RelativeLayout.LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        tvNoData.setLayoutParams(params);
        relativeLayout.addView(tvNoData);
    }

    private void setBnVoiceFuncs() {
        ImageView IMGVoice = (ImageView) rootView.findViewById(R.id.iv_voice);
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
                rootView.findViewById(R.id.iv_voice).setVisibility(View.INVISIBLE);
                rootView.findViewById(R.id.pro_voice).setVisibility(View.VISIBLE);
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
        MsgUtils.sendMsg(handler, fileType);
    }

    @Override
    public void onReadFailed(String fileName, byte fileType, byte errCode) {
        // TODO Auto-generated method stub
        MsgUtils.sendMsg(handler, fileType, errCode);
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
                            makeSpotCheckReport(Constant.SHARE_TYPE_LOCAL);
                        } else {// share
                            makeSpotCheckReport(Constant.SHARE_TYPE_NET);
                        }
                        JProgressDialog.show(mContext);
                    }
                }).show();
        alertDialog.setCanceledOnTouchOutside(true);
    }

    /**
     * 生成spot报告
     *
     * @param shareType 分享种类，存本地或网络
     */
    protected void makeSpotCheckReport(int shareType) {
        LogUtils.d("开始生成spot报告");
        SpotCheckReportUtils.makeSpotReportPicture(mContext, handler, (RelativeLayout) rootView,
                Constant.curSpotUser.getUserInfo(), curItem, shareType);
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

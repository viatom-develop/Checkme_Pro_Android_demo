package com.checkme.azur.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarLineChartBase.BorderPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.Legend.LegendForm;
import com.github.mikephil.charting.utils.LimitLine;
import com.github.mikephil.charting.utils.LimitLine.LimitLabelPosition;
import com.checkme.newazur.R;
import com.checkme.azur.element.Constant;
import com.checkme.azur.measurement.SLMItem;
import com.checkme.azur.tools.MyBarLineChartTouchListener;
import com.checkme.azur.tools.SLMReportUtils;
import com.checkme.azur.tools.ShareUtils;
import com.checkme.azur.tools.StringMaker;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.widget.JProgressDialog;
import com.checkme.azur.widget.SLMChart;
import com.checkme.azur.widget.SLMView;

public class SLMDetailFragment extends Fragment {

    private View rootView;

    SLMView slmView;
    SLMChart spo2Chart, prChart;
    private final static int MSG_SLM_VIEW_FINISH = -1;
    private SLMItem curItem;
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
    private Handler slmDetailHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_SLM_VIEW_FINISH:
                    rootView.findViewById(R.id.SLMDetailPro).setVisibility(View.INVISIBLE);
                    LinearLayout linearLayout = (LinearLayout) rootView
                            .findViewById(R.id.SLMDetailLinearChart);
                    linearLayout.removeAllViews();
                    linearLayout.addView(slmView);
                    break;
                case Constant.MSG_REPORT_BITMAP_CONVERTED:
                    SLMReportUtils.removeScrollView((RelativeLayout) rootView);
                    Bitmap bitmap = (Bitmap) msg.obj;
                    if (msg.arg1 == Constant.SHARE_TYPE_LOCAL) {//本地存储
                        ShareUtils.shareToLocal(mContext, bitmap,
                                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()));
                    } else if (msg.arg1 == Constant.SHARE_TYPE_NET) {//网络分享
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

    public SLMDetailFragment() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void setArguments(Bundle mBundle) {
        curItem = (SLMItem) mBundle.getSerializable("CurItem");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        LogUtils.d("调用onCreateView");
        rootView = inflater.inflate(R.layout.fragment_slm_detail, container, false);

        //如果是竖屏，显示结果
        if (mContext.getResources().getConfiguration()
                .orientation == Configuration.ORIENTATION_PORTRAIT) {
            rootView = inflater.inflate(R.layout.fragment_slm_detail, container, false);
            initResultView();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                makeSpo2Wave();
                makePrWave();
                setChartListener();
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

    public void initResultView() {
        TextView TextLowOxygenTime = (TextView) rootView.findViewById(R.id.SLMDetailTextLowOxygenTime);
        TextView TextMinOxygen = (TextView) rootView.findViewById(R.id.SLMDetailTextMinOxygen);
        TextView TextTotalTime = (TextView) rootView.findViewById(R.id.SLMDetailTextTotalTime);
        TextView TextAverageOxygen = (TextView) rootView.findViewById(R.id.SLMDetailTextAverageOxygen);
        TextView TextResult = (TextView) rootView.findViewById(R.id.SLMDetailTextResult);
        ImageView IMGResult = (ImageView) rootView.findViewById(R.id.SLMDetailImgResult);

        TextLowOxygenTime.setText("<90% " + Constant.getString(R.string.stat) + curItem
                .getLowOxygenNum() + Constant.getString(R.string.drops) + ", "
                + StringMaker.makeSecondToMinute(curItem.getLowOxygenTime()));
        TextMinOxygen.setText(Constant.getString(R.string.lowest) + " "
                + ((curItem.getLowestOxygen()) == 0 ? "--" : curItem.getLowestOxygen()) + "%");
        TextAverageOxygen.setText(Constant.getString(R.string.average) + " "
                + ((curItem.getAverageOxygen()) == 0 ? "--" : curItem.getAverageOxygen()) + "%");
        TextTotalTime.setText(Constant.getString(R.string.total_duration) + " "
                + StringMaker.makeSecondToMinute(curItem.getTotalTime()));
        TextResult.setText(StringMaker.makeSLMResultStr(curItem.getImgResult(),
                curItem.getLowOxygenNum()));
        IMGResult.setImageResource(Constant.RESULT_IMG[curItem.getImgResult()]);
    }

    public void makeSLMView() {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.SLMDetailSeekBar);
//				int[] OxygenY = curItem.getInnerItem().getOxygenData();
//				int[] PRY = curItem.getInnerItem().getPrData();
//				LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.SLMDetailLinearChart);
//				slmView = new SLMView(getActivity(), StringMaker
//						.makeSLMTimeList(curItem.getStartTime(),
//								OxygenY.length), OxygenY, PRY, linearLayout.getWidth(),
//								linearLayout.getHeight()-seekBar.getHeight(), seekBar);
//				InitSeekBar(seekBar);
//				MsgUtils.SendMSG(slmDetailHandler, MSG_SLM_VIEW_FINISH);
//			}
//		}).start();
    }

    public void InitSeekBar(SeekBar seekBar) {
        seekBar.setMax(slmView.getLength());
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar arg0, int progress,
                                          boolean fromUser) {
//				LogUtils.Log("改变seekbar pro："+progress);
//				progress = progress < 0 ? 0 : progress;
                slmView.setCurrentX(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
            }
        });
    }

    /**
     * 生成spo2波形
     */
    private void makeSpo2Wave() {
        spo2Chart = (SLMChart) rootView.findViewById(R.id.SLMDetailChartSPO2);

//        spo2Chart.setUnit("%");
        spo2Chart.isSpo2Val(true);
        spo2Chart.setDrawUnitsInChart(true);
        spo2Chart.setStartAtZero(false);
        spo2Chart.setDrawYValues(false);
        spo2Chart.setDrawBorder(true);
        spo2Chart.setBorderPositions(new BorderPosition[]{
                BorderPosition.BOTTOM
        });
        spo2Chart.setDescription("");
        spo2Chart.setNoDataTextDescription(Constant.getString(R.string.no_data));
        spo2Chart.setDoubleTapToZoomEnabled(false);
        spo2Chart.setHighlightEnabled(false);
        spo2Chart.setTouchEnabled(true);
        spo2Chart.setDragEnabled(true);
        spo2Chart.setScaleEnabled(true);
//        mChart.setPinchZoom(true);
        spo2Chart.setData(makeSpo2Data(50));
        spo2Chart.setYRange(65, 100, true);
        spo2Chart.animateX(1500);
        Legend l = spo2Chart.getLegend();
        l.setForm(LegendForm.LINE);
    }

    /**
     * 生成pr波形
     */
    private void makePrWave() {
        prChart = (SLMChart) rootView.findViewById(R.id.SLMDetailChartPR);

//		prChart.setUnit("%");
        prChart.isSpo2Val(false);
        prChart.setDrawUnitsInChart(true);
        prChart.setStartAtZero(false);
        prChart.setDrawYValues(false);
        prChart.setDrawBorder(true);
        prChart.setBorderPositions(new BorderPosition[]{
                BorderPosition.BOTTOM
        });
        prChart.setDescription("");
        prChart.setNoDataTextDescription(Constant.getString(R.string.no_data));
        prChart.setDoubleTapToZoomEnabled(false);
        prChart.setHighlightEnabled(false);
        prChart.setTouchEnabled(true);
        prChart.setDragEnabled(true);
        prChart.setScaleEnabled(true);
//        mChart.setPinchZoom(true);
        prChart.setData(makePrData(50));
        prChart.animateX(1500);
        Legend l = prChart.getLegend();
        l.setForm(LegendForm.LINE);

        //设置Y轴范围
        Integer max = curItem.getInnerItem().getMaxPR();
        if (max <= 120) {
            prChart.setYRange(30, 120, true);
        } else if (max <= 150) {
            prChart.setYRange(30, 150, true);
        } else if (max <= 180) {
            prChart.setYRange(30, 180, true);
        } else {
            prChart.setYRange(30, 240, true);
        }
    }

    /**
     * 生成spo2波形的数据
     *
     * @param sampleStep 这个参数没有用 ， 减少代码量而没有改动。
     * @return
     */
    private LineData makeSpo2Data(int sampleStep) {
        if (curItem == null) {
            return null;
        }

        List<Integer> spo2List = curItem.getInnerItem().getSpo2List();
        Log.d("CD", spo2List.size() + "");
        //横坐标
        ArrayList<String> xVals = new ArrayList<String>();
        Date startDate = curItem.getDate();
        for (int i = 0; i < spo2List.size(); i++) {
            //每个点时间为起始点加2S取点间隔
            xVals.add(StringMaker.makeTimeString(new Date(startDate.getTime() + i * 2000)));
        }

        //Y值
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for (int i = 0; i < spo2List.size(); i++) {
            yVals.add(new Entry(spo2List.get(i), i));
        }

        LineDataSet lineDataSet = new LineDataSet(yVals, "SpO2(%)");
        lineDataSet.setColor(Color.BLACK);
        lineDataSet.setCircleColor(Color.BLACK);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleSize(0f);
        lineDataSet.setFillAlpha(65);
        lineDataSet.setFillColor(Color.BLACK);

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(lineDataSet);

        LineData data = new LineData(xVals, dataSets);

        //警告线
        LimitLine line = new LimitLine(90f);
        line.setLineWidth(1f);
        line.enableDashedLine(10f, 10f, 0f);
        line.setDrawValue(true);
        line.setLabelPosition(LimitLabelPosition.RIGHT);

        data.addLimitLine(line);

        return data;
    }

    /**
     * 生成pr波形的数据
     *
     * @param sampleStep
     * @return
     */
    private LineData makePrData(int sampleStep) {
        if (curItem == null) {
            return null;
        }

        List<Integer> spo2List = curItem.getInnerItem().getPrList();

        //横坐标
        ArrayList<String> xVals = new ArrayList<String>();
        Date startDate = curItem.getDate();
        for (int i = 0; i < spo2List.size(); i++) {
            //每个点时间为起始点加2S取点间隔
            xVals.add(StringMaker.makeTimeString(new Date(startDate.getTime() + i * 2000)));
        }

        //Y值
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for (int i = 0; i < spo2List.size(); i++) {
            yVals.add(new Entry(spo2List.get(i), i));
        }

        LineDataSet lineDataSet = new LineDataSet(yVals, "PR(/min)");
        lineDataSet.setColor(Color.BLACK);
        lineDataSet.setCircleColor(Color.BLACK);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleSize(0f);
        lineDataSet.setFillAlpha(65);
        lineDataSet.setFillColor(Color.BLACK);

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(lineDataSet);

        LineData data = new LineData(xVals, dataSets);

        //警告线
        LimitLine line = new LimitLine(35f);
        line.setLineWidth(1f);
        line.enableDashedLine(10f, 10f, 0f);
        line.setDrawValue(true);
        line.setLabelPosition(LimitLabelPosition.RIGHT);

        data.addLimitLine(line);

        return data;
    }

    /**
     * 为两个chart设置同一个listener
     */
    private void setChartListener() {
        if (spo2Chart == null || prChart == null) {
            return;
        }
        MyBarLineChartTouchListener mListener = new MyBarLineChartTouchListener(spo2Chart, prChart
                , spo2Chart.getTransformer().getTouchMatrix()
                , prChart.getTransformer().getTouchMatrix());
        spo2Chart.setOnTouchListener(mListener);
        prChart.setOnTouchListener(mListener);
    }

    /**
     * 显示分享对话框
     */
    public void showShareAlertView() {
        String[] ShareList = {Constant.getString(R.string.save_to_album),
                Constant.getString(R.string.share)};
        AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setItems(ShareList,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                if (whichButton == 0) {//Local
                                    makeSLMReport(Constant.SHARE_TYPE_LOCAL);
                                } else {// share
                                    makeSLMReport(Constant.SHARE_TYPE_NET);
                                }
                                JProgressDialog.show(mContext);
                            }
                        }).show();
        alertDialog.setCanceledOnTouchOutside(true);
    }

    /**
     * 生成slm报告
     *
     * @param shareType 分享种类，存本地或网络
     */
    protected void makeSLMReport(int shareType) {
        LogUtils.d("开始生成slm报告");
        SLMReportUtils.makeSLMReportPicture(mContext, slmDetailHandler
                , (RelativeLayout) rootView, curItem, shareType);
    }
}

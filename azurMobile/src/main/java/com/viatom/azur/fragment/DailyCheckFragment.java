package com.viatom.azur.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.viatom.azur.bluetooth.BTBinder;
import com.viatom.azur.element.CEApplication;
import com.viatom.azur.tools.PreferenceUtils;
import com.viatom.newazur.R;
import com.viatom.azur.bluetooth.BTUtils;
import com.viatom.azur.bluetooth.ReadFileListener;
import com.viatom.azur.element.CommonCreator;
import com.viatom.azur.element.Constant;
import com.viatom.azur.measurement.BPCalItem;
import com.viatom.azur.measurement.CommonItem;
import com.viatom.azur.measurement.DailyCheckItem;
import com.viatom.azur.measurement.MeasurementConstant;
import com.viatom.azur.measurement.User;
import com.viatom.azur.tools.DailyCheckAdapter;
import com.viatom.azur.tools.DailyCheckChartPagerAdapter;
import com.viatom.azur.tools.NoInfoViewUtils;
import com.viatom.azur.tools.StringMaker;
import com.viatom.azur.utils.CommonItemFilter;
import com.viatom.azur.utils.FileUtils;
import com.viatom.azur.utils.FileDriver;
import com.viatom.azur.utils.LogUtils;
import com.viatom.azur.utils.MsgUtils;
import com.viatom.azur.utils.TimeComparator;
import com.viatom.azur.widget.ChartView;
import com.viatom.azur.widget.DailyCheckChartViewPager;

public class DailyCheckFragment extends Fragment implements OnClickListener
        , OnItemClickListener, OnMenuItemClickListener, ReadFileListener {
    private BTBinder mBinder;
    private Handler callerHandler;
    private View rootView;
    private DailyCheckChartViewPager mPager;
    private DailyCheckChartPagerAdapter adapter;

    private DailyCheckAdapter dailyCheckAdapter;
    private int DownLoadingItemPos;

    int[] bnChartResList = {R.id.DailyCheckChartBnHR, R.id.DailyCheckChartBnSPO2,
            R.id.DailyCheckChartBnPI, R.id.DailyCheckChartBnBP};
    Button[] bnChartList = new Button[bnChartResList.length];
    int[] bnSwitchResList = {R.id.DailyCheckBnDay, R.id.DailyCheckBnWeek,
            R.id.DailyCheckBnMonth, R.id.DailyCheckBnYear};
    Button[] bnSwitchList = new Button[bnSwitchResList.length];
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
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.CMD_TYPE_DLC:
                    processMsgDLC(msg);
                    break;
                case Constant.CMD_TYPE_BPCAL:
                    //无论有无bpcal，都不提示，直接读本地dlc并刷新
                    readLocalDailyCheckList();
                    break;
                case Constant.CMD_TYPE_ECG_NUM:
                    processMsgECG(msg);
                    break;
                case Constant.MSG_PART_FINISHED:
                    if (dailyCheckAdapter != null)
                        dailyCheckAdapter.SetDownProgress(msg.arg1);
                    break;
                default:
                    break;
            }
        }

    };

    /**
     * 接收到dlc-list后处理
     *
     * @param msg
     */
    private void processMsgDLC(Message msg) {
        if (msg == null)
            return;
        if (msg.arg1 == ReadFileListener.ERR_CODE_TIMEOUT) {
            LogUtils.d("获取dlc列表超时");
            Toast.makeText(mContext, Constant.getString(R.string.time_out)
                    , Toast.LENGTH_SHORT).show();
            //虽然在线列表获取失败，但同样读本地列表
            readLocalDailyCheckList();
        } else if (msg.arg1 == ReadFileListener.ERR_CODE_NORMAL) {
            LogUtils.d("dlc列表为空");
            //虽然在线列表获取失败，但同样读本地列表
            readLocalDailyCheckList();
        } else {//正常情况下,继续读BPCal
            mBinder.interfaceReadFile("bpcal.dat", Constant.CMD_TYPE_BPCAL, 5000, this);
        }
    }

    /**
     * 接收到ECG-Num后处理
     *
     * @param msg
     */
    private void processMsgECG(Message msg) {
        if (msg == null)
            return;
        if (msg.arg1 == ReadFileListener.ERR_CODE_TIMEOUT) {
            LogUtils.d("获取ecg数据超时");
            Toast.makeText(mContext, Constant.getString(R.string.time_out)
                    , Toast.LENGTH_SHORT).show();
            if (dailyCheckAdapter != null)
                dailyCheckAdapter.SetItembDownloading(DownLoadingItemPos,
                        false);
        } else if (msg.arg1 == ReadFileListener.ERR_CODE_NORMAL) {
            LogUtils.d("ecg数据不存在");
            Toast.makeText(mContext, Constant.getString(R.string.download_failed)
                    , Toast.LENGTH_SHORT).show();
            if (dailyCheckAdapter != null)
                dailyCheckAdapter.SetItembDownloading(DownLoadingItemPos, false);
        }
    }

    public void setArguments(Handler handler) {
        this.mBinder = Constant.binder;
        this.callerHandler = handler;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        rootView = inflater.inflate(R.layout.fragment_daily_check, container, false);
        setButtonFunc();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                getDailyCheckList();
            }
        }, 50);
        return rootView;
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    private void getDailyCheckList() {
        if (Constant.curUser.getDlcList().size() == 0) {//列表不在缓存中
            if (Constant.btConnectFlag) {
                String fileName = Constant.curUser.getUserInfo().getID() + "dlc.dat";
                mBinder.interfaceReadFile(fileName, MeasurementConstant.CMD_TYPE_DLC, 5000, this);
            } else {
                readLocalDailyCheckList();
            }
        } else {//列表在缓存中
            refreshView();
        }
    }

    public void refreshView() {
        rootView.findViewById(R.id.DailyCheckListPro).setVisibility(View.INVISIBLE);
        ReFiltrateList();
        InitViewPager();
        refreshNoInfoView();
    }

    public void InitViewPager() {
        mPager = (DailyCheckChartViewPager) rootView.findViewById(R.id.DailyCheckChartViewPager);
        List<View> listViews = new ArrayList<View>();
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        listViews.add(mInflater.inflate(R.layout.tab_chart, null));
        listViews.add(mInflater.inflate(R.layout.tab_chart, null));
        listViews.add(mInflater.inflate(R.layout.tab_chart, null));
        listViews.add(mInflater.inflate(R.layout.tab_chart, null));
        adapter = new DailyCheckChartPagerAdapter(listViews);
        mPager.setAdapter(adapter);
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());

        int width = rootView.findViewById(R.id.DailyCheckChartViewPager).getWidth();
        int height = rootView.findViewById(R.id.DailyCheckChartViewPager).getHeight();

        LogUtils.d("width" + width);
        LogUtils.d("height" + height);
        if (Constant.curUser.getDlcList() == null)
            return;
        adapter.addHRView(mContext, Constant.DLC_INFO_HR, width, height);
        String deviceName = PreferenceUtils.readStrPreferences(Constant.mContext, "PreDeviceName");
        if(!TextUtils.isEmpty(deviceName) && deviceName.startsWith("CheckmeLE")) {
            adapter.AddOxygenView(mContext, Constant.DLC_INFO_NONE, width, height);
        } else {
            adapter.AddOxygenView(mContext, Constant.DLC_INFO_OXYGEN, width, height);
        }

        adapter.addPIView(mContext, Constant.DLC_INFO_PI, width, height);
        adapter.AddBPView(mContext, width, height);

        refreshBnChartView();
        refreshBnSwitchView(1);//默认week选中
    }

    public void refreshNoInfoView() {

        if (Constant.curUser.getDlcList() == null || Constant.curUser.getDlcList().size() == 0) {
            NoInfoViewUtils.showNoInfoView(mContext, rootView.findViewById(R.id.ImgNoInfo));
        } else {
            NoInfoViewUtils.hideNoInfoView(mContext, rootView.findViewById(R.id.ImgNoInfo));
        }
    }

    private void setButtonFunc() {
        for (int i = 0; i < bnChartResList.length; i++) {
            bnChartList[i] = (Button) rootView.findViewById(bnChartResList[i]);
            bnChartList[i].setOnClickListener(this);
        }
        for (int i = 0; i < bnSwitchResList.length; i++) {
            bnSwitchList[i] = (Button) rootView.findViewById(bnSwitchResList[i]);
            bnSwitchList[i].setOnClickListener(this);
        }
    }

    /**
     * 更新选中按钮
     */
    private void refreshBnChartView() {
        int curTab = mPager.getCurrentItem();
        if (curTab > bnChartList.length)
            return;
        for (int i = 0; i < bnChartList.length; i++) {
            if (i == curTab) {//被选中
                bnChartList[i].setSelected(true);
            } else {
                bnChartList[i].setSelected(false);
            }
        }
    }

    private void refreshBnSwitchView(int index) {
        for (int i = 0; i < bnSwitchList.length; i++) {
            if (i == index) {//被选中
                bnSwitchList[i].setSelected(true);
            } else {
                bnSwitchList[i].setSelected(false);
            }
        }
    }

    /**
     * 重新筛选列表
     */
    public void ReFiltrateList() {
        SwipeMenuListView listView = (SwipeMenuListView) rootView.findViewById(R.id.DailyCheckList);
        if (Constant.curUser == null)
            return;
        if (Constant.curUser.getDlcList().size() != 0) {
            dailyCheckAdapter = new DailyCheckAdapter(mContext);
            listView.setAdapter(dailyCheckAdapter);
            listView.setVisibility(View.VISIBLE);
            listView.setMenuCreator(CommonCreator.makeSwipeMenuCreator(
                    mContext.getApplicationContext()));
            listView.setOnMenuItemClickListener(this);
            listView.setOnItemClickListener(this);
            View view = rootView.findViewById(R.id.DailyCheckReChartMain);
            view.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.INVISIBLE);
            //没数据则隐藏趋势图
            View view = rootView.findViewById(R.id.DailyCheckReChartMain);
            view.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 读取本地列表和校准文件
     */
    public void readLocalDailyCheckList() {
        //读取上次退出是存储的旧列表
        List<DailyCheckItem> oldDLCItems = FileUtils.readDailyCheckList(Constant.dir
                , Constant.curUser.getUserInfo().getID()
                        + MeasurementConstant.FILE_NAME_DLC_LIST_OLD);
        if (oldDLCItems != null && oldDLCItems.size() != 0) {
            Constant.curUser.addDLCList(oldDLCItems);
        }
        //读取新下载的列表，合并到旧列表
//        if (Constant.btConnectFlag) {
            List<DailyCheckItem> newDLCItems = FileUtils.readDailyCheckList(Constant.dir
                    , Constant.curUser.getUserInfo().getID()
                            + MeasurementConstant.FILE_NAME_DLC_LIST);
            if (newDLCItems != null && newDLCItems.size() != 0) {
                Constant.curUser.addDLCList(newDLCItems);
            }
//        }
        //去除重复，并倒序排列
        CommonItemFilter.removeSameItems(Constant.curUser.getDlcList());
        Collections.sort((List<CommonItem>) Constant.curUser
                        .getList(MeasurementConstant.CMD_TYPE_DLC)
                , new TimeComparator());

        //读校准文件，并设置到用户数据中
        List<BPCalItem> bpcalList = FileUtils.readBPCalFile(Constant.dir
                , MeasurementConstant.FILE_NAME_BPCAL);
        if (bpcalList != null && bpcalList.size() != 0) {
            for (int i = 0; i < Constant.userList.length; i++) {
                User user = Constant.userList[i];
                for (int j = 0; j < bpcalList.size(); j++) {
                    BPCalItem bpCalItem = bpcalList.get(j);
                    if (user.getUserInfo().getID() == bpCalItem.getId()) {
                        user.setBpCalItem(bpCalItem);
                    }
                }
            }
        }

        //刷新view
        refreshView();
    }

    /**
     * Item点击监听函数,下载或进入
     *
     * @param position
     */
    public void ClickItemProcess(View view, int position) {
        DailyCheckItem curItem = Constant.curUser.getDlcList().get(position);
        if (curItem.isDownloaded() == false) {// 没下载
            if (Constant.btConnectFlag) {// 如果蓝牙链接了
                if (mBinder.isAnyThreadRunning()) {
                    Toast.makeText(mContext,
                            Constant.getString(R.string.wait_last_downloading),
                            Toast.LENGTH_SHORT).show();
                } else {
                    DownLoadingItemPos = position;
                    dailyCheckAdapter.SetItembDownloading(position, true);
                    String fileName = StringMaker.makeDateFileName(
                            Constant.curUser.getDlcList().get(position)
                                    .getDate(), Constant.CMD_TYPE_ECG_NUM);

                    mBinder.interfaceReadFile(fileName, Constant.CMD_TYPE_ECG_NUM, 5000, this);

                }
            } else {// 否则不做操作
                view.startAnimation(AnimationUtils.loadAnimation(mContext,
                        R.anim.shake_x));
                Toast.makeText(mContext, Constant.getString(R.string.down_in_offline),
                        Toast.LENGTH_SHORT).show();
            }
        } else {// 下载完了
            Bundle bundle = MakeDetailInfo(position);
            MsgUtils.sendMsg(callerHandler, bundle, Constant.MSG_GOTO_DLC_DETAIL);
        }
    }

    /**
     * 传入Detail界面的数据
     *
     * @param position
     * @return
     */
    public Bundle MakeDetailInfo(int position) {
        Bundle bundle = new Bundle();
        DailyCheckItem item = Constant.curUser.getDlcList().get(position);
        item.setInnerItem(FileUtils.readECGInnerItem(Constant.dir
                , StringMaker.makeDateFileName(item.getDate()
                        , MeasurementConstant.CMD_TYPE_ECG_NUM), mContext));
        bundle.putSerializable("CurItem", item);
        return bundle;
    }

    @Override
    public void onReadPartFinished(String fileName, byte fileType, float percentage) {
        // TODO Auto-generated method stub
        MsgUtils.sendMsg(handler, Constant.MSG_PART_FINISHED, (int) (percentage * 100));
    }

    @Override
    public void onReadSuccess(String fileName, byte fileType, byte[] fileBuf) {
        // TODO Auto-generated method stub
        //删除旧文件，保存新数据表到本地文件
        FileDriver.delFile(Constant.dir, fileName);
        FileDriver.write(Constant.dir, fileName, fileBuf);
        MsgUtils.sendMsg(handler, fileType);
    }

    @Override
    public void onReadFailed(String fileName, byte fileType, byte errCode) {
        // TODO Auto-generated method stub
        //读失败同样删除旧文件
//        FileDriver.delFile(Constant.dir, fileName);
        MsgUtils.sendMsg(handler, fileType, errCode);
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        switch (arg0.getId()) {
            case R.id.DailyCheckChartBnHR:
                mPager.setCurrentItem(0);
                refreshBnChartView();
                break;
            case R.id.DailyCheckChartBnSPO2:
                mPager.setCurrentItem(1);
                refreshBnChartView();
                break;
            case R.id.DailyCheckChartBnPI:
                mPager.setCurrentItem(2);
                refreshBnChartView();
                break;
            case R.id.DailyCheckChartBnBP:
                mPager.setCurrentItem(3);
                refreshBnChartView();
                break;
            case R.id.DailyCheckBnDay:
                adapter.switchChart(mPager.getCurrentItem(), ChartView.FILTER_TYPE_DAY);
                refreshBnSwitchView(0);
                break;
            case R.id.DailyCheckBnWeek:
                adapter.switchChart(mPager.getCurrentItem(), ChartView.FILTER_TYPE_WEEK);
                refreshBnSwitchView(1);
                break;
            case R.id.DailyCheckBnMonth:
                adapter.switchChart(mPager.getCurrentItem(), ChartView.FILTER_TYPE_MONTH);
                refreshBnSwitchView(2);
                break;
            case R.id.DailyCheckBnYear:
                adapter.switchChart(mPager.getCurrentItem(), ChartView.FILTER_TYPE_YEAR);
                refreshBnSwitchView(3);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
        // TODO Auto-generated method stub
        LogUtils.d("Click Item" + position);
        ClickItemProcess(view, position);
    }

    @Override
    public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
        // TODO Auto-generated method stub
        if (index == 0) {//如果是第0个按键按下
            List<DailyCheckItem> list = Constant.curUser.getDlcList();
            FileDriver.delFile(Constant.dir, StringMaker.makeDateFileName(list.get(position)
                    .getDate(), Constant.CMD_TYPE_ECG_NUM));
            list.remove(position);
            FileUtils.saveListToFile(Constant.dir,Constant.curUser.getUserInfo().getID()+MeasurementConstant.FILE_NAME_DLC_LIST, (ArrayList<? extends CommonItem>) list);
            dailyCheckAdapter.notifyDataSetChanged();
            InitViewPager();
            if (list.size() == 0) {
                refreshView();
            }
        }

        return false;
    }

    public class MyOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageSelected(int arg0) {

            switch (arg0) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}

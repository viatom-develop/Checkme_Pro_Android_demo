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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.viatom.azur.bluetooth.BTBinder;
import com.viatom.azur.element.CEApplication;
import com.viatom.newazur.R;
import com.viatom.azur.bluetooth.BTUtils;
import com.viatom.azur.bluetooth.ReadFileListener;
import com.viatom.azur.element.CommonCreator;
import com.viatom.azur.element.Constant;
import com.viatom.azur.measurement.CommonItem;
import com.viatom.azur.measurement.ECGItem;
import com.viatom.azur.measurement.MeasurementConstant;
import com.viatom.azur.tools.ECGMainAdapter;
import com.viatom.azur.tools.NoInfoViewUtils;
import com.viatom.azur.tools.StringMaker;
import com.viatom.azur.utils.CommonItemFilter;
import com.viatom.azur.utils.FileUtils;
import com.viatom.azur.utils.FileDriver;
import com.viatom.azur.utils.LogUtils;
import com.viatom.azur.utils.MsgUtils;
import com.viatom.azur.utils.TimeComparator;

public class ECGMainFragment extends Fragment implements
        OnMenuItemClickListener, OnItemClickListener, ReadFileListener {
    private View rootView;
    private BTBinder mBinder;
    private Handler callerHandler;
    private ECGMainAdapter ecgMainAdapter;
    private int DownLoadingItemPos;//正在下载项
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
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.CMD_TYPE_ECG_LIST:
                    readLocalECGList();
                    break;
                case Constant.CMD_TYPE_ECG_NUM:
                    processMsgECG(msg);
                    break;
                case Constant.MSG_PART_FINISHED:
                    if (ecgMainAdapter != null)
                        ecgMainAdapter.SetDownProgress(msg.arg1);
                    break;
                default:
                    break;
            }
        }

        ;
    };


    public void setArguments(Handler mHandler) {
        this.mBinder = Constant.binder;
        this.callerHandler = mHandler;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        rootView = inflater.inflate(R.layout.fragment_ecg_main, container, false);
        getECGList();
        return rootView;
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    /**
     * 获取ecg列表
     */
    private void getECGList() {
        if (Constant.defaultUser.getEcgList().size() == 0) {//无数据
            if (Constant.btConnectFlag == true) {
                mBinder.interfaceReadFile(MeasurementConstant.FILE_NAME_ECG_LIST
                        , Constant.CMD_TYPE_ECG_LIST, 5000, this);
            } else {
                readLocalECGList();
            }
        } else {
            refreshView();
        }
    }

    @Override
    public void onReadPartFinished(String fileName, byte fileType,
                                   float percentage) {
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

    /**
     * 读取本地列表
     */
    public void readLocalECGList() {
        LogUtils.d("读取本地ECG列表文件");

        //读取上次退出是存储的旧列表
        List<ECGItem> oldECGItems = FileUtils.readECGList(Constant.dir
                , MeasurementConstant.FILE_NAME_ECG_LIST_OLD);
        if (oldECGItems != null && oldECGItems.size() != 0) {
            Constant.defaultUser.addECGList(oldECGItems);
        }
        //读取新下载的列表，合并到旧列表
//        if (Constant.btConnectFlag) {
        List<ECGItem> newECGItems = FileUtils.readECGList(Constant.dir
                , MeasurementConstant.FILE_NAME_ECG_LIST);
        if (newECGItems != null && newECGItems.size() != 0) {
            Constant.defaultUser.addECGList(newECGItems);
        }
//        }
        //去除重复，并倒序排列
        CommonItemFilter.removeSameItems(Constant.defaultUser.getEcgList());
        Collections.sort((List<CommonItem>) Constant.defaultUser
                        .getList(MeasurementConstant.CMD_TYPE_ECG_LIST)
                , new TimeComparator());

        refreshView();
    }

    /**
     * 刷新视图
     */
    public void refreshView() {
        ((ProgressBar) rootView.findViewById(R.id.ECGMainListPro))
                .setVisibility(View.INVISIBLE);
        reFiltrateList();
        refreshNoInfoView();
    }

    /**
     * 刷新列表
     */
    public void reFiltrateList() {
        if (Constant.defaultUser.getEcgList().size() != 0) {
            SwipeMenuListView listView = (SwipeMenuListView) rootView.findViewById(R.id.ECGMainList);
            ecgMainAdapter = new ECGMainAdapter(mContext);
            listView.setAdapter(ecgMainAdapter);
            listView.setMenuCreator(CommonCreator.makeSwipeMenuCreator(
                    mContext.getApplicationContext()));
            listView.setOnMenuItemClickListener(this);
            listView.setOnItemClickListener(this);

        } else {
            ListView list = (ListView) rootView.findViewById(R.id.ECGMainList);
            list.setVisibility(View.INVISIBLE);
        }
    }

    public void refreshNoInfoView() {

        if (Constant.defaultUser.getEcgList() == null || Constant.defaultUser.getEcgList().size() == 0) {
            NoInfoViewUtils.showNoInfoView(mContext, rootView.findViewById(R.id.ImgNoInfo));
        } else {
            NoInfoViewUtils.hideNoInfoView(mContext, rootView.findViewById(R.id.ImgNoInfo));
        }
    }

    /**
     * 处理下载的ECG数据
     *
     * @param msg
     */
    private void processMsgECG(Message msg) {
        if (msg == null) {
            return;
        }
        if (msg.arg1 == ReadFileListener.ERR_CODE_TIMEOUT) {
            LogUtils.d("获取ecg数据超时");
            Toast.makeText(mContext, Constant.getString(R.string.time_out)
                    , Toast.LENGTH_SHORT).show();
            if (ecgMainAdapter != null)
                ecgMainAdapter.SetItembDownloading(DownLoadingItemPos, false);
        } else if (msg.arg1 == ReadFileListener.ERR_CODE_NORMAL) {
            LogUtils.d("ecg数据不存在");
            Toast.makeText(mContext, Constant.getString(R.string.download_failed)
                    , Toast.LENGTH_SHORT).show();
            if (ecgMainAdapter != null)
                ecgMainAdapter.SetItembDownloading(DownLoadingItemPos, false);
        }
    }

    /**
     * Item点击监听函数,下载或进入
     *
     * @param position
     */
    public void processClickItem(View view, int position) {
        ECGItem item = Constant.defaultUser.getEcgList().get(position);

        if (item.isDownloaded() == false) {// 没下载
            if (Constant.btConnectFlag) {// 如果蓝牙链接了
                if (mBinder.isAnyThreadRunning()) {
                    Toast.makeText(mContext,
                            Constant.getString(R.string.wait_last_downloading),
                            Toast.LENGTH_SHORT).show();
                } else {
                    DownLoadingItemPos = position;
                    ecgMainAdapter.SetItembDownloading(position, true);
                    String fileName = StringMaker.makeDateFileName(item.getDate(),
                            Constant.CMD_TYPE_ECG_NUM);
                    mBinder.interfaceReadFile(fileName, Constant.CMD_TYPE_ECG_NUM, 5000, this);
                }
            } else {// 否则不做操作
                view.startAnimation(AnimationUtils.loadAnimation(
                        mContext, R.anim.shake_x));
                Toast.makeText(mContext,
                        Constant.getString(R.string.down_in_offline),
                        Toast.LENGTH_SHORT).show();
            }
        } else {// 下载完了
            Bundle bundle = makeDetailInfo(position);
            MsgUtils.sendMsg(callerHandler, bundle, Constant.MSG_GOTO_ECG_DETAIL);
        }
    }

    /**
     * 生成详情bundle内容
     *
     * @param position
     * @return
     */
    public Bundle makeDetailInfo(int position) {
        Bundle bundle = new Bundle();
        ECGItem item = Constant.defaultUser.getEcgList().get(position);
        item.setInnerItem(FileUtils.readECGInnerItem(Constant.dir
                , StringMaker.makeDateFileName(item.getDate()
                        , MeasurementConstant.CMD_TYPE_ECG_NUM), mContext));
        bundle.putSerializable("CurItem", item);
        return bundle;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
        // TODO Auto-generated method stub
        LogUtils.d("Click Item" + index);
        processClickItem(view, index);
    }

    @Override
    public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
        // TODO Auto-generated method stub
        if (index == 0) {//如果是第0个按键按下
            List<ECGItem> list = Constant.defaultUser.getEcgList();
            FileDriver.delFile(Constant.dir, StringMaker.makeDateFileName(list.get(position)
                    .getDate(), Constant.CMD_TYPE_ECG_NUM));
            list.remove(position);
            FileUtils.saveListToFile(
                    Constant.dir, MeasurementConstant.FILE_NAME_ECG_LIST, (ArrayList<? extends CommonItem>) list);
            ecgMainAdapter.notifyDataSetChanged();

            if (list.size() == 0) {
                refreshNoInfoView();
            }
        }
        return false;
    }

}

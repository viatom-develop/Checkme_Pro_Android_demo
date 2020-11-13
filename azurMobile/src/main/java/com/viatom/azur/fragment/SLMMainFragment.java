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
import com.viatom.azur.measurement.MeasurementConstant;
import com.viatom.azur.measurement.SLMItem;
import com.viatom.azur.tools.NoInfoViewUtils;
import com.viatom.azur.tools.SLMAdapter;
import com.viatom.azur.tools.StringMaker;
import com.viatom.azur.utils.CommonItemFilter;
import com.viatom.azur.utils.FileUtils;
import com.viatom.azur.utils.FileDriver;
import com.viatom.azur.utils.LogUtils;
import com.viatom.azur.utils.MsgUtils;
import com.viatom.azur.utils.TimeComparator;

public class SLMMainFragment extends Fragment implements
        OnItemClickListener, OnMenuItemClickListener, ReadFileListener {
    private View rootView;
    private BTBinder mBinder;
    private Handler mHandler;

    private SLMAdapter slmAdapter;
    private int DownLoadingItemPos;
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
                case Constant.CMD_TYPE_SLM_LIST:
                    ReadLocalSLMList();
                    break;
                case Constant.CMD_TYPE_SLM_NUM:
                    processMsgSLMNum(msg);
                    break;
                case Constant.MSG_PART_FINISHED:
                    if (slmAdapter != null) {
                        slmAdapter.SetDownProgress(msg.arg1);
                    }
                    break;

                default:
                    break;
            }
        }

        ;
    };


    public void setArguments(Handler mHandler) {
        this.mBinder = Constant.binder;
        this.mHandler = mHandler;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        rootView = inflater.inflate(R.layout.fragment_slm_main, container, false);
        getSLMList();
        return rootView;
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    private void getSLMList() {
        if (Constant.defaultUser.getSlmList().size() == 0) {
            if (Constant.btConnectFlag == true) {
                mBinder.interfaceReadFile(MeasurementConstant.FILE_NAME_SLM_LIST
                        , Constant.CMD_TYPE_SLM_LIST, 5000, this);
            } else {
                ReadLocalSLMList();
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
    public void ReadLocalSLMList() {

        //读取上次退出是存储的旧列表
        List<SLMItem> oldSLMItems = FileUtils.readSLMList(Constant.dir
                , MeasurementConstant.FILE_NAME_SLM_LIST_OLD);
        if (oldSLMItems != null && oldSLMItems.size() != 0) {
            Constant.defaultUser.addSLMList(oldSLMItems);
        }
        //读取新下载的列表，合并到旧列表
//        if (Constant.btConnectFlag) {
        List<SLMItem> newSLMItems = FileUtils.readSLMList(Constant.dir
                , MeasurementConstant.FILE_NAME_SLM_LIST);
        if (newSLMItems != null && newSLMItems.size() != 0) {
            Constant.defaultUser.addSLMList(newSLMItems);
        }
//        }
        //去除重复，并倒序排列
        CommonItemFilter.removeSameItems(Constant.defaultUser.getSlmList());
        Collections.sort((List<CommonItem>) Constant.defaultUser
                        .getList(MeasurementConstant.CMD_TYPE_SLM_LIST)
                , new TimeComparator());

        refreshView();
    }

    public void refreshView() {
        ((ProgressBar) rootView.findViewById(R.id.SLMMainListPro)).setVisibility(View.INVISIBLE);
        ReFiltrateList();
        refreshNoInfoView();
    }

    /**
     * 重新筛选列表
     */
    public void ReFiltrateList() {
        if (Constant.defaultUser.getSlmList().size() != 0) {
            SwipeMenuListView listView = (SwipeMenuListView) rootView
                    .findViewById(R.id.SLMMainList);
            slmAdapter = new SLMAdapter(mContext);
            listView.setAdapter(slmAdapter);
            listView.setVisibility(View.VISIBLE);
            listView.setMenuCreator(CommonCreator.makeSwipeMenuCreator(
                    mContext.getApplicationContext()));
            listView.setOnMenuItemClickListener(this);
            listView.setOnItemClickListener(this);
        } else {
            ListView listView = (ListView) rootView.findViewById(R.id.SLMMainList);
            listView.setVisibility(View.INVISIBLE);
        }
    }

    public void refreshNoInfoView() {

        if (Constant.defaultUser.getSlmList() == null || Constant.defaultUser.getSlmList().size() == 0) {
            NoInfoViewUtils.showNoInfoView(mContext, rootView.findViewById(R.id.ImgNoInfo));
        } else {
            NoInfoViewUtils.hideNoInfoView(mContext, rootView.findViewById(R.id.ImgNoInfo));
        }
    }

    /**
     * @param msg
     */
    private void processMsgSLMNum(Message msg) {
        if (msg == null) {
            return;
        }
        if (msg.arg1 < 0) {
            LogUtils.d("slm数据不存在");
            Toast.makeText(mContext, Constant.getString(R.string.download_failed),
                    Toast.LENGTH_SHORT).show();
            if (slmAdapter != null) {
                slmAdapter.SetItembDownloading(DownLoadingItemPos, false);
            }
        }
    }

    /**
     * Item点击监听函数,下载或进入
     *
     * @param position
     */
    public void ClickItemProcess(View view, int position) {
        SLMItem item = Constant.defaultUser.getSlmList().get(position);

        if (item.isDownloaded() == false) {// 没下载
            if (Constant.btConnectFlag) {// 如果蓝牙链接了
                if (mBinder.isAnyThreadRunning()) {
                    Toast.makeText(mContext,
                            Constant.getString(R.string.wait_last_downloading),
                            Toast.LENGTH_SHORT).show();
                } else {
                    DownLoadingItemPos = position;
                    slmAdapter.SetItembDownloading(position, true);
                    String fileName = StringMaker.makeDateFileName(item.getDate(),
                            Constant.CMD_TYPE_SLM_NUM);
                    mBinder.interfaceReadFile(fileName, Constant.CMD_TYPE_SLM_NUM, 5000, this);

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
            MsgUtils.sendMsg(mHandler, bundle, Constant.MSG_GOTO_SLM_DETAIL);
        }
    }

    /**
     * 传入Detail界面的数据
     *
     * @param position
     * @return
     */
    public Bundle makeDetailInfo(int position) {
        Bundle bundle = new Bundle();
        SLMItem item = Constant.defaultUser.getSlmList().get(position);
        item.setInnerItem(FileUtils.readSLMInnerItem(Constant.dir
                , StringMaker.makeDateFileName(item.getDate()
                        , MeasurementConstant.CMD_TYPE_SLM_NUM)));
        bundle.putSerializable("CurItem", item);
        return bundle;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
        // TODO Auto-generated method stub
        LogUtils.d("Click Item" + index);
        ClickItemProcess(view, index);
    }

    @Override
    public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
        // TODO Auto-generated method stub
        if (index == 0) {//如果是第0个按键按下
            List<SLMItem> list = Constant.defaultUser.getSlmList();
            FileDriver.delFile(Constant.dir, StringMaker.makeDateFileName(list.get(position)
                    .getDate(), Constant.CMD_TYPE_SLM_NUM));
            list.remove(position);
            FileUtils.saveListToFile(Constant.dir, MeasurementConstant.FILE_NAME_SLM_LIST, (ArrayList<? extends CommonItem>) list);
            slmAdapter.notifyDataSetChanged();

            if (list.size() == 0) {
                refreshNoInfoView();
            }
        }

        return false;
    }

}

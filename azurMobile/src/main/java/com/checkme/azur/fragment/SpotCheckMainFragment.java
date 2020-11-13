package com.checkme.azur.fragment;

import java.util.Collections;
import java.util.List;

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
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.checkme.azur.bluetooth.BTBinder;
import com.checkme.newazur.R;
import com.checkme.azur.bluetooth.ReadFileListener;
import com.checkme.azur.element.CommonCreator;
import com.checkme.azur.element.Constant;
import com.checkme.azur.measurement.CommonItem;
import com.checkme.azur.measurement.MeasurementConstant;
import com.checkme.azur.measurement.SpotCheckItem;
import com.checkme.azur.tools.NoInfoViewUtils;
import com.checkme.azur.tools.SpotCheckAdapter;
import com.checkme.azur.tools.StringMaker;
import com.checkme.azur.utils.CommonItemFilter;
import com.checkme.azur.utils.FileDriver;
import com.checkme.azur.utils.FileUtils;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.utils.MsgUtils;
import com.checkme.azur.utils.TimeComparator;

public class SpotCheckMainFragment extends Fragment implements
        ReadFileListener, OnMenuItemClickListener, OnItemClickListener {

    private View rootView;
    private BTBinder mBinder;
    private Handler callerHandler;

    private SpotCheckAdapter spotCheckAdapter;
    private int DownLoadingItemPos;

    public void setArguments(Handler handler) {
        this.mBinder = Constant.binder;
        this.callerHandler = handler;
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MeasurementConstant.CMD_TYPE_SPOT:
                    readLocalSpotCheckList();
                    break;
                case Constant.CMD_TYPE_ECG_NUM:
                    processMsgECG(msg);
                    break;
                case Constant.MSG_PART_FINISHED:
                    if (spotCheckAdapter != null)
                        spotCheckAdapter.SetDownProgress(msg.arg1);
                    break;
                default:
                    break;
            }
        }

        ;
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        rootView = inflater.inflate(R.layout.fragment_spot_check, container, false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                getSpotCheckList();
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

    private void getSpotCheckList() {
        if (Constant.curSpotUser.getSpotCheckList().size() == 0) {//列表不在缓存中
            if (Constant.btConnectFlag) {
                String fileName = Constant.curSpotUser.getUserInfo().getId()
                        + MeasurementConstant.FILE_NAME_SPOT_LIST;
                mBinder.interfaceReadFile(fileName, MeasurementConstant.CMD_TYPE_SPOT, 5000, this);
            } else {
                readLocalSpotCheckList();
            }
        } else {//列表在缓存中
            refreshView();
        }
    }

    /**
     * 读取本地列表和校准文件
     */
    private void readLocalSpotCheckList() {
        //读取上次退出是存储的旧列表
        List<SpotCheckItem> oldSpotCheckItems = FileUtils.readSpotCheckList(Constant.dir
                , Constant.curSpotUser.getUserInfo().getId()
                        + MeasurementConstant.FILE_NAME_SPOT_LIST_OLD);
        if (oldSpotCheckItems != null && oldSpotCheckItems.size() != 0) {
            Constant.curSpotUser.addSpotList(oldSpotCheckItems);
        }
        //读取新下载的列表，合并到旧列表
        if (Constant.btConnectFlag) {
            List<SpotCheckItem> newSpotCheckItems = FileUtils.readSpotCheckList(Constant.dir
                    , Constant.curSpotUser.getUserInfo().getId()
                            + MeasurementConstant.FILE_NAME_SPOT_LIST);
            if (newSpotCheckItems != null && newSpotCheckItems.size() != 0) {
                Constant.curSpotUser.addSpotList(newSpotCheckItems);
            }
        }
        //去除重复，并倒序排列
        CommonItemFilter.removeSameItems(Constant.curSpotUser.getSpotCheckList());
        Collections.sort((List<CommonItem>) Constant.curSpotUser
                        .getList(MeasurementConstant.CMD_TYPE_SPOT)
                , new TimeComparator());

        //刷新view
        refreshView();
    }

    public void refreshView() {
        rootView.findViewById(R.id.pro_main).setVisibility(View.INVISIBLE);
        ReFiltrateList();
        refreshNoInfoView();
    }

    /**
     * 重新筛选列表
     */
    public void ReFiltrateList() {
        SwipeMenuListView listView = (SwipeMenuListView) rootView.findViewById(R.id.lv_main);
        if (Constant.curSpotUser.getSpotCheckList().size() != 0) {
            spotCheckAdapter = new SpotCheckAdapter(getActivity());
            listView.setAdapter(spotCheckAdapter);
            listView.setVisibility(View.VISIBLE);
            listView.setMenuCreator(CommonCreator.makeSwipeMenuCreator(
                    getActivity().getApplicationContext()));
            listView.setOnMenuItemClickListener(this);
            listView.setOnItemClickListener(this);
        } else {
            listView.setVisibility(View.INVISIBLE);
        }
    }

    public void refreshNoInfoView() {

        if (Constant.curSpotUser.getSpotCheckList() == null || Constant.curSpotUser.getSpotCheckList().size() == 0) {
            NoInfoViewUtils.showNoInfoView(getActivity(), rootView.findViewById(R.id.iv_no_record));
        } else {
            NoInfoViewUtils.hideNoInfoView(getActivity(), rootView.findViewById(R.id.iv_no_record));
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
            Toast.makeText(getActivity(), Constant.getString(R.string.time_out)
                    , Toast.LENGTH_SHORT).show();
            if (spotCheckAdapter != null)
                spotCheckAdapter.SetItembDownloading(DownLoadingItemPos, false);
        } else if (msg.arg1 == ReadFileListener.ERR_CODE_NORMAL) {
            LogUtils.d("ecg数据不存在");
            Toast.makeText(getActivity(), Constant.getString(R.string.download_failed)
                    , Toast.LENGTH_SHORT).show();
            if (spotCheckAdapter != null)
                spotCheckAdapter.SetItembDownloading(DownLoadingItemPos, false);
        }
    }

    /**
     * Item点击监听函数,下载或进入
     *
     * @param position
     */
    public void processClickItem(View view, int position) {
        SpotCheckItem item = Constant.curSpotUser.getSpotCheckList().get(position);

        if (item.isDownloaded() == false) {// 没下载
            if (Constant.btConnectFlag) {// 如果蓝牙链接了
                if (mBinder.isAnyThreadRunning()) {
                    Toast.makeText(getActivity(),
                            Constant.getString(R.string.wait_last_downloading),
                            Toast.LENGTH_SHORT).show();
                } else {
                    DownLoadingItemPos = position;
                    spotCheckAdapter.SetItembDownloading(position, true);
                    String fileName = StringMaker.makeDateFileName(item.getDate(),
                            Constant.CMD_TYPE_ECG_NUM);
                    mBinder.interfaceReadFile(fileName, Constant.CMD_TYPE_ECG_NUM, 5000, this);
                }
            } else {// 否则不做操作
                view.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), R.anim.shake_x));
                Toast.makeText(getActivity(),
                        Constant.getString(R.string.down_in_offline),
                        Toast.LENGTH_SHORT).show();
            }
        } else {// 下载完了
            Bundle bundle = makeDetailInfo(position);
            MsgUtils.sendMsg(callerHandler, bundle, Constant.MSG_GOTO_SPOT_DETAIL);
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
        SpotCheckItem item = Constant.curSpotUser.getSpotCheckList().get(position);
        item.setInnerItem(FileUtils.readECGInnerItem(Constant.dir
                , StringMaker.makeDateFileName(item.getDate()
                        , MeasurementConstant.CMD_TYPE_ECG_NUM), getActivity()));
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
            List<SpotCheckItem> list = Constant.curSpotUser.getSpotCheckList();
            FileDriver.delFile(Constant.dir, StringMaker.makeDateFileName(list.get(position)
                    .getDate(), Constant.CMD_TYPE_ECG_NUM));
            list.remove(position);
            spotCheckAdapter.notifyDataSetChanged();

            if (list.size() == 0) {
                refreshNoInfoView();
            }
        }
        return false;
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
        LogUtils.d(fileName + "读取成功");
        //删除旧文件，保存新数据表到本地文件
        FileDriver.delFile(Constant.dir, fileName);
        FileDriver.write(Constant.dir, fileName, fileBuf);
        MsgUtils.sendMsg(handler, fileType);
    }

    @Override
    public void onReadFailed(String fileName, byte fileType, byte errCode) {
        // TODO Auto-generated method stub
        LogUtils.d(fileName + "读取失败  " + errCode);
        MsgUtils.sendMsg(handler, fileType, errCode);
    }


}

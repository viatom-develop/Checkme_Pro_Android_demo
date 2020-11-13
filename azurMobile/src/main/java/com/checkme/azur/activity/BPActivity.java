package com.checkme.azur.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.checkme.azur.bluetooth.ReadFileListener;
import com.checkme.azur.element.CommonCreator;
import com.checkme.azur.element.Constant;
import com.checkme.azur.fragment.RightMenuFragment;
import com.checkme.azur.measurement.BPItem;
import com.checkme.azur.measurement.MeasurementConstant;
import com.checkme.azur.tools.NoInfoViewUtils;
import com.checkme.azur.tools.StringMaker;
import com.checkme.azur.utils.FileDriver;
import com.checkme.azur.utils.FileUtils;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.utils.MsgUtils;
import com.checkme.newazur.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BPActivity extends BaseActivity implements SwipeMenuListView.OnMenuItemClickListener, ReadFileListener {

    SimpleAdapter simpleAdapter;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.CMD_TYPE_BP:
                    readLocalBPList();
                    break;
                case Constant.MSG_USER_CHOSED:
                    getSlidingMenu().showContent();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            initUI();
                            getBPList();
                        }
                    }, 300);
                    break;

            }
        };
    };


    @Override
    public boolean isTheSameIntent(int menuTag) {
        return (menuTag == Constant.MSG_BNBP_CLICKED);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bp);

        setSlidingMenu();

        initUI();
        getBPList();
    }

    public void initUI() {
        reFreshTitle(getResources().getString(R.string.title_bp) + " - " + Constant.curUser.getUserInfo().getName());
        reFreshActionBarBn(0,true,false);
    }

    public void setSlidingMenu() {
        SlidingMenu sm = getSlidingMenu();
        sm.setMode(SlidingMenu.LEFT_RIGHT);
        sm.setSecondaryMenu(R.layout.menu_frame_two);
        sm.setSecondaryShadowDrawable(R.drawable.shadowright);

        RightMenuFragment rmf = new RightMenuFragment();
        rmf.setmHandler(handler);
        rmf.setUserList();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.menu_frame_two,rmf)
                .commitAllowingStateLoss();
    }

    // 获取 BP list
    public void getBPList() {
        if (Constant.curUser.getBpList().size() == 0) {
            readLocalBPList();
        } else {
            refreshView();
        }
    }

    public void readLocalBPList() {
        List<BPItem> bpItems = FileUtils.readBPList(Constant.dir, Constant.curUser.getUserInfo().getID() + MeasurementConstant.FILE_NAME_BP_LIST);
        if (bpItems != null && bpItems.size() != 0) {
            Constant.curUser.addBPList(bpItems);
        }

        refreshView();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.WidgetActionbarBnUser:
                showSecondaryMenu();
                break;
            case R.id.WidgetActionbarBnMenu:
                if(getSupportFragmentManager().getBackStackEntryCount()==0){
                    //在主界面
                    toggle();
                }else{
                    getSupportFragmentManager().popBackStack();
                    reFreshActionBarBn(0,true,false);
                }
                break;
        }
    }

    private void refreshNoInfoView() {
        if (Constant.curUser.getBpList()==null || Constant.curUser.getBpList().size()==0) {
            NoInfoViewUtils.showNoInfoView(BPActivity.this, findViewById(R.id.ImgNoInfo));
        }else {
            NoInfoViewUtils.hideNoInfoView(BPActivity.this, findViewById(R.id.ImgNoInfo));
        }
    }

    public void refreshView() {
        ((ProgressBar) findViewById(R.id.BPMainListPro)).setVisibility(View.INVISIBLE);
        processBPList();
        refreshNoInfoView();
    }

    public void processBPList() {
        SwipeMenuListView listView = (SwipeMenuListView) findViewById(R.id.BPMainList);
        if (Constant.curUser.getBpList().size() != 0) {
            LogUtils.d("curUser: "+ Constant.curUser.getUserInfo().getName() + "BP list size: " + Constant.curUser.getBpList().size());
            List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
            ArrayList<BPItem> list = Constant.curUser.getBpList();
            for (BPItem item : list) {
                Map<String, Object> listItem = new HashMap<String, Object>();
                listItem.put("Times", StringMaker.makeTimeString(item.getDate()));
                listItem.put("Dates", StringMaker.makeDateString(item.getDate()));
                listItem.put("Sys", item.getSys() == 0 ? "--" : item.getSys());
                listItem.put("Dia", item.getDia() == 0 ? "--" : item.getDia());
                listItem.put("Pr", item.getPr() == 0 ? "--" : item.getPr());
                listItems.add(listItem);
            }

            simpleAdapter = new SimpleAdapter(BPActivity.this, listItems, R.layout.list_bp,
                    new String[] {"Times", "Dates", "Sys", "Dia", "Pr"},
                    new int[] {R.id.ListBPTextTime, R.id.ListBPTextDate, R.id.ListBPSysValue,
                    R.id.ListBPDiaValue, R.id.ListBPPrValue});

            listView.setAdapter(simpleAdapter);
            listView.setVisibility(View.VISIBLE);
            listView.setMenuCreator(CommonCreator.makeSmallSwipeMenuCreator(getApplicationContext()));
            listView.setOnMenuItemClickListener(this);
        } else {
            listView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
        if (index == 0) {//如果是第0个按键按下
            List<BPItem> list= Constant.curUser.getBpList();
            list.remove(position);
            refreshView();
        }

        return false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            FileUtils.saveListToFile(Constant.dir
                    , Constant.curUser.getUserInfo().getID() + MeasurementConstant.FILE_NAME_BP_LIST_OLD
                    , Constant.curUser.getBpList());
            FileDriver.delFile(Constant.dir, Constant.curUser.getUserInfo().getID() +
                    MeasurementConstant.FILE_NAME_BP_LIST);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return super.onKeyDown(keyCode, event);
        }
        return false;
    }


    @Override
    public void onReadPartFinished(String fileName, byte fileType,
                                   float percentage) {
    }

    @Override
    public void onReadSuccess(String fileName, byte fileType, byte[] fileBuf) {
        //删除旧文件，保存新数据表到本地文件
        FileDriver.delFile(Constant.dir, fileName);
        FileDriver.write(Constant.dir,  fileName, fileBuf);
        MsgUtils.sendMsg(handler, fileType);
    }

//    @Override
//    public void onReadFailed(String fileName, String newFileName, byte fileType, byte errCode) {
//        //读失败同样删除旧文件
//        FileDriver.delFile(Constant.dir, fileName);
//        MsgUtils.sendMsg(handler, fileType, errCode);
//    }

    @Override
    public void onReadFailed(String fileName, byte fileType, byte errCode) {
        //读失败同样删除旧文件
        FileDriver.delFile(Constant.dir, fileName);
        MsgUtils.sendMsg(handler, fileType, errCode);
    }
}

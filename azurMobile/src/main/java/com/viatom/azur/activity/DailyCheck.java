package com.viatom.azur.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.viatom.azur.EventBusEvent.FlushIvEvent;
import com.viatom.azur.bluetooth.BTBinder;
import com.viatom.azur.measurement.BPItem;
import com.viatom.azur.measurement.DailyCheckItem;
import com.viatom.azur.measurement.ECGItem;
import com.viatom.azur.measurement.PedItem;
import com.viatom.azur.measurement.SLMItem;
import com.viatom.azur.measurement.SPO2Item;
import com.viatom.azur.measurement.TempItem;
import com.viatom.azur.measurement.User;
import com.viatom.azur.tools.StringMaker;
import com.viatom.azur.widget.HorizontalProgressBarWithNumber;
import com.viatom.azur.widget.JProgressDialog;
import com.viatom.newazur.R;
import com.viatom.azur.bluetooth.ReadFileListener;
import com.viatom.azur.element.Constant;
import com.viatom.azur.fragment.DailyCheckDetailFragment;
import com.viatom.azur.fragment.DailyCheckFragment;
import com.viatom.azur.fragment.RightMenuFragment;
import com.viatom.azur.measurement.MeasurementConstant;
import com.viatom.azur.tools.PreferenceUtils;
import com.viatom.azur.utils.FileDriver;
import com.viatom.azur.utils.FileUtils;
import com.viatom.azur.utils.LogUtils;
import com.viatom.azur.utils.MsgUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DailyCheck extends BaseActivity implements ReadFileListener {

    Dialog guideDialog;
    Bundle preBundle;
    User[] tempUserList;
    User curDownLoadingUser;
    int fileNum = 0;
    private BTBinder mBinder;
    private ArrayList<String> unDLoadECGNames;
    private ArrayList<String> unDLoadSLMNames;
    private Dialog progressDialog;
    private TextView tv_msg;
    private HorizontalProgressBarWithNumber pb_num;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.MSG_USER_CHOSED:
                    getSlidingMenu().showContent();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            initDailyCheckMainFragment();
                            reFreshActionBarBn(0, true, false);
                            reFreshTitle(Constant.getString(R.string.title_dlc) + " - "
                                    + Constant.curUser.getUserInfo().getName());
                        }
                    }, 300);
                    break;
                case Constant.MSG_GOTO_DLC_DETAIL:
                    preBundle = (Bundle) msg.obj;
                    initDailyCheckDetailFragment((Bundle) msg.obj);
                    reFreshActionBarBn(1, false, true);
                    break;
                case Constant.CMD_TYPE_USER_LIST:
//					readLocalUserList();
                    if (Constant.btConnectFlag) {
                        downLoadList();
                    } else {
                        readLocalUserList();
                    }
                    break;
                case Constant.CMD_TYPE_DLC:
                    processMsg(msg, Constant.CMD_TYPE_DLC);
                    break;
                case Constant.CMD_TYPE_ECG_LIST:
                    processMsg(msg, Constant.CMD_TYPE_ECG_LIST);
                    break;
                case Constant.CMD_TYPE_ECG_NUM:
                    processMsg(msg, Constant.CMD_TYPE_ECG_NUM);
                    break;
                case Constant.CMD_TYPE_SLM_NUM:
                    processMsg(msg, Constant.CMD_TYPE_SLM_NUM);
                    break;
                case Constant.CMD_TYPE_SPO2:
                    processMsg(msg, Constant.CMD_TYPE_SPO2);
                    break;
                case Constant.CMD_TYPE_BP:
                    processMsg(msg, Constant.CMD_TYPE_BP);
                    break;
                case Constant.CMD_TYPE_TEMP:
                    processMsg(msg, Constant.CMD_TYPE_TEMP);
                    break;
                case Constant.CMD_TYPE_SLM_LIST:
                    processMsg(msg, Constant.CMD_TYPE_SLM_LIST);
                    break;
                case Constant.CMD_TYPE_PED:
                    processMsg(msg, Constant.CMD_TYPE_PED);
                    break;
                case Constant.MSG_PART_FINISHED:
                    processMsgPartFinish(msg);
                    break;
            }
        }
    };

    @Override
    public boolean isTheSameIntent(int menuTag) {
        return (menuTag == Constant.MSG_BNDLC_CLICKED);
    }

    protected void processMsgPartFinish(Message msg) {
        if (progressDialog != null) {
            pb_num.setProgress(msg.arg1);
        }

    }

    protected void processMsg(Message msg, int fileType) {
        if (msg == null)
            return;
        if (msg.arg1 == ReadFileListener.ERR_CODE_TIMEOUT) {
            switch (fileType) {
                case Constant.CMD_TYPE_DLC:
                    LogUtils.d("获取dlc列表超时");
                    break;
                case Constant.CMD_TYPE_ECG_LIST:
                    LogUtils.d("获取ecg列表超时");
                    break;
                case Constant.CMD_TYPE_SPO2:
                    LogUtils.d("获取spo2列表超时");
                    break;
                case Constant.CMD_TYPE_TEMP:
                    LogUtils.d("获取tmp列表超时");
                    break;
                case Constant.CMD_TYPE_BP:
                    LogUtils.d("获取nibp列表超时");
                    break;
                case Constant.CMD_TYPE_SLM_LIST:
                    LogUtils.d("获取slm列表超时");
                    break;
                case Constant.CMD_TYPE_ECG_NUM:
                    LogUtils.d("获取ecg-num超时");
                    break;
                case Constant.CMD_TYPE_SLM_NUM:
                    LogUtils.d("获取slm-num超时");
                    break;
                case Constant.CMD_TYPE_PED:
                    LogUtils.d("获取ped超时");
                    break;
            }
            Toast.makeText(this, Constant.getString(R.string.time_out), Toast.LENGTH_SHORT).show();
            // 虽然在线列表获取失败,进入本地
            readLocalUserList();
        } else if (msg.arg1 == ReadFileListener.ERR_CODE_NORMAL) {
            LogUtils.d("列表为空");
            // 虽然在线列表获取失败,进入本地
            processReadDAT(fileType);
        } else {
            processReadDAT(fileType);
        }
    }

    private void processReadDAT(int fileType) {
        switch (fileType) {
            case Constant.CMD_TYPE_DLC:
                readList(Constant.CMD_TYPE_DLC);
                break;
            case Constant.CMD_TYPE_ECG_LIST:
                readList(Constant.CMD_TYPE_ECG_LIST);
                break;
            case Constant.CMD_TYPE_SPO2:
                readList(Constant.CMD_TYPE_SPO2);
                break;
            case Constant.CMD_TYPE_TEMP:
                readList(Constant.CMD_TYPE_TEMP);
                break;
            case Constant.CMD_TYPE_BP:
                readList(Constant.CMD_TYPE_BP);
                break;
            case Constant.CMD_TYPE_SLM_LIST:
                readList(Constant.CMD_TYPE_SLM_LIST);
                break;
            case Constant.CMD_TYPE_ECG_NUM:
                downLoadEcgNum();
                break;
            case Constant.CMD_TYPE_SLM_NUM:
                downLoadSlmNum();
                break;
            case Constant.CMD_TYPE_PED:
                readList(Constant.CMD_TYPE_PED);
                break;
        }
    }

    private void downLoadSlmNum() {
        if (fileNum != unDLoadSLMNames.size() - 1) {
            ++fileNum;
            tv_msg.setText((fileNum + 1+unDLoadECGNames.size()) + "/" +
                    (unDLoadECGNames.size() + unDLoadSLMNames.size()));
            pb_num.setProgress(0);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ReadDatFile(unDLoadSLMNames, fileNum, MeasurementConstant.CMD_TYPE_SLM_NUM);
                }
            }, 150);
        } else {
            endDownload();
        }
    }

    private void downLoadEcgNum() {
        if (fileNum != unDLoadECGNames.size() - 1) {
            ++fileNum;
            tv_msg.setText((fileNum + 1 )+ "/" +
                    (unDLoadECGNames.size() + unDLoadSLMNames.size()));
            pb_num.setProgress(0);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ReadDatFile(unDLoadECGNames, fileNum, MeasurementConstant.CMD_TYPE_ECG_NUM);
                }
            }, 150);
        } else {
            if (unDLoadSLMNames != null && unDLoadSLMNames.size() != 0) {
                fileNum = 0;
                tv_msg.setText((fileNum + 1+unDLoadECGNames.size()) + "/" +
                        (unDLoadECGNames.size() + unDLoadSLMNames.size()));
                pb_num.setProgress(0);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ReadDatFile(unDLoadSLMNames, fileNum, MeasurementConstant.CMD_TYPE_SLM_NUM);
                    }
                }, 150);
            } else {
                endDownload();
            }
        }
    }

    private void endDownload() {
        LogUtils.d(unDLoadECGNames.size()+"and"+unDLoadSLMNames.size());
        fileNum = 0;
        progressDialog.dismiss();
        unDLoadECGNames = null;
        unDLoadSLMNames = null;
        readLocalUserList();
    }

    private void readList(int fileType) {
        switch (fileType) {
            case Constant.CMD_TYPE_DLC:
                if (fileNum != tempUserList.length - 1) {
                    ++fileNum;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ReadDatFile(tempUserList, fileNum, MeasurementConstant.CMD_TYPE_DLC);
                        }
                    }, 150);
                } else {
                    fileNum = 0;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ReadDatFile(tempUserList, fileNum, MeasurementConstant.CMD_TYPE_ECG_LIST);
                        }
                    }, 150);
                }
                break;
            case Constant.CMD_TYPE_ECG_LIST:
                fileNum = 0;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ReadDatFile(tempUserList, fileNum, MeasurementConstant.CMD_TYPE_SPO2);
                    }
                }, 150);
                break;
            case Constant.CMD_TYPE_SPO2:
                fileNum = 0;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ReadDatFile(tempUserList, fileNum, MeasurementConstant.CMD_TYPE_TEMP);
                    }
                }, 150);
                break;
            case Constant.CMD_TYPE_TEMP:
                fileNum = 0;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ReadDatFile(tempUserList, fileNum, MeasurementConstant.CMD_TYPE_BP);
                    }
                }, 150);
                break;
            case Constant.CMD_TYPE_BP:
                if (fileNum != tempUserList.length - 1) {
                    ++fileNum;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ReadDatFile(tempUserList, fileNum, MeasurementConstant.CMD_TYPE_BP);
                        }
                    }, 150);
                } else {
                    fileNum = 0;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ReadDatFile(tempUserList, fileNum, MeasurementConstant.CMD_TYPE_SLM_LIST);
                        }
                    }, 150);
                }
                break;
            case Constant.CMD_TYPE_SLM_LIST:
                fileNum=0;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ReadDatFile(tempUserList, fileNum, MeasurementConstant.CMD_TYPE_PED);
                    }
                }, 150);
                break;
            case Constant.CMD_TYPE_PED:
                if (fileNum != tempUserList.length - 1) {
                    ++fileNum;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ReadDatFile(tempUserList, fileNum, MeasurementConstant.CMD_TYPE_PED);
                        }
                    }, 150);
                } else {
                    fileNum = 0;
                    // 下载列表文件结束,开始读详情文件
                    tempUserList = null;
                    downloadECGNum();
                }
                break;
        }
    }

    private void downLoadList() {
        // 下载
        Constant.userList = FileUtils.readUserList(Constant.dir, MeasurementConstant.FILE_NAME_USER_LIST);
        // 检查设备端是否有删除用户
        if (Constant.userList != null) {
            //下载数据
            JProgressDialog.show(DailyCheck.this);
            tempUserList = Constant.userList;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ReadDatFile(tempUserList, fileNum, MeasurementConstant.CMD_TYPE_DLC);
                }
            }, 100);
        } else {
            processNoUserFile();
            readLocalUserList();
        }
    }

    private void downloadECGNum() {
        if (Constant.userList != null) {
            if (Constant.btConnectFlag) {
                addListToUser();
                if (unDLoadECGNames != null && unDLoadECGNames.size() != 0
                        || unDLoadSLMNames != null && unDLoadSLMNames.size() != 0) {
//                    showDownLoadDialog();
                    downLoadNum();
                } else {
                    readLocalUserList();
                    //TODO
                }
            } else {
                readLocalUserList();
            }
        } else {
            processNoUserFile();
        }
    }

//    private void showDownLoadDialog() {
//        new AlertDialog.Builder(this).setTitle(Constant.getString(R.string.notice))
//                .setMessage(Constant.getString(R.string.download_data))
//                .setPositiveButton(Constant.getString(R.string.download), updateDialogListener)
//                .setNegativeButton(Constant.getString(R.string.later), updateDialogListener).setCancelable(false).show();
//    }
//
//    private DialogInterface.OnClickListener updateDialogListener = new DialogInterface.OnClickListener() {
//
//        @Override
//        public void onClick(DialogInterface arg0, int arg1) {
//            if (arg1 == AlertDialog.BUTTON_POSITIVE) {
//                downLoadNum(arg0);
//            } else if (arg1 == AlertDialog.BUTTON_NEGATIVE) {
//                // 不下载
//                unDownLoadNum(arg0);
//            }
//        }
//
//    };

    private void showProgressDialog() {
        JProgressDialog.cancel();
        progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.wiget_progress);
        tv_msg = (TextView) progressDialog.findViewById(R.id.tv_msg);
        pb_num = (HorizontalProgressBarWithNumber) progressDialog.findViewById(R.id.pb_num);
        pb_num.setMax(100);
        tv_msg.setText(fileNum + 1 + "/" + (unDLoadECGNames.size() + unDLoadSLMNames.size()));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void downLoadNum() {
        showProgressDialog();
        if (unDLoadECGNames != null && unDLoadECGNames.size() != 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ReadDatFile(unDLoadECGNames, fileNum, MeasurementConstant.CMD_TYPE_ECG_NUM);
                }
            }, 150);
        } else if (unDLoadSLMNames != null && unDLoadSLMNames.size() != 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ReadDatFile(unDLoadSLMNames, fileNum, MeasurementConstant.CMD_TYPE_SLM_NUM);
                }
            }, 150);
        }
    }

    private void ReadDatFile(ArrayList<String> names, int fileNum, byte fileType) {
        if (Constant.btConnectFlag && names != null && names.size() != 0) {
            if (mBinder.isAnyThreadRunning()) {
                // mBinder.interfaceInterruptAllThread();
                while (mBinder.isAnyThreadRunning()) {

                }
                selectFileToRead(names, fileNum, fileType);
            } else {
                selectFileToRead(names, fileNum, fileType);
            }
        } else {
            readLocalUserList();
        }
    }

    private void selectFileToRead(ArrayList<String> names, int fileNum, byte fileType) {
        switch (fileType) {
            case Constant.CMD_TYPE_ECG_NUM:
                mBinder.interfaceReadFile(names.get(fileNum), fileType, 5000, this);
                break;
            case Constant.CMD_TYPE_SLM_NUM:
                mBinder.interfaceReadFile(names.get(fileNum), fileType, 5000, this);
                break;

            default:
                break;
        }

    }

    private void unDownLoadNum(DialogInterface arg0) {
        arg0.dismiss();
        readLocalUserList();
    }

    private void addListToUser() {
        unDLoadECGNames = new ArrayList<>();
        unDLoadSLMNames = new ArrayList<>();
        for (int i = 0; i < Constant.userList.length; i++) {
            List<DailyCheckItem> tempDailyCheckItem = FileUtils.readDailyCheckList(Constant.dir,
                    Constant.userList[i].getUserInfo().getID() + MeasurementConstant.FILE_NAME_DLC_LIST);
            if (tempDailyCheckItem != null && tempDailyCheckItem.size() != 0) {
                Constant.userList[i].addDLCList(tempDailyCheckItem);
                for (int j = 0; j < tempDailyCheckItem.size(); j++) {
                    String fileName = StringMaker.makeDateFileName(tempDailyCheckItem.get(j).getDate(),
                            Constant.CMD_TYPE_ECG_NUM);
                    if (!FileDriver.isFileExist(Constant.dir, fileName)) {
                        unDLoadECGNames.add(fileName);
                    }
                }
            }
            List<BPItem> tempBpItem = FileUtils.readBPList(Constant.dir,
                    Constant.userList[i].getUserInfo().getID() + MeasurementConstant.FILE_NAME_BP_LIST);
            if (tempBpItem != null && tempBpItem.size() != 0) {
                Constant.userList[i].addBPList(tempBpItem);
            }

            List<PedItem> tempPedItem = FileUtils.readPedList(Constant.dir,
                    Constant.userList[i].getUserInfo().getID() + MeasurementConstant.FILE_NAME_PED_LIST);
            if (tempPedItem != null && tempPedItem.size() != 0) {
                Constant.userList[i].addPedList(tempPedItem);
            }
        }
        List<ECGItem> tempECGItem = FileUtils.readECGList(Constant.dir,MeasurementConstant.FILE_NAME_ECG_LIST);
        if (tempECGItem != null && tempECGItem.size() != 0) {
            Constant.userList[0].addECGList(tempECGItem);
            for (int j = 0; j < tempECGItem.size(); j++) {
                String fileName = StringMaker.makeDateFileName(tempECGItem.get(j).getDate(),
                        Constant.CMD_TYPE_ECG_NUM);
                if (!FileDriver.isFileExist(Constant.dir, fileName)) {
                    unDLoadECGNames.add(fileName);
                }
            }
        }
        List<SPO2Item> tempOxiItem = FileUtils.readSPO2List(Constant.dir,MeasurementConstant.FILE_NAME_SPO2_LIST);
        if (tempOxiItem != null && tempOxiItem.size() != 0) {
            Constant.userList[0].addSPO2List(tempOxiItem);
        }
        List<TempItem> tempTMPItem = FileUtils.readTempList(Constant.dir, MeasurementConstant.FILE_NAME_TEMP_LIST);
        if (tempTMPItem != null && tempTMPItem.size() != 0) {
            Constant.userList[0].addTempList(tempTMPItem);
        }
        List<SLMItem> tempSLMItem = FileUtils.readSLMList(Constant.dir,MeasurementConstant.FILE_NAME_SLM_LIST);
        if (tempSLMItem != null && tempSLMItem.size() != 0) {
            Constant.userList[0].addSLMList(tempSLMItem);
            for (int j = 0; j < tempSLMItem.size(); j++) {
                String fileName = StringMaker.makeDateFileName(tempSLMItem.get(j).getDate(),
                        Constant.CMD_TYPE_SLM_NUM);
                if (!FileDriver.isFileExist(Constant.dir, fileName)) {
                    unDLoadSLMNames.add(fileName);
                }
            }
        }
    }

    private void ReadDatFile(User[] userList, int fileNum, byte fileType) {
        if (Constant.btConnectFlag && userList != null && userList.length != 0) {
            if (mBinder.isAnyThreadRunning()) {
                // mBinder.interfaceInterruptAllThread();
                while (mBinder.isAnyThreadRunning()) {

                }
                selectFileToRead(userList, fileNum, fileType);
            } else {
                selectFileToRead(userList, fileNum, fileType);
            }
        } else {
            readLocalUserList();
        }
    }

    private void selectFileToRead(User[] userList, int fileNum, byte fileType) {
        LogUtils.d(userList[fileNum].getUserInfo().getName() + "");
        switch (fileType) {
            case Constant.CMD_TYPE_DLC:
                String bdcfileName = userList[fileNum].getUserInfo().getID() + MeasurementConstant.FILE_NAME_DLC_LIST;
                curDownLoadingUser = userList[fileNum];
                mBinder.interfaceReadFile(bdcfileName, fileType, 5000, this);
                break;
            case Constant.CMD_TYPE_ECG_LIST:
                String ecgfileName =  MeasurementConstant.FILE_NAME_ECG_LIST;
                curDownLoadingUser = userList[fileNum];
                mBinder.interfaceReadFile(ecgfileName, fileType, 5000, this);
                break;
            case Constant.CMD_TYPE_SPO2:
                String oxifileName =  MeasurementConstant.FILE_NAME_SPO2_LIST;
                curDownLoadingUser = userList[fileNum];
                mBinder.interfaceReadFile(oxifileName, fileType, 5000, this);
                break;
            case Constant.CMD_TYPE_TEMP:
                String tmpfileName =  MeasurementConstant.FILE_NAME_TEMP_LIST;
                curDownLoadingUser = userList[fileNum];
                mBinder.interfaceReadFile(tmpfileName, fileType, 5000, this);
                break;
            case Constant.CMD_TYPE_BP:
                String nibpfileName = userList[fileNum].getUserInfo().getID() + MeasurementConstant.FILE_NAME_BP_LIST;
                curDownLoadingUser = userList[fileNum];
                mBinder.interfaceReadFile(nibpfileName, fileType, 5000, this);
                break;
            case Constant.CMD_TYPE_SLM_LIST:
                String hrvfileName =  MeasurementConstant.FILE_NAME_SLM_LIST;
                curDownLoadingUser = userList[fileNum];
                mBinder.interfaceReadFile(hrvfileName, fileType, 5000, this);
                break;
            case Constant.CMD_TYPE_PED:
                String pedfileName =  userList[fileNum].getUserInfo().getID() +MeasurementConstant.FILE_NAME_PED_LIST;
                curDownLoadingUser = userList[fileNum];
                mBinder.interfaceReadFile(pedfileName, fileType, 5000, this);
                break;
            default:
                break;
        }

    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFlushIvEvent(FlushIvEvent event) {
        initBtnCloud(event.getUploadState());
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_check);
        EventBus.getDefault().register(this);
        mBinder=Constant.binder;
        //没有用户列表的时候获取用户列表
        if (Constant.userList == null) {
            getUserList();
        } else {
            setSlidingMenu();
            initDailyCheckMainFragment();
            reFreshActionBarBn(0, true, false);
            reFreshTitle(Constant.getString(R.string.title_dlc) + " - "
                    + Constant.curUser.getUserInfo().getName());
        }
        showGuide();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void reloadPreUserIndex() {

        int preUserIndex = PreferenceUtils.readIntPreferences(
                getApplicationContext(), "PreUserIndex");
        if (preUserIndex > Constant.userList.length - 1) {//防止主机删除用户后越界
            preUserIndex = 0;
        }
        Constant.curUser = Constant.userList[preUserIndex];
    }

    public void getUserList() {
        if (Constant.btConnectFlag) {
            mBinder.interfaceReadFile("usr.dat", Constant.CMD_TYPE_USER_LIST, 5000, this);

        } else {
            readLocalUserList();
        }
    }

    private void readLocalUserList() {
        JProgressDialog.cancel();
        Constant.userList = FileUtils.readUserList(Constant.dir
                , MeasurementConstant.FILE_NAME_USER_LIST);
        if (Constant.userList != null) {// 存在用户列表文件
            Constant.defaultUser = Constant.userList[0];
            Constant.curUser = Constant.defaultUser;
            reloadPreUserIndex();
            setSlidingMenu();
            initDailyCheckMainFragment();
            reFreshActionBarBn(0, true, false);
            reFreshTitle(Constant.getString(R.string.title_dlc) + " - "
                    + Constant.curUser.getUserInfo().getName());
            uploadToCloud();
        } else {// 没有用户文件
            processNoUserFile();
        }
    }

    public void processNoUserFile() {
        JProgressDialog.cancel();
        User u = new User();
        Constant.defaultUser=u;
        Constant.curUser=u;
        findViewById(R.id.ImgNoInfo).setVisibility(View.VISIBLE);
        setSlidingMenu();
        reFreshActionBarBn(0, true, false);
        reFreshTitle(Constant.getString(R.string.title_dlc) + " - "
                + Constant.curUser.getUserInfo().getName());
//        new AlertDialog.Builder(DailyCheck.this)
//                .setTitle(Constant.getString(R.string.warning))
//                .setMessage(Constant.getString(R.string.no_user_info)).setCancelable(false)
//                .setPositiveButton(Constant.getString(R.string.exit),
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog,
//                                                int which) {
//                                finish();
//                            }
//                        })
//                .show();
    }

    public void initDailyCheckMainFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();//warning test
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DailyCheckFragment fragment = new DailyCheckFragment();
        fragment.setArguments(handler);
        fragmentTransaction.replace(R.id.DailyCheckReMain, fragment);
        fragmentTransaction.commit();
    }

    public void initDailyCheckDetailFragment(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();//warning test
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DailyCheckDetailFragment fragment = new DailyCheckDetailFragment();
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.DailyCheckReMain, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /**
     * 设置右边用户列表
     */
    public void setSlidingMenu() {
        SlidingMenu sm = getSlidingMenu();
        sm.setMode(SlidingMenu.LEFT_RIGHT);
        sm.setSecondaryMenu(R.layout.menu_frame_two);
        sm.setSecondaryShadowDrawable(R.drawable.shadowright);

        RightMenuFragment rmf = new RightMenuFragment();
        rmf.setmHandler(handler);
        rmf.setUserList();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.menu_frame_two, rmf)
                .commit();
    }


    @Override
    public void onReadPartFinished(String fileName, byte fileType, float percentage) {
        // TODO Auto-generated method stub
        LogUtils.d(fileName + "部分完成  " + percentage);
        MsgUtils.sendMsg(handler, Constant.MSG_PART_FINISHED, (int) (percentage * 100));
    }

    @Override
    public void onReadSuccess(String fileName, byte fileType, byte[] fileBuf) {
        LogUtils.d(fileName + "下载成功  " + fileBuf);
        //删除旧文件，保存新数据表到本地文件
        FileDriver.delFile(Constant.dir, fileName);
        FileDriver.write(Constant.dir, fileName, fileBuf);
        MsgUtils.sendMsg(handler, fileType);
    }

    @Override
    public void onReadFailed(String fileName, byte fileType, byte errCode) {
        LogUtils.d(fileName + "下载失败  " + errCode);
        MsgUtils.sendMsg(handler, fileType, errCode);
    }

    protected void onBnShareClicked() {
        LogUtils.d("share键按下");
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments.size() >= 3) {
            int index = fragments.size() - 1;
            DailyCheckDetailFragment detailFragment = (DailyCheckDetailFragment) fragments.get(index);
            detailFragment.showShareAlertView();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    //主界面
                    initDailyCheckMainFragment();
                } else {
                    //详情界面
                    initDailyCheckDetailFragment(preBundle);
                }
            }
        }, 20);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.WidgetActionbarBnUser:
                showSecondaryMenu();
                //切换用户前保存当前dlc列表
                saveCurUserDLCList();
                break;
            case R.id.WidgetActionbarBnMenu:
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    //在主界面
                    toggle();
                } else {
                    getSupportFragmentManager().popBackStack();
                    String userName = "";
                    if(Constant.curUser != null && Constant.curUser.getUserInfo() != null) {
                        userName = Constant.curUser.getUserInfo().getName();
                        if(!TextUtils.isEmpty(userName)) {
                            userName = userName.trim();
                        }
                    }
                    if(!TextUtils.isEmpty(userName)) {
                        reFreshTitle(Constant.getString(R.string.title_dlc) + " - " + userName);
                    } else {
                        reFreshTitle(Constant.getString(R.string.title_dlc));
                    }
                    reFreshActionBarBn(0, true, false);
                }
                break;
            case R.id.WidgetActionbarBnShare:
                onBnShareClicked();
                break;
            case R.id.WidgetActionbarBnCloud:
                showSettingActivity();
                break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            //离开act时保存当前用户列表
            saveCurUserDLCList();
        }
    }

    /**
     * 保存当前用户dlc列表
     */
    private void saveCurUserDLCList() {
        if (Constant.curUser == null) {
            return;
        }
        LogUtils.d("保存dlc列表，用户ID：" + Constant.curUser.getUserInfo().getID());
        FileUtils.saveListToFile(Constant.dir
                , Constant.curUser.getUserInfo().getID()
                        + MeasurementConstant.FILE_NAME_DLC_LIST_OLD
                , Constant.curUser.getDlcList());
    }

    /**
     * 初次使用时显示指引
     */
    private void showGuide() {
        //如果读到已经显示过guide了，直接返回
        if (PreferenceUtils.readBoolPreferences(getApplicationContext(), "Guide1")) {
            return;
        }
        //保存状态位，下次不再显示
        PreferenceUtils.savePreferences(getApplicationContext(), "Guide1", true);

        //初始化guide对话框
        if (guideDialog != null && guideDialog.isShowing()) {
            guideDialog.cancel();
        }
        guideDialog = new Dialog(this, R.style.Dialog_Fullscreen);
        guideDialog.setContentView(R.layout.wiget_guide);
        ImageView iv = (ImageView) guideDialog.findViewById(R.id.iv_start);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (guideDialog != null) {
                    guideDialog.dismiss();
                }
            }
        });
        guideDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                return super.onKeyDown(keyCode, event);
            } else {
                getSupportFragmentManager().popBackStack();
                String userName = "";
                if(Constant.curUser != null && Constant.curUser.getUserInfo() != null) {
                    userName = Constant.curUser.getUserInfo().getName();
                    if(!TextUtils.isEmpty(userName)) {
                        userName = userName.trim();
                    }
                }
                if(!TextUtils.isEmpty(userName)) {
                    reFreshTitle(Constant.getString(R.string.title_dlc) + " - " + userName);
                } else {
                    reFreshTitle(Constant.getString(R.string.title_dlc));
                }
                reFreshActionBarBn(0, true, false);
            }
        }
        return false;
    }

}

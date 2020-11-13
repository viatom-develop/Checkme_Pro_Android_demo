package com.checkme.azur.fragment;

import org.xutils.common.Callback;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.checkme.azur.element.UpdatePatch;
import com.checkme.azur.internet.DownloadUtils;
import com.checkme.azur.utils.StringUtils;
import com.checkme.newazur.R;
import com.checkme.azur.bluetooth.BTConstant;
import com.checkme.azur.bluetooth.WriteFileListener;
import com.checkme.azur.element.AppPatch;
import com.checkme.azur.element.CheckmeDevice;
import com.checkme.azur.element.Constant;
import com.checkme.azur.element.LanguagePatch;
import com.checkme.azur.internet.FileDownloadTask;
import com.checkme.azur.utils.FileDriver;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.utils.MsgUtils;

public class CheckmeUpdateFragment extends Fragment implements WriteFileListener {

    private static final String APP_PATCH_NAME = "app.bin";
    private static final String LANGUAGE_PATCH_NAME = "language.bin";
    public static final int PATCH_TYPE_APP = 1;
    public static final int PATCH_TYPE_LANGUAGE = 2;
    public static final byte IMG_STATE_DOWNLOADING = 1;
    public static final byte IMG_STATE_WRITING = 2;
    private View layout;
    private CheckmeDevice checkmeInfo;
    private Handler callerHandler;
    private String wantLanguage;
    private UpdatePatch updatePatch;
    private TextView textState;
    private ProgressBar progressBar;
    private int appPro = 0, languagePro = 0;//两包的下载进度
    private FileDownloadTask appDownloadTask, languageDownloadTask;
    private Callback.Cancelable mAPPCancelable;
    private Callback.Cancelable mLANCancelable;
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

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.MSG_DOWNLOAD_PART_FINISH:
                    if (msg.arg1 == PATCH_TYPE_APP) {
                        appPro = msg.arg2;
                    } else if (msg.arg1 == PATCH_TYPE_LANGUAGE) {
                        languagePro = msg.arg2;
                    }
                    progressBar.setProgress(appPro + languagePro);
//                    if (progressBar.getProgress() == progressBar.getMax()) {
//                        progressBar.setProgress(0);
//                        if (getActivity() != null) {//防止退出后仍提示
//                            Toast.makeText(getActivity(), Constant.getString(
//                                    R.string.download_successfully), Toast.LENGTH_SHORT).show();
//                        }
//                        startWritePatchsToCheckme();
//                        refreshStateImg(IMG_STATE_WRITING);
//                    }
                    if (progressBar.getProgress() == 200) {
                        progressBar.setProgress(0); //数据初始化。
                        if (getActivity() != null) {//防止退出后仍提示
                            Toast.makeText(getActivity(), Constant.getString(
                                    R.string.download_successfully), Toast.LENGTH_SHORT).show();
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startWritePatchsToCheckme();
                                refreshStateImg(IMG_STATE_WRITING);
                            }
                        }, 1000);
//                        startWritePatchsToCheckme();
//                        refreshStateImg(IMG_STATE_WRITING);
                    }
                    break;
                case Constant.MSG_PART_FINISHED:
                    languagePro = msg.arg1;//进来默认当事lang的进度
                    progressBar.setProgress(languagePro + appPro);
                    if (languagePro + appPro == 100) {
                        //双刷写入完一半或者单刷一个的时候写完了
                        appPro = 100;
                        languagePro = 0;
                    }
                    break;
                case BTConstant.CMD_WORD_LANG_UPDATE_START:
                    //语言包写入完了
                    onWriteLanguageFinished();
                    break;
                case BTConstant.CMD_WORD_APP_UPDATE_START:
                    //APP包写入完了
                    MsgUtils.sendMsg(callerHandler, Constant.MSG_SHOW_UPDATE_SUCCESS);
                    break;
                default:
                    break;
            }
        }

        ;
    };

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        layout = inflater.inflate(R.layout.fragment_checkme_update, container, false);
        initUI();
        new GetPatchsThread(checkmeInfo, wantLanguage, updatePatch).start();
        return layout;
    }

    public void onResume() {
        super.onResume();
//        MobclickAgent.onPageStart("Checkme Update"); //统计页面
    }

    public void onPause() {
        super.onPause();
//        MobclickAgent.onPageEnd("Checkme Update");
    }

    public void setArguments(CheckmeDevice checkmeInfo, String wantLanguage, Handler handler, UpdatePatch updatePatch) {
        this.checkmeInfo = checkmeInfo;
        this.callerHandler = handler;
        this.wantLanguage = wantLanguage;
        this.updatePatch = updatePatch;
    }

    private void initUI() {
        progressBar = (ProgressBar) layout.findViewById(R.id.FragmentCheckmeUpdatePro);
        textState = (TextView) layout.findViewById(R.id.FragmentCheckmeUpdateTextState);
    }

    /**
     * @author zouhao
     *         升级包地址获取线程
     */
    public class GetPatchsThread extends Thread {
        private CheckmeDevice checkmeInfo;
        private String wantLanguage;
        private UpdatePatch updatePatch;

        public GetPatchsThread(CheckmeDevice checkmeInfo, String wantLanguage, UpdatePatch updatePatch) {
            super();
            this.checkmeInfo = checkmeInfo;
            this.wantLanguage = wantLanguage;
            this.updatePatch = updatePatch;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (checkmeInfo == null || wantLanguage == null || updatePatch == null) {
                return;
            }
            try {
//				//获取升级包下载地址
//				JSONObject jsonObject = PostUtils.doPost(Constant.URL_GET_CHECKME_PATCH
//						, PostInfoMaker.makeGetCheckmePatchInfo(checkmeInfo, wantLanguage));
//				if (jsonObject == null) {
//					//获取错误
//					MsgUtils.sendMsg(callerHandler, Constant.MSG_SHOW_UPDATE_FAILED);
//				}else {
//					if (jsonObject.getString("Result").equals("NULL")) {
//						MsgUtils.sendMsg(callerHandler, Constant.MSG_SHOW_UPDATE_FAILED);
//					}else {
//						//判断App包和语言包版本，并下载
//						AppPatch appPatch = new AppPatch(jsonObject);
//						LanguagePatch languagePatch = new LanguagePatch(jsonObject);
//						if (appPatch.getDependLanguageVersion() != languagePatch.getVersion()) {
//							//两包版本不对应,升级失败
//							MsgUtils.sendMsg(callerHandler, Constant.MSG_SHOW_UPDATE_FAILED);
//						}else {
//							//先删除旧的本地升级包，再下载新升级包
//							deleteOldPatchs();
//							downloadPatchs(appPatch, languagePatch);
//						}
//					}
//				}
                if (StringUtils.isUpdateAvailable(updatePatch.getVersion(), makeVersion(checkmeInfo.getSoftware()))) {
                    deleteOldPatchs();
                    downloadPatchs(updatePatch, wantLanguage);
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                MsgUtils.sendMsg(callerHandler, Constant.MSG_SHOW_UPDATE_FAILED);
            }

        }
    }

    private void downloadPatchs(UpdatePatch updatePatch, String wantLanguage) {
        if (updatePatch == null) {
            Toast.makeText(mContext.getApplicationContext(), Constant.getString(R.string.download_failed)
                    , Toast.LENGTH_SHORT).show();
            return;
        }
        LogUtils.d(wantLanguage);
        //清零两个进度
        appPro = 0;
        languagePro = 0;
        //④app包不同，两个都升
        LogUtils.d("两个都升");
        progressBar.setMax(200);
        progressBar.setProgress(0);
        mAPPCancelable = DownloadUtils.downloadPatch(updatePatch.getFileLocate(),
                Constant.dir + "/" + APP_PATCH_NAME, PATCH_TYPE_APP, handler);
        mLANCancelable = DownloadUtils.downloadPatch(updatePatch.getLanLocate().get(wantLanguage),
                Constant.dir + "/" + LANGUAGE_PATCH_NAME, PATCH_TYPE_LANGUAGE, handler);
    }

    /**
     * 生成版本号
     *
     * @param software
     * @return
     */
    private String makeVersion(int software) {
        if (software <= 0) {
            return "--";
        }
        String version = new String();
        version += software / 10000 + ".";
        version += (software % 10000) / 100 + ".";
        version += software % 100;
        LogUtils.d(version);
        return version;
    }

    /**
     * 下载升级包
     * 根据包信息和主机信息选择要下载的包
     *
     * @param appPatch
     * @param languagePatch
     */
    private void downloadPatchs(AppPatch appPatch, LanguagePatch languagePatch) {
        if (appPatch == null || languagePatch == null) {
            Toast.makeText(mContext.getApplicationContext(), Constant.getString(R.string.download_failed)
                    , Toast.LENGTH_SHORT).show();
            return;
        }
        //清零两个进度
        appPro = 0;
        languagePro = 0;

        if (languagePatch.getVersion() == checkmeInfo.getLanguage()) {
            //语言包版本相同，继续判断
            if (languagePatch.getLanguages().contains(checkmeInfo.getCurLanguage())) {
                //语言包中包含当前语言，至少不用升语言包
                if (appPatch.getVersion() == checkmeInfo.getSoftware()) {
                    //①app包也相同，两个都不升
                    LogUtils.d("两个都不升");
                    MsgUtils.sendMsg(callerHandler, Constant.MSG_SHOW_UNNECESSARY_UPDATE);
                    MsgUtils.sendMsg(callerHandler, Constant.MSG_BACK_TO_LAST_FRAG);
                } else {
                    //②app包不同，只升App
                    LogUtils.d("只升APP");
                    appDownloadTask = new FileDownloadTask(Constant.SERVER_ADDRESS + appPatch.getAddress(),
                            6, Constant.dir + "/" + APP_PATCH_NAME, PATCH_TYPE_APP, handler);
                    appDownloadTask.start();
                    progressBar.setMax(100);
                }
            } else {
                //语言包中包不含当前语言，至少要升语言包
                if (appPatch.getVersion() == checkmeInfo.getSoftware()) {
                    //③app包相同，只升语言包
                    LogUtils.d("只升语言包");
                    languageDownloadTask = new FileDownloadTask(Constant.SERVER_ADDRESS + languagePatch.getAddress(),
                            6, Constant.dir + "/" + LANGUAGE_PATCH_NAME, PATCH_TYPE_LANGUAGE, handler);
                    languageDownloadTask.start();
                    progressBar.setMax(100);
                } else {
                    //④app包不同，两个都升
                    LogUtils.d("两个都升1");
                    appDownloadTask = new FileDownloadTask(Constant.SERVER_ADDRESS + appPatch.getAddress(),
                            6, Constant.dir + "/" + APP_PATCH_NAME, PATCH_TYPE_APP, handler);
                    appDownloadTask.start();
                    languageDownloadTask = new FileDownloadTask(Constant.SERVER_ADDRESS + languagePatch.getAddress(),
                            6, Constant.dir + "/" + LANGUAGE_PATCH_NAME, PATCH_TYPE_LANGUAGE, handler);
                    languageDownloadTask.start();
                    progressBar.setMax(200);
                }
            }
        } else {
            //⑤语言包版本不同，两个都升
            LogUtils.d("两个都升2");
            appDownloadTask = new FileDownloadTask(Constant.SERVER_ADDRESS + appPatch.getAddress(),
                    6, Constant.dir + "/" + APP_PATCH_NAME, PATCH_TYPE_APP, handler);
            appDownloadTask.start();
            languageDownloadTask = new FileDownloadTask(Constant.SERVER_ADDRESS + languagePatch.getAddress(),
                    6, Constant.dir + "/" + LANGUAGE_PATCH_NAME, PATCH_TYPE_LANGUAGE, handler);
            languageDownloadTask.start();
            progressBar.setMax(200);
        }
    }

    /**
     * 开始写升级包到Checkme
     */
    private void startWritePatchsToCheckme() {
        if (!Constant.btConnectFlag) {
            LogUtils.d("蓝牙已断开，无法写入升级");
            MsgUtils.sendMsg(callerHandler, Constant.MSG_SHOW_CANT_UPDATE_IN_OFFLINE);
            return;
        }
        //修改状态字符
        textState.setText(Constant.getString(R.string.updating));

        appPro = 0;
        languagePro = 0;

        //去本地读两个包
        if (FileDriver.isFileExist(Constant.dir, LANGUAGE_PATCH_NAME)) {
            //有语言包，先升语言包，成功后发消息到handler处理继续升app
            if (FileDriver.isFileExist(Constant.dir, APP_PATCH_NAME)) {
                //同时有APP包，进度条设200
                progressBar.setMax(200);
                LogUtils.d("两个包都写");
            } else {
                //只有语言包，进度条100
                progressBar.setMax(100);
                LogUtils.d("只写语言包");
            }
            //测试
            byte[] fileBuf = FileDriver.read(Constant.dir, LANGUAGE_PATCH_NAME);
            Constant.binder.interfaceWriteFile(LANGUAGE_PATCH_NAME, fileBuf
                    , BTConstant.CMD_WORD_LANG_UPDATE_START, 100000, this);
        } else {
            //如果没有语言包，直接升APP，进度条设100
            if (FileDriver.isFileExist(Constant.dir, APP_PATCH_NAME)) {
                progressBar.setMax(100);
                LogUtils.d("只写APP包");
                byte[] fileBuf = FileDriver.read(Constant.dir, APP_PATCH_NAME);
                Constant.binder.interfaceWriteFile(APP_PATCH_NAME, fileBuf
                        , BTConstant.CMD_WORD_APP_UPDATE_START, 100000, this);
            }
        }
    }

    /**
     * 语言包写完后处理函数
     * 如果有APP，继续写APP包
     */
    private void onWriteLanguageFinished() {
        if (!Constant.btConnectFlag) {
            LogUtils.d("蓝牙已断开，无法写入升级");
            MsgUtils.sendMsg(callerHandler, Constant.MSG_SHOW_CANT_UPDATE_IN_OFFLINE);
            return;
        }
        if (FileDriver.isFileExist(Constant.dir, APP_PATCH_NAME)) {
            byte[] fileBuf = FileDriver.read(Constant.dir, APP_PATCH_NAME);
            Constant.binder.interfaceWriteFile(APP_PATCH_NAME, fileBuf
                    , BTConstant.CMD_WORD_APP_UPDATE_START, 100000, this);
        } else {
            //没有app包 升级完成
            MsgUtils.sendMsg(callerHandler, Constant.MSG_SHOW_CHANGE_LANGUAGE_SUCCESS);
        }
    }

    /**
     * 删除旧的本地补丁
     */
    private void deleteOldPatchs() {
        if (FileDriver.isFileExist(Constant.dir, LANGUAGE_PATCH_NAME)) {
            FileDriver.delFile(Constant.dir, LANGUAGE_PATCH_NAME);
        }
        if (FileDriver.isFileExist(Constant.dir, APP_PATCH_NAME)) {
            FileDriver.delFile(Constant.dir, APP_PATCH_NAME);
        }
    }

    /**
     * 刷新状态图片
     *
     * @param curState
     */
    private void refreshStateImg(byte curState) {
        ImageView imgDownloading = (ImageView) layout.findViewById
                (R.id.FragmentCheckmeUpdateImgCloud);
        ImageView imgWriting = (ImageView) layout.findViewById
                (R.id.FragmentCheckmeUpdateImgWriting);

        if (curState == IMG_STATE_DOWNLOADING) {
            imgDownloading.setVisibility(View.VISIBLE);
            imgWriting.setVisibility(View.INVISIBLE);
        } else if (curState == IMG_STATE_WRITING) {
            imgDownloading.setVisibility(View.INVISIBLE);
            imgWriting.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onWritePartFinished(String fileName, byte fileType, float percentage) {
        // TODO Auto-generated method stub
        if (fileType == BTConstant.CMD_WORD_LANG_UPDATE_START) {
            LogUtils.d("lang包进度" + percentage * 100);
            MsgUtils.sendMsg(handler, Constant.MSG_PART_FINISHED
                    , (int) (percentage * 100));
        } else if (fileType == BTConstant.CMD_WORD_APP_UPDATE_START) {
            LogUtils.d("app包进度" + percentage * 100);
            MsgUtils.sendMsg(handler, Constant.MSG_PART_FINISHED
                    , (int) (percentage * 100));
        }
    }

    @Override
    public void onWriteSuccess(String fileName, byte fileType) {
        // TODO Auto-generated method stub
        MsgUtils.sendMsg(handler, fileType);
    }

    @Override
    public void onWriteFailed(byte fileType, byte errCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        LogUtils.d("CheckmeUpadateFragment Destroy!");
        if (appDownloadTask != null) {
            LogUtils.d("停止app下载线程");
            appDownloadTask.interrupt();
        }
        if (languageDownloadTask != null) {
            LogUtils.d("停止language下载线程");
            languageDownloadTask.interrupt();
        }
        if (!mAPPCancelable.isCancelled())
            mAPPCancelable.cancel();
        if (!mLANCancelable.isCancelled())
            mLANCancelable.cancel();
        if (Constant.binder != null) {
            Constant.binder.interfaceInterruptAllThread();
        }
        super.onDestroy();
    }
}

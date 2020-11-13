package com.viatom.azur.element;

import android.app.Application;
import android.os.Environment;

import com.viatom.azur.utils.LogUtils;
import com.viatom.azur.utils.RegisterReceiverUtils;
import com.viatom.newazur.BuildConfig;

import org.xutils.DbManager;
import org.xutils.x;

import java.io.File;

public class CEApplication extends Application {
    private DbManager.DaoConfig daoConfig;
    private boolean policy_flag = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Constant.init(this);
        RegisterReceiverUtils.registerConnectionChangeReceiver(this);
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG);
        daoConfig = new DbManager.DaoConfig()
                .setDbName(Constant.DB_ADDRESS)
                .setDbDir(initDir())
                .setDbVersion(1)
                .setDbOpenListener(new DbManager.DbOpenListener() {
                    @Override
                    public void onDbOpened(DbManager db) {
                        // 开启WAL, 对写入加速提升巨大
                        db.getDatabase().enableWriteAheadLogging();
                    }
                })
                .setAllowTransaction(true)
                .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                    @Override
                    public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                        //TODO
//                        try {
//                            db.addColumn(SleepData.class,"mIsUploaded");
//                        } catch (DbException e) {
//                            e.printStackTrace();
//                        }
//                        try {
//                            db.dropDb();
//                        } catch (DbException e) {
//                            e.printStackTrace();
//                        }
                    }
                });
    }

    private File initDir() {
        File root = Environment.getExternalStorageDirectory();
        if (root == null) {
            root = Environment.getDataDirectory();
        }
        File dir = new File(root, "CheckmeMobile");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                LogUtils.d("Create dir failed");
            }
        }
        LogUtils.d("Current dir:" + dir.toString());
        return dir;
    }


    public DbManager.DaoConfig getDaoConfig() {
        return daoConfig;
    }

    public boolean getPolicyFlag() {
        return policy_flag;
    }
    public void setPolicyFlag(boolean policy_flag) {
        this.policy_flag = policy_flag;
    }
}

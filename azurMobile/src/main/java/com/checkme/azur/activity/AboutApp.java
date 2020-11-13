package com.checkme.azur.activity;

import java.io.File;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.checkme.newazur.R;
import com.checkme.azur.element.CheckmeMobilePatch;
import com.checkme.azur.element.Constant;
import com.checkme.azur.internet.FileDownloadTask;
import com.checkme.azur.tools.CheckmeMobileUpdateUtils;
import com.checkme.azur.tools.NetWorkUtils;
import com.checkme.azur.tools.ToastUtils;
import com.checkme.azur.utils.FileDriver;
import com.checkme.azur.utils.LogUtils;

public class AboutApp extends BaseActivity implements OnClickListener{

	private ProgressDialog dialog;
	private FileDownloadTask downloadTask;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
				case Constant.MSG_CHECKME_MOBILE_PATCH_EXIST:
					CheckmeMobilePatch patch = (CheckmeMobilePatch)msg.obj;
					setBnUpdate(true, patch.getAddress());
					break;
				case Constant.MSG_CHECKME_MOBILE_PATCH_NOT_EXIST:
					setBnUpdate(false, null);
					break;
				case Constant.MSG_DOWNLOAD_PART_FINISH:
					processPatch(msg.arg2);
					break;
				case Constant.MSG_DOWNLOAD_FAILED:
					processUpdateFailed();
					break;
				default:
					break;
			}
		};
	};

	@Override
	public boolean isTheSameIntent(int menuTag) {
		return (menuTag == Constant.MSG_BNABOUT_APP_CLICKED);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_app);
		initUI();

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			String patchAddress = bundle.getString("PatchAddress");
			if (patchAddress != null) {
				//有地址，直接升级
				showUpdateDialog();
				startUpdate(patchAddress);
			}
		}else {
			//没地址，正常显示
			checkUpdate();
		}

	}

	public void initActionBar() {
		super.initActionBar();
	}


	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void initUI() {
		reFreshTitle(Constant.getString(R.string.title_about));
		reFreshActionBarBn(0,false,false);
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			TextView textVersion = (TextView)findViewById(R.id.tv_version_value);
			textVersion.setText(info.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		TextView tv_disclaimer_item = (TextView) findViewById(R.id.tv_disclaimer_item);
		tv_disclaimer_item.setOnClickListener(this);
	}

	/**
	 * 检查是否有升级
	 */
	private void checkUpdate() {
		if (!NetWorkUtils.isNetWorkAvailable(getApplicationContext())) {
			return;
		}
		CheckmeMobileUpdateUtils.getPatch(this, handler);
	}

	/**
	 * 刷新升级键状态
	 * @param available
	 */
	private void setBnUpdate(boolean available, String patchUrl) {
		Button bnUpdate = (Button)findViewById(R.id.bn_update);
		if (available) {
			bnUpdate.setVisibility(View.VISIBLE);
			bnUpdate.setOnClickListener(this);
			//将地址存储在bnTag里面
			bnUpdate.setTag(patchUrl);
		}else {
			bnUpdate.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 *  显示升级进度框
	 */
	private void showUpdateDialog() {
		dialog = new ProgressDialog(AboutApp.this);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setTitle(Constant.getString(R.string.notice));
		dialog.setMessage(Constant.getString(R.string.downloading_app));
		dialog.setCancelable(false);
		dialog.setButton(Constant.getString(R.string.cancel)
				, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						if (downloadTask != null) {
							downloadTask.interrupt();
						}
						//没有用户(直接栋mainAct进来的) 则到dlc里获取
						if (Constant.defaultUser == null) {
							Intent intent = new Intent();
							intent.setClass(AboutApp.this,DailyCheck.class);
							startActivity(intent);
							finish();
						}
					}
				});
		dialog.show();
	}

	/**
	 * 开始升级（下载升级包）
	 * @param urlStr
	 */
	private void startUpdate(String urlStr) {

		if (urlStr == null) {
			return;
		}
		//终止之前的下载
		if (downloadTask != null) {
			downloadTask.interrupt();
		}
		//删除旧文件
		FileDriver.delFile(Constant.dir, "CheckmeMobile.apk");
		//下载
		downloadTask = new FileDownloadTask(urlStr, 6,
				Constant.dir + "/" +"CheckmeMobile.apk", 0, handler);
		downloadTask.start();
	}

	/**
	 * 下载中处理函数
	 * @param progress 进度
	 */
	private void processPatch(int progress) {
		if (dialog!=null && dialog.isShowing()) {
			LogUtils.d("设置进度对话框进度：" + progress);
			dialog.setProgress(progress);
			if (progress == 100) {
				dialog.cancel();
				installApk();
			}
		}

	}

	/**
	 * 升级失败处理
	 */
	private void processUpdateFailed() {
		if (dialog!=null && dialog.isShowing()) {
			dialog.cancel();
		}
		ToastUtils.show(AboutApp.this, Constant.getString(R.string.update_failed));
	}

	/**
	 * 安装apk
	 */
	private void installApk()
	{
		File apkfile = new File(Constant.dir, "CheckmeMobile.apk");
		if (!apkfile.exists())
		{
			return;
		}
		// 通过Intent安装APK文件
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + apkfile.toString())
				, "application/vnd.android.package-archive");
		startActivity(intent);
	}

	private void intent2Disclaimer() {
		Intent intent = new Intent(getApplicationContext(), DisclaimerActivity.class);
		startActivity(intent);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		switch (view.getId()) {
			case R.id.bn_update:
				startUpdate((String)view.getTag());
				showUpdateDialog();
				break;
			case R.id.tv_disclaimer_item:
				intent2Disclaimer();
				break;
			default:
				break;
		}
	}
}

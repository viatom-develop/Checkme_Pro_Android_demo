package com.checkme.azur.monitor.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;


public class CheckmeMonitorUpdateUtils {

	public static void getPatch(Context context, CheckmeMonitorUpdateListener listener) {
		new GetPatchsThread(getIntVersion(context), listener).start();
	}

	private static class GetPatchsThread extends Thread{
		private int curAppVersion;
		private CheckmeMonitorUpdateListener listener;

		public GetPatchsThread(int curAppVersion, CheckmeMonitorUpdateListener listener) {
			super();
			this.curAppVersion = curAppVersion;
			this.listener = listener;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
//			try {
//				//获取升级包下载地址
//				JSONObject jsonObject = PostUtils.doPost(Constant.URL_GET_APP_PATCH
//						, PostInfoMaker.makeGetAppPatchInfo(curAppVersion));
//				if (jsonObject == null) {
//					//获取错误
//					listener.onPatchNotExist();
//					LogUtils.d("CheckmeMobile升级包获取错误");
//				}else {
//					if (jsonObject.getString("Result").equals("NULL") ||
//							jsonObject.getString("Result").equals("EXP")) {
//						LogUtils.d("CheckmeMobile升级包获取失败");
//						listener.onPatchNotExist();
//					}else if(jsonObject.getString("Result").equals("SUCCESS")){
//						//获取成功，生成ckmPatch对象
//						CheckmeMobilePatch patch = new CheckmeMobilePatch(jsonObject);
//						LogUtils.d("CheckmeMobile升级包获取成功");
//						listener.onPatchExist();
//					}
//				}
//			} catch (Exception e) {
//				// TODO: handle exception
//				e.printStackTrace();
//				LogUtils.d("CheckmeMobile升级包获取异常");
//				listener.onPatchNotExist();
//			}

		}
	}

	/**
	 * 获取app的版本，并返回int类型
	 * @return
	 */
	private static int getIntVersion(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			String strVersion = info.versionName.replace(".", "");
			return Integer.parseInt(strVersion);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}

	public static interface CheckmeMonitorUpdateListener {

		public void onPatchExist();

		public void onPatchNotExist();
	}

}

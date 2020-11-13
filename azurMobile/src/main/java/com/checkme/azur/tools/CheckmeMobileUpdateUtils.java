package com.checkme.azur.tools;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;

import com.checkme.azur.element.CheckmeMobilePatch;
import com.checkme.azur.element.Constant;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.utils.MsgUtils;

public class CheckmeMobileUpdateUtils {

	public static void getPatch(Context context, final Handler callerHandler) {
//		new GetPatchsThread(getIntVersion(context), callerHandler).start();

		RequestParams requestParams = new RequestParams(Constant.URL_GET_APP_PATCH);
		requestParams.addParameter("OSType", String.valueOf(CheckmeMobilePatch.OS_TYPE_ANDROID));
		requestParams.addParameter("Version", String.valueOf(getIntVersion(context)));
		requestParams.setConnectTimeout(5000);
		x.http().post(requestParams, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					//获取升级包下载地址
					JSONObject jsonObject = new JSONObject(result);
					if (jsonObject.getString("Result").equals("NULL") ||
							jsonObject.getString("Result").equals("EXP")) {
						LogUtils.d("CheckmeMobile升级包获取失败");
						MsgUtils.sendMsg(callerHandler, Constant.MSG_CHECKME_MOBILE_PATCH_NOT_EXIST);

					}else if(jsonObject.getString("Result").equals("SUCCESS")){
						//获取成功，生成ckmPatch对象
						CheckmeMobilePatch patch = new CheckmeMobilePatch(jsonObject);
						LogUtils.d("CheckmeMobile升级包获取成功");
						MsgUtils.sendMsg(callerHandler, patch, Constant.MSG_CHECKME_MOBILE_PATCH_EXIST);
					}
				} catch (Exception e) {
					e.printStackTrace();
					LogUtils.d("CheckmeMobile升级包获取异常");
					MsgUtils.sendMsg(callerHandler, Constant.MSG_CHECKME_MOBILE_PATCH_NOT_EXIST);
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				//获取错误
				MsgUtils.sendMsg(callerHandler, Constant.MSG_CHECKME_MOBILE_PATCH_NOT_EXIST);
				LogUtils.d("CheckmeMobile升级包获取错误");
			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}
		});
	}

//	private static class GetPatchsThread extends Thread{
//		private int curAppVersion;
//		private Handler callerHandler;
//
//		public GetPatchsThread(int curAppVersion, Handler callerHandler) {
//			super();
//			this.curAppVersion = curAppVersion;
//			this.callerHandler = callerHandler;
//		}
//
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			try {
//				//获取升级包下载地址
//				JSONObject jsonObject = PostUtils.doPost(Constant.URL_GET_APP_PATCH
//						, PostInfoMaker.makeGetAppPatchInfo(curAppVersion));
//				if (jsonObject == null) {
//					//获取错误
//					MsgUtils.sendMsg(callerHandler, Constant.MSG_CHECKME_MOBILE_PATCH_NOT_EXIST);
//					LogUtils.d("CheckmeMobile升级包获取错误");
//				}else {
//					if (jsonObject.getString("Result").equals("NULL") ||
//							jsonObject.getString("Result").equals("EXP")) {
//						LogUtils.d("CheckmeMobile升级包获取失败");
//						MsgUtils.sendMsg(callerHandler, Constant.MSG_CHECKME_MOBILE_PATCH_NOT_EXIST);
//
//					}else if(jsonObject.getString("Result").equals("SUCCESS")){
//						//获取成功，生成ckmPatch对象
//						CheckmeMobilePatch patch = new CheckmeMobilePatch(jsonObject);
//						LogUtils.d("CheckmeMobile升级包获取成功");
//						MsgUtils.sendMsg(callerHandler, patch, Constant.MSG_CHECKME_MOBILE_PATCH_EXIST);
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				LogUtils.d("CheckmeMobile升级包获取异常");
//				MsgUtils.sendMsg(callerHandler, Constant.MSG_CHECKME_MOBILE_PATCH_NOT_EXIST);
//			}
//
//		}
//	}

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

}

package com.checkme.azur.tools;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.widget.Toast;

import com.checkme.azur.utils.FileUtils;
import com.checkme.bluetooth.Logger;
import com.checkme.newazur.R;
import com.checkme.azur.element.Constant;
import com.checkme.azur.utils.LogUtils;

public class ShareUtils {

	private static Context mContext;

	private static Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case Constant.MSG_SAVE_BMP_SUCCESS:
					if (mContext!=null) {
						Toast.makeText(mContext, Constant.getString(R.string.save_successfully)
								, Toast.LENGTH_SHORT).show();
					}
					break;
				case Constant.MSG_SAVE_BMP_FAILED:
					if (mContext!=null) {
						Toast.makeText(mContext, Constant.getString(R.string.save_failed)
								, Toast.LENGTH_SHORT).show();
					}
					break;
				default:
					break;
			}

		};
	};

	/**
	 * 分享到本地相册
	 * @param context
	 * @param bitmap
	 * @param fileName
	 */
	public static void shareToLocal(Context context, Bitmap bitmap, String fileName) {
		if (context==null || bitmap==null || fileName==null) {
			return;
		}
		LogUtils.d("分享到本地");
		mContext = context;

		new saveBitmapThread(context, bitmap, Constant.pic_dir, fileName, false).start();
	}

	/**
	 * 分享到网络
	 * @param context
	 * @param bitmap
	 */
	public static void shareToNet(Context context, Bitmap bitmap) {
		if (context==null || bitmap==null) {
			return;
		}
		LogUtils.d("分享到网络");
		mContext = context;

		new saveBitmapThread(context, bitmap, Constant.shared_pic_dir, "Report", true).start();

		Intent intent = new Intent(
				Intent.ACTION_SEND_MULTIPLE);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_SUBJECT,
				"Health Data Measured by Checkme ");
		intent.putExtra(Intent.EXTRA_TEXT,
				"Reports: See file attachted");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		ArrayList<Uri> files = new ArrayList<>();
		File file = new File(Constant.shared_pic_dir, "Report.png");
		Uri contentUri = FileProvider.getUriForFile(context, "com.viatom.azur.fileprovider", file);
		if (contentUri != null && !TextUtils.isEmpty(contentUri.toString())) {
			contentUri = FileUtils.getImageContentUri(Constant.mContext, file);
		}
		Logger.d(ShareUtils.class, "contentUri == " + contentUri);
		files.add(contentUri);


		intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
//		context.startActivty(intent);
	}
	/**
	 * 分享到网络
	 * @param context
	 * @param fileName
	 */
	public static void shareToNet(Context context, String fileName) {
		if (context==null || fileName==null) {
			return;
		}
		LogUtils.d("分享到网络");
		mContext = context;

		Intent intent = new Intent(
				Intent.ACTION_SEND);
		intent.setType("*/*");
		intent.putExtra(Intent.EXTRA_SUBJECT,
				"Health Data Measured by Checkme ");
		intent.putExtra(Intent.EXTRA_TEXT,
				"Reports: See file attachted");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile
				(new File(Constant.dir, fileName)));

		context.startActivity(intent);
	}

	/**
	 * @author zouhao
	 * 保存图片线程，保存时间长会阻塞主线程
	 */
	public static class saveBitmapThread extends Thread {
		Context context;
		Bitmap bitmap;
		File dir;
		String fileName;
		boolean tempFile; //转发的

		public saveBitmapThread(Context context, Bitmap bitmap, File dir,
								String fileName, boolean tempFile) {
			super();
			this.context = context;
			this.bitmap = bitmap;
			this.dir = dir;
			this.fileName = fileName;
			this.tempFile = tempFile;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			BitmapUtils.saveBitmap(context, handler, dir, bitmap, fileName, tempFile);
		}
	}
}

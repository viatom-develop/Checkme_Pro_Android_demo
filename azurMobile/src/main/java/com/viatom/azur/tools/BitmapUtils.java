package com.viatom.azur.tools;

import java.io.File;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.FileProvider;

import com.viatom.azur.element.Constant;
import com.viatom.azur.utils.MsgUtils;

public class BitmapUtils {

	@SuppressLint("SdCardPath")
	public static void saveBitmap(Context context, Handler handler, File dir
			, Bitmap bitmap, String fileName, boolean tempFile) {
		if (!dir.exists()) {
			dir.mkdir();
		}
		File file = new File(dir, fileName + ".png");
		FileOutputStream out;
		try {
			out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
				if (!tempFile) {
					MsgUtils.sendMsg(handler, Constant.MSG_SAVE_BMP_SUCCESS);
				}
				out.flush();
				out.close();
				bitmap.recycle();
				bitmap = null;
			}
		} catch (Exception e) {
			if (!tempFile) {
				MsgUtils.sendMsg(handler, Constant.MSG_SAVE_BMP_FAILED);
			}
			e.printStackTrace();
		}


		//将图片保存到相册中
		Uri contentUri = FileProvider.getUriForFile(context, "com.viatom.azur.fileprovider", file);
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri));
//		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
	}

}

package com.viatom.azur.monitor.tools;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;

public class BitmapUtils {
	
	@SuppressLint("SdCardPath")
	public static void saveBitmap( File dir
			, Bitmap bitmap, String fileName) {
		if (!dir.exists()) {
			dir.mkdir();
		}
		File file = new File(dir, fileName + ".png");
		FileOutputStream out;
		try {
			out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
				out.flush();
				out.close();
				bitmap.recycle();
				bitmap = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

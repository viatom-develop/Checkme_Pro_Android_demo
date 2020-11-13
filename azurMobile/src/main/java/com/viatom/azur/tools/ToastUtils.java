package com.viatom.azur.tools;

import android.content.Context;
import android.widget.Toast;


public class ToastUtils {
	
	public static void show(Context context, String str) {
		Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
	}
}

package com.checkme.azur.widget;

import android.app.ProgressDialog;
import android.content.Context;

import com.checkme.newazur.R;

public class JProgressDialog {
	private static ProgressDialog progressDialog;
	
	public static void show(Context context) {
		if (progressDialog != null) {
			progressDialog.cancel();
		}
		
		progressDialog = new ProgressDialog(context, R.style.JDialogStyle);
		progressDialog.show();
		progressDialog.setContentView(R.layout.widget_j_progress_dialog);
		progressDialog.setCancelable(false);
	}
	
	public static void cancel() {
		if (progressDialog != null) {
			progressDialog.cancel();
		}
	}
}

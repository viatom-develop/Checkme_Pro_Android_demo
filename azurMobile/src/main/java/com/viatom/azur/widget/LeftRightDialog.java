package com.viatom.azur.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.viatom.newazur.R;

public class LeftRightDialog {

	public static final int STYLE_LEFT = 1;
	public static final int STYLE_RIGHT = 2;

	private static ProgressDialog dialog;

	public static void show(Context context, int style) {
		if (dialog != null) {
			dialog.cancel();
		}

		dialog = new ProgressDialog(context, R.style.JDialogStyle);
		dialog.show();

		//改变左右箭头可见性
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.widget_lr_dialog, null);
		if (style == STYLE_LEFT) {
			ImageView ivLeft = (ImageView)view.findViewById(R.id.iv_left);
			ivLeft.setVisibility(View.VISIBLE);
		}else {
			ImageView ivRight = (ImageView)view.findViewById(R.id.iv_right);
			ivRight.setVisibility(View.VISIBLE);
		}
		dialog.setContentView(view);

		delayCancel(200);
	}

	/**
	 * 延迟取消
	 * @param delay 单位为ms
	 */
	private static void delayCancel(int delay) {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				cancel();
			}
		}, delay);
	}

	public static void cancel() {
		if (dialog != null) {
			dialog.cancel();
		}
	}
}

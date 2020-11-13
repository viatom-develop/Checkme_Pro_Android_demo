package com.checkme.azur.monitor.widget;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.checkme.azur.activity.MainActivity;
import com.checkme.azur.monitor.element.Constant;
import com.checkme.newazur.R;

public class MypopuListFinish {

	public Handler mHandler;
	public PopupWindow popuWindow;

	private ImageView iv_back, iv_play;

	public MypopuListFinish(Handler mHandler) {
		this.mHandler = mHandler;

		initPopupWindow();

	}

	public MypopuListFinish() {
		super();
		// TODO Auto-generated constructor stub
		initPopupWindow();
	}

	public void initPopupWindow() {
		View view = (LinearLayout) MainActivity.instance.getLayoutInflater()
				.inflate(R.layout.widget_popuwindow_finish_dialog, null);

		// popuWindow = new PopupWindow(view, 500, 600);

		popuWindow = new PopupWindow(view);

		popuWindow.setWindowLayoutMode(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);

		popuWindow.setFocusable(true);

		iv_back = (ImageView) view.findViewById(R.id.iv_back);
		iv_play = (ImageView) view.findViewById(R.id.iv_paly);


		iv_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Message msg = new Message();
				msg.what = Constant.BACK_PLAY ;

				mHandler.sendMessage(msg);
				popuWindow.dismiss();
			}
		});

		iv_play.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				popuWindow.dismiss();

				Message msg = new Message();
				msg.what = Constant.PAUSE_PLAY ;

				mHandler.sendMessage(msg);

			}
		});

		popuWindow.update();
		// 只能点击之后才能取消，点击外面不能取消
		// popuWindow.setBackgroundDrawable(new ColorDrawable(0x88000000));
		// popuWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

		// 这里设置显示PopuWindow之后在外面点击是否有效。如果为false的话，那么点击PopuWindow外面并不会关闭PopuWindow。
		popuWindow.setOutsideTouchable(true);// 不能在没有焦点的时候使用

	}

}

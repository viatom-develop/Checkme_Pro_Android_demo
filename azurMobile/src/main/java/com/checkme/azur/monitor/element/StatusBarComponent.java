package com.checkme.azur.monitor.element;

import android.content.Context;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.checkme.azur.monitor.utils.MsgUtils;
import com.checkme.newazur.R;

import java.util.List;
import java.util.Timer;

public class StatusBarComponent {

	protected static final int MSG_REFRESH_VALS = 1001;

	private Context context;
	private RelativeLayout rootLayout;
	private List<OtherData> dataList;
	private Timer timer;

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
				case MSG_REFRESH_VALS:
					doReFreshVals((OtherData)msg.obj);
					break;

				default:
					break;
			}
		};

	};

	public StatusBarComponent(Context context, RelativeLayout rootLayout, List<OtherData> dataList) {
		super();
		this.rootLayout = rootLayout;
		this.dataList = dataList;
		this.context = context;
	}

	public void refreshViews() {
		if (dataList != null) {
			synchronized (dataList) {
				if (dataList.size() != 0) {
					OtherData refreshData = dataList.get(0);
					dataList.clear();
					MsgUtils.sendMsg(handler, refreshData, MSG_REFRESH_VALS);
				}

			}
		}
	}

	private void doReFreshVals(OtherData otherData) {
		if (otherData == null || rootLayout == null) {
			return;
		}
		//刷新电量
		int power = otherData.getPower();
		ImageView ivBattery = (ImageView)rootLayout.findViewById(R.id.iv_battery);
		if (power >= 75) {
			ivBattery.setImageDrawable(context.getResources().getDrawable(R.drawable.battery1));
		}else if (power >= 50) {
			ivBattery.setImageDrawable(context.getResources().getDrawable(R.drawable.battery2));
		}else if (power >= 25) {
			ivBattery.setImageDrawable(context.getResources().getDrawable(R.drawable.battery3));
		}else {
			ivBattery.setImageDrawable(context.getResources().getDrawable(R.drawable.battery4));
		}
	}
}

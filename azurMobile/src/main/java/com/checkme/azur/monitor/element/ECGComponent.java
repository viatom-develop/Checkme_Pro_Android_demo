package com.checkme.azur.monitor.element;

import android.os.Handler;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.checkme.azur.monitor.utils.FlagUtils;
import com.checkme.azur.monitor.utils.MsgUtils;
import com.checkme.azur.monitor.widget.RTWaveView;
import com.checkme.newazur.R;

import java.util.List;

public class ECGComponent extends MonitorComponent<ECGData> {

	protected static final int MSG_REFRESH_VALS = 1001;
	private boolean b ; 
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case MSG_REFRESH_VALS:
				doRefreshVals((ECGData) msg.obj);
				break;

			default:
				break;
			}
		};
	};

	public ECGComponent(RelativeLayout rootLayout, RTWaveView waveView,
			List<ECGData> dataList, CompParameters parameters,boolean b) {
		super(rootLayout, waveView, dataList, parameters);
		this.b = b ;
		// TODO Auto-generated constructor stub
	}

	/**
	 * 锟斤拷写刷锟斤拷锟斤拷值锟斤拷锟斤拷锟斤拷锟斤拷锟竭筹拷刷锟斤拷
	 * 
	 * @param data
	 */
	@Override
	protected void refreshVals(ECGData data) {
		// TODO Auto-generated method stub
		MsgUtils.sendMsg(handler, data, MSG_REFRESH_VALS);
	}

	/**
	 * 锟斤拷锟竭筹拷执锟斤拷刷锟斤拷锟斤拷值
	 * 
	 * @param data
	 */
	private void doRefreshVals(final ECGData data) {
		TextView tvECGVal = (TextView) rootLayout
				.findViewById(R.id.tv_main_val);
		FlagUtils.getInstence().mark = 1;
		    //蹇冪數澹伴煶鍝�
		// TextView tvPVCS =
		// (TextView)rootLayout.findViewById(R.id.tv_left_bottom);
		//
		if (data == null) {
			tvECGVal.setText("--");
			// tvPVCS.setText("PVCs  --");
			FlagUtils.getInstence().mark = 2;
			System.out.println("数据为空********");
			// tvPVCS.setText("锟斤拷幕锟斤拷图");
		} else {
			/*int hr = data.getHr();
			if (hr == 0 || hr == 0xFF) {
				tvECGVal.setText("--");
				FlagUtils.getInstence().mark = 2;
			} else {
				tvECGVal.setText(hr + "");
				
				// if(i%20==0){
				FlagUtils.getInstence().mark = 1;
				if (data.getrFlag() == 1) {
				//	Constant.binder.startMedia("ecg.wav");
					MedioPlayUtils.playVoice("ecg.wav");
				}
			}*/
			
			
			
			int hr = data.getHr();
			if (hr == 0 || hr == 0xFF) {
				tvECGVal.setText("--");
				FlagUtils.getInstence().mark = 2;
				// 就是没有数据的时候波形是直线，数据为 -5.0     
				if(data.getDatas()[0]==-5.0&&data.getDatas()[1]==-5.0&&data.getDatas()[2]==-5.0&&data.getDatas()[3]==-5.0){
					FlagUtils.getInstence().mark = 2;
				//	LogUtils.d("数据没有********"+data.getDatas()[0]+data.getDatas()[1]);
				}
		//		FlagUtils.getInstence().mark = 2;
				
			} else {
				
				if(hr>20&&hr<300){
					tvECGVal.setText(hr + "");
				}
				
			}
			
			if (data.getrFlag() == 1&&FlagUtils.getInstence().mark == 1&&b) {
				Constant.binder.startMedia("ecg.wav");
				
			//	MedioPlayUtils.playVoice("ecg.wav");
			}

			if (data.getPvcs() != 0) {
				// tvPVCS.setText("PVCs  " + data.getPvcs());
				// tvPVCS.setText("锟斤拷幕锟斤拷图");
			} else {
				// tvPVCS.setText("PVCs  --");
				// tvPVCS.setText("锟斤拷幕锟斤拷图");
			}
		}
	}
}

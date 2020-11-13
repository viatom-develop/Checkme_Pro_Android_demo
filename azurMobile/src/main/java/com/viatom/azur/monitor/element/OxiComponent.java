package com.viatom.azur.monitor.element;

import android.os.Handler;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viatom.azur.monitor.utils.FlagUtils;
import com.viatom.azur.monitor.utils.MsgUtils;
import com.viatom.azur.monitor.widget.OxiValueView;
import com.viatom.azur.monitor.widget.RTWaveView;
import com.viatom.newazur.R;

import java.util.List;

public class OxiComponent extends MonitorComponent<OxiData> {
	
	protected static final int MSG_REFRESH_VALS = 1001;
	public int i = 1 ;
	private boolean b ;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			
			switch (msg.what) {
			case MSG_REFRESH_VALS:
				doRefreshVals((OxiData)msg.obj);
				break;

			default:
				break;
			}
		};
	};
	
	public OxiComponent(RelativeLayout rootLayout, RTWaveView waveView,
			List<OxiData> dataList, CompParameters parameters,boolean b) {
		super(rootLayout, waveView, dataList, parameters);
		this.b = b;
		// TODO Auto-generated constructor stub
	}

	/**
	 * ��дˢ����ֵ���������߳�ˢ��
	 * @param data
	 */
	@Override
	protected void refreshVals(OxiData data) {
		// TODO Auto-generated method stub
		MsgUtils.sendMsg(handler, data, MSG_REFRESH_VALS);
	}
	
	/**
	 * ���߳�ִ��ˢ����ֵ
	 * @param data
	 */
	private void doRefreshVals(final OxiData data) {
		if (data == null) {
			TextView tvOxiVal = (TextView)rootLayout.findViewById(R.id.tv_main_val);
			tvOxiVal.setText("--");
			TextView tvPI = (TextView)rootLayout.findViewById(R.id.tv_pi_val);
			tvPI.setText("--");
			OxiValueView valueView = (OxiValueView)rootLayout.findViewById(R.id.wg_oxi_val);
			valueView.reDraw(200);
			return;
		}
		
		//SPO2
		TextView tvOxiVal = (TextView)rootLayout.findViewById(R.id.tv_main_val);
		final int spo2 = data.getSpo2();
		if(spo2 == 0 || spo2 == 0xFF){
			tvOxiVal.setText("--");
			
		}else {
			tvOxiVal.setText(spo2 + "");
			int i = data.getpFlag() ;
			
			int j = FlagUtils.getInstence().mark ;
			if(i == 1){
				System.out.println("huibuhuichuxian iii**"+j);
			}
			
			
			if(data.getpFlag()==1&&FlagUtils.getInstence().mark==2){	
				Constant.binder.startMedia(spo2+".wav");
			//	MedioPlayUtils.playVoice(spo2+".wav");
			}
		}
		
		//PI
		TextView tvPI = (TextView)rootLayout.findViewById(R.id.tv_pi_val);
		float pi = data.getPi();
		if(spo2 == 0 || spo2 == 0xFF){
			tvPI.setText("--");
		}else {
			tvPI.setText(pi + "");
		}
		
		//PR
//		TextView tvPR = (TextView)rootLayout.findViewById(R.id.tv_pr_val);
//		int pr = data.getPr();
//		if(pr == 0 || pr == 0xFF){
//			tvPR.setText("--");
//		}else {
//			tvPR.setText(pr + "");
//		}
		// Oxi value view
		OxiValueView valueView = (OxiValueView)rootLayout.findViewById(R.id.wg_oxi_val);
		valueView.reDraw(data.getDatas()[0].intValue());
	}
}

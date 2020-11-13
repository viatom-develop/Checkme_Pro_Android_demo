package com.checkme.azur.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.checkme.newazur.R;
import com.checkme.azur.element.Constant;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.utils.MsgUtils;

public class LeftMenuFragment extends ListFragment {

	Handler mHandler;
	private Context mContext;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mContext = activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.mContext = null;
	}

	public LeftMenuFragment() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void setmHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		setAdapter();
		return inflater.inflate(R.layout.fragment_left_menu, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}


	private void setAdapter() {
		SampleAdapter adapter = new SampleAdapter(mContext);

		int[] strList;
		int[] imgList;
		int[] tagList;
		//根据医院和家庭模式显示不同列表
		if (Constant.CKM_MODE.equals(Constant.CKM_MODE_HOSPITAL) ) {

			// for IMAD add sync patient list to device
			/*int[] strList1 = {
					R.string.title_spot_check,
					R.string.title_ecg,
					R.string.title_oximeter,
					R.string.title_temp,
					R.string.title_slm,
					R.string.title_device,
					R.string.title_settings,
					R.string.title_download,
					R.string.title_about};
			int[] imgList1 = {
					R.drawable.dlc_l,
					R.drawable.ecg_l,
					R.drawable.spo2_l,
					R.drawable.temp_l,
					R.drawable.slm_l,
					R.drawable.update_l,
					R.drawable.setting_l,
					R.drawable.setting_l
					,R.drawable.about_l };
			int[] tagList1 = {
					Constant.MSG_BNSPOT_CLICKED,
					Constant.MSG_BNECG_CLICKED
					,Constant.MSG_BNSPO2_CLICKED,
					Constant.MSG_BNTEMP_CLICKED
					,Constant.MSG_BNSLM_CLICKED,
					Constant.MSG_BNABOUT_CKM_CLICKED
					,Constant.MSG_BNSETTINGS_CLICKED,
					Constant.MSG_BNDOWNLOAD_CLICKED,
					Constant.MSG_BNABOUT_APP_CLICKED};*/

			// for general use
			int[] strList1 = {
					R.string.title_spot_check,
					R.string.title_ecg,
					R.string.title_oximeter,
					R.string.title_temp,
//					R.string.title_slm,
					R.string.title_device,
//					R.string.title_settings,
					R.string.title_about};
			int[] imgList1 = {
					R.drawable.dlc_l,
					R.drawable.ecg_l,
					R.drawable.spo2_l,
					R.drawable.temp_l,
//					R.drawable.slm_l,
					R.drawable.update_l,
//					R.drawable.setting_l,
					R.drawable.about_l };
			int[] tagList1 = {
					Constant.MSG_BNSPOT_CLICKED,
					Constant.MSG_BNECG_CLICKED
					,Constant.MSG_BNSPO2_CLICKED,
					Constant.MSG_BNTEMP_CLICKED,
//					,Constant.MSG_BNSLM_CLICKED,
					Constant.MSG_BNABOUT_CKM_CLICKED,
//					,Constant.MSG_BNSETTINGS_CLICKED,
					Constant.MSG_BNABOUT_APP_CLICKED};

			strList = strList1;
			imgList = imgList1;
			tagList = tagList1;
		}else {
			/*int[] strList2 = {
					R.string.title_dlc,
					R.string.title_ecg,
					R.string.title_oximeter,
					R.string.title_temp,
					R.string.title_slm,
					R.string.title_ped,
					R.string.title_device,
					R.string.title_settings,
					R.string.title_download,
					R.string.title_about
			};

			int[] imgList2 = {
					R.drawable.dlc_l,
					R.drawable.ecg_l,
					R.drawable.spo2_l,
					R.drawable.temp_l,
					R.drawable.slm_l,
					R.drawable.step_l,
					R.drawable.update_l,
					R.drawable.setting_l,
					R.drawable.setting_l,
					R.drawable.about_l
			};
			int[] tagList2 = {
					Constant.MSG_BNDLC_CLICKED,
					Constant.MSG_BNECG_CLICKED,
					Constant.MSG_BNSPO2_CLICKED,
					Constant.MSG_BNTEMP_CLICKED,
					Constant.MSG_BNSLM_CLICKED,
					Constant.MSG_BNPED_CLICKED,
					Constant.MSG_BNABOUT_CKM_CLICKED,
					Constant.MSG_BNSETTINGS_CLICKED,
					Constant.MSG_BNDOWNLOAD_CLICKED,
					Constant.MSG_BNABOUT_APP_CLICKED
			};*/

			int[] strList2 = {
					R.string.title_dlc,
					R.string.title_ecg,
					R.string.title_oximeter,
					R.string.title_temp,
//					R.string.title_bp,
//					R.string.title_slm,
					R.string.title_ped,
					R.string.title_device,
//					R.string.title_settings,
					R.string.title_about
			};

			int[] imgList2 = {
					R.drawable.dlc_l,
					R.drawable.ecg_l,
					R.drawable.spo2_l,
					R.drawable.temp_l,
//					R.drawable.bp_l,
//					R.drawable.slm_l,
					R.drawable.step_l,
					R.drawable.update_l,
//					R.drawable.setting_l,
					R.drawable.about_l
			};
			int[] tagList2 = {
					Constant.MSG_BNDLC_CLICKED,
					Constant.MSG_BNECG_CLICKED,
					Constant.MSG_BNSPO2_CLICKED,
					Constant.MSG_BNTEMP_CLICKED,
//					Constant.MSG_BNBP_CLICKED,
//					Constant.MSG_BNSLM_CLICKED,
					Constant.MSG_BNPED_CLICKED,
					Constant.MSG_BNABOUT_CKM_CLICKED,
//					Constant.MSG_BNSETTINGS_CLICKED,
					Constant.MSG_BNABOUT_APP_CLICKED
			};


			strList = strList2;
			imgList = imgList2;
			tagList = tagList2;
		}

		for (int i = 0; i < Math.min(strList.length, imgList.length); i++) {
			adapter.add(new SampleItem(Constant.getString(strList[i]), imgList[i], tagList[i]));
		}
		setListAdapter(adapter);
	}

	private class SampleItem {
		public String title;
		public int iconRes;
		public int tag;

		public SampleItem(String title, int iconRes, int tag) {
			this.title = title;
			this.iconRes = iconRes;
			this.tag = tag;
		}
	}

	public class SampleAdapter extends ArrayAdapter<SampleItem> {

		public SampleAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.row_left_menu, null);
			}
			ImageView icon = (ImageView) convertView
					.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView
					.findViewById(R.id.row_title);
			title.setText(getItem(position).title);
			convertView.setTag(getItem(position).tag);
			return convertView;
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (mHandler == null) {
			LogUtils.d("mHadler=null,无法跳转");
			return;
		}

		MsgUtils.sendMsg(mHandler, ((Integer)v.getTag()).intValue());
	}
}

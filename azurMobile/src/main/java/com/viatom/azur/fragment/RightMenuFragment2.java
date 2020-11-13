package com.viatom.azur.fragment;

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

import com.viatom.azur.element.CEApplication;
import com.viatom.newazur.R;
import com.viatom.azur.element.Constant;
import com.viatom.azur.measurement.SpotUser;
import com.viatom.azur.measurement.SpotUser.SpotUserInfo;
import com.viatom.azur.tools.PreferenceUtils;
import com.viatom.azur.utils.LogUtils;
import com.viatom.azur.utils.MsgUtils;

/**
 * @author zouhao
 * 医院模式下用
 */
public class RightMenuFragment2 extends ListFragment {

	Handler mHandler;
	SpotUser[] userList;
	Context mContext;
	private Context newContext;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.newContext = activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.newContext = null;
	}

	public void setmHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

	public void setmContext(Context mContext) {
		this.mContext = mContext;
	}

	public void setUserList(){
		userList = Constant.spotUserList;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_right_menu, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setAdapter();
	}

	private void setAdapter() {
		if(userList==null||userList.length==0)
			return;
		SampleAdapter adapter = new SampleAdapter(newContext);
		for (int i = 0; i < userList.length; i++) {
			int ico;
			SpotUserInfo info = userList[i].getUserInfo();
			if (info.getGender() == 0) {
				ico = Constant.ICO_IMG[0];
			}else if (info.getGender() == 1) {
				ico = Constant.ICO_IMG[3];
			}else {
				ico = Constant.ICO_IMG[8];
			}

			adapter.add(new SampleItem(info.getPatientID(), ico ));
		}
		setListAdapter(adapter);
	}

	private class SampleItem {
		public String tag;
		public int iconRes;

		public SampleItem(String tag, int iconRes) {
			this.tag = tag;
			this.iconRes = iconRes;
		}
	}

	public class SampleAdapter extends ArrayAdapter<SampleItem> {

		public SampleAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.row_right_menu, null);
			}
			ImageView icon = (ImageView) convertView
					.findViewById(R.id.RowRightMenuImgIcon);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView
					.findViewById(R.id.RowRightMenuTextTitle);
			title.setText(getItem(position).tag);
			return convertView;
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		if(mHandler!=null){
			Constant.curSpotUser = Constant.spotUserList[position];
			//保存当前用户index
			PreferenceUtils.savePreferences(newContext.getApplicationContext(), "PreSpotUserIndex", position);
			MsgUtils.sendMsg(mHandler, Constant.MSG_USER_CHOSED);
		}
		else
			LogUtils.d("mHadler=null,无法跳转");
	}
}

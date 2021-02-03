package com.checkme.azur.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.checkme.bluetooth.Logger;
import com.checkme.newazur.R;
import com.checkme.azur.element.Constant;
import com.checkme.azur.measurement.User;
import com.checkme.azur.tools.PreferenceUtils;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.utils.MsgUtils;

/**
 * @author zouhao
 * 普通模式下用
 */
public class RightMenuFragment extends ListFragment {

	Handler mHandler;
	User[] userList;
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
		this.newContext = mContext;
	}

	public void setUserList(){
		userList = Constant.userList;
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
		Logger.d(RightMenuFragment.class, "userList.length == " + userList.length);
		for (int i = 0; i < userList.length; i++) {
			int icoNum = userList[i].getUserInfo().getICO();
			Logger.d(RightMenuFragment.class, "UserInfo().getICO() == " + userList[i].getUserInfo().getICO());
			icoNum = (icoNum-1) > Constant.ICO_IMG.length ? 1 : icoNum;
			if(icoNum > 0) {
				adapter.add(new SampleItem(userList[i].getUserInfo().getName(), Constant.ICO_IMG[icoNum-1] ));
			} else {
				adapter.add(new SampleItem(userList[i].getUserInfo().getName(), Constant.ICO_IMG[icoNum] ));
			}

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
			Constant.curUser = Constant.userList[position];
			//保存当前用户index
			PreferenceUtils.savePreferences(newContext.getApplicationContext(), "PreUserIndex", position);
			MsgUtils.sendMsg(mHandler, Constant.MSG_USER_CHOSED);
		}
		else
			LogUtils.d("mHadler=null,无法跳转");
	}
}

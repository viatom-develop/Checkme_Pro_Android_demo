package com.checkme.azur.tools;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.checkme.newazur.R;
import com.checkme.azur.element.CheckmeDevice;
import com.checkme.azur.utils.LogUtils;

public class ChooseDeviceAdapter extends BaseAdapter{

	private LayoutInflater mInflater;
	private List<CheckmeDevice> deviceList;


	public ChooseDeviceAdapter(Context context, List<CheckmeDevice> deviceList) {
		super();
		// TODO Auto-generated constructor stub
		this.deviceList = deviceList;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		if (deviceList == null) {
			return 0;
		}else {
			return deviceList.size();
		}
	}

	@Override
	public Object getItem(int pos) {
		if (deviceList == null) {
			return null;
		}else {
			return deviceList.get(pos);
		}
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		if (convertView == null) {
			LogUtils.println("convertView=null");
			convertView = mInflater.inflate(R.layout.list_device, null);
			viewHolder = new ViewHolder();
			//获得View资源ID
			viewHolder.tvDevice = (TextView)convertView.findViewById(R.id.tv_device);
			viewHolder.ivDevice = (ImageView)convertView.findViewById(R.id.iv_device);
			viewHolder.rbDevice = (RadioButton)convertView.findViewById(R.id.rb_device);
			//设置标签
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		//设置属性
		CheckmeDevice device = (CheckmeDevice)getItem(position);
		if(!TextUtils.isEmpty(device.getName()) && device.getName().startsWith("Checkme Lite")) {
			viewHolder.ivDevice.setImageResource(R.drawable.checkmel);
		}
		viewHolder.tvDevice.setText(device.getName());
		viewHolder.rbDevice.setChecked(device.isAvailable());

		return convertView;
	}

	public static class ViewHolder {
		public ImageView ivDevice;
		public TextView tvDevice;
		public RadioButton rbDevice;
	}
}

package com.viatom.azur.monitor.tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.viatom.newazur.R;

import java.util.List;

public class MyMuluAdapter extends BaseAdapter{
	
	private List<String> list ;
	private LayoutInflater mInflater ;
	private Context mContext ;
	
	public MyMuluAdapter(List list,LayoutInflater inf,Context mContext){
		this.list = list ;
		this.mInflater = inf ;
		this.mContext = mContext ;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder ;
		if (convertView == null ) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.monitor_detail_list, null);
		//	convertView.setBackgroundColor(mContext.getResources().getColor(R.color.White));
			holder.img = (ImageView) convertView.findViewById(R.id.iv_file_flag);
			holder.tvname = (TextView) convertView.findViewById(R.id.tv_file_name);
			convertView.setTag(holder); 
			
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.tvname.setText(list.get(position));
		String s = list.get(position) ;
		String[] str = s.split("\\.");
		
		if(str.length == 1){
			holder.img.setBackgroundResource(R.drawable.video);
		}else{
			holder.img.setBackgroundResource(R.drawable.picture);
		}
		
		return convertView;
	}

	
	class ViewHolder{
		public TextView tvname ;
		public ImageView img ;
	}

}

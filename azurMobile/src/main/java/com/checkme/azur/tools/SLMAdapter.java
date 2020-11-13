package com.checkme.azur.tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.checkme.newazur.R;
import com.checkme.azur.element.Constant;
import com.checkme.azur.measurement.SLMItem;

public class SLMAdapter extends BaseAdapter{
	private LayoutInflater mInflater;
	private int DownProgress = 0;
	private int PreClickBnAct = 0;

	public SLMAdapter(Context context){
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return Constant.defaultUser.getSlmList().size();
	}

	@Override
	public Object getItem(int pos) {
		return Constant.defaultUser.getSlmList().get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup arg2) {
		ViewHolder viewHolder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_slm, null);
			viewHolder = new ViewHolder();
			//获得View资源ID
			viewHolder.mTextDate = (TextView)convertView.findViewById(R.id.ListSLMTextDate);
			viewHolder.mTextTime = (TextView)convertView.findViewById(R.id.ListSLMTextTime);
			viewHolder.mIMGResult = (ImageView)convertView.findViewById(R.id.ListSLMImgResult);
			viewHolder.mBnAct = (Button)convertView.findViewById(R.id.ListSLMBnAct);
			viewHolder.mProgressBar = (ProgressBar)convertView.findViewById(R.id.ListSLMPro);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		//获得View在列表中对应的信息
		SLMItem item = Constant.defaultUser.getSlmList().get(pos);
		String Date = StringMaker.makeDateString(item.getStartTime());
		String Time = StringMaker.makeTimeString(item.getStartTime());

//		int IMGResult = Constant.RESULT_IMG[item.getImgResult()];
		int IMGResult = R.drawable.none;

		int ProVisible = item.isbDownloading() == true ? View.VISIBLE:View.INVISIBLE;
		int IMGBnAct = item.isDownloaded() == true ? R.drawable.button_enter:R.drawable.button_download;
		//设置View对应信息
		viewHolder.mTextDate.setText(Date);
		viewHolder.mTextTime.setText(Time);
		viewHolder.mIMGResult.setImageResource(IMGResult);
		viewHolder.mBnAct.setBackgroundResource(IMGBnAct);
		viewHolder.mProgressBar.setVisibility(ProVisible);
		viewHolder.mProgressBar.setProgress(DownProgress);
		//设置每个BnAct对应标签(pos)
		viewHolder.mBnAct.setTag(pos);

		return convertView;
	}

	/**
	 * 显示进度条
	 * @param position
	 */
	public void SetItembDownloading(int position,boolean bDownloading){
		PreClickBnAct = position;//记录已点击的按键pos
		DownProgress = 0;//点击后进度条归零
		((SLMItem)getItem(position)).setbDownloading(bDownloading);
		notifyDataSetChanged();
	}

	public void SetDownProgress(int Progress){
		DownProgress = Progress;
		notifyDataSetChanged();
		if(DownProgress>=100)
			SetBnActToBnEnter();
	}

	public void SetBnActToBnEnter(){
		((SLMItem)getItem(PreClickBnAct)).setbDownloading(false);
		((SLMItem)getItem(PreClickBnAct)).setDownloaded(true);
		notifyDataSetChanged();
	}


	public static class ViewHolder
	{
		public TextView mTextDate;
		public TextView mTextTime;
		public ImageView mIMGResult;
		public Button mBnAct;
		public ProgressBar mProgressBar;
	}
}

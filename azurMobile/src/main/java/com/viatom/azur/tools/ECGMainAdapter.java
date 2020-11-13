package com.viatom.azur.tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.viatom.newazur.R;
import com.viatom.azur.element.Constant;
import com.viatom.azur.measurement.ECGItem;
import com.viatom.azur.measurement.User;

public class ECGMainAdapter extends BaseAdapter{
	private LayoutInflater mInflater;
	private int DownProgress = 0;
	private int PreClickBnAct = 0;

	public ECGMainAdapter(Context context){
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {

		return Constant.defaultUser.getEcgList().size();
	}

	@Override
	public Object getItem(int pos) {
		return Constant.defaultUser.getEcgList().get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup arg2) {
		ViewHolder viewHolder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_ecg_main, null);
			viewHolder = new ViewHolder();
			//获得View资源ID
			viewHolder.mTextDate = (TextView)convertView.findViewById(R.id.ListECGMainTextDate);
			viewHolder.mTextTime = (TextView)convertView.findViewById(R.id.ListECGMainTextTime);
			viewHolder.mCheckMode = (ImageView)convertView.findViewById(R.id.ListECGMainImgCheckMode);
			viewHolder.mIMGResult = (ImageView)convertView.findViewById(R.id.ListECGMainImgResult);
			viewHolder.mVoiceFlag = (ImageView)convertView.findViewById(R.id.ListECGMainImgVoice);
			viewHolder.mBnAct = (Button)convertView.findViewById(R.id.ListECGMainBnAct);
			viewHolder.mProgressBar = (ProgressBar)convertView.findViewById(R.id.ListECGMainPro);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		//获得View在列表中对应的信息
		ECGItem item = (ECGItem)getItem(pos);
		String Date = StringMaker.makeDateString(item.getDate());
		String Time = StringMaker.makeTimeString(item.getDate());
		int IMGCheckMode = Constant.CHECK_MODE_IMG[item.getMeasuringMode()-1];
		int IMGResult = Constant.RESULT_IMG[item.getImgResult()];
		int IMGVoiceFlag = Constant.VOICE_IMG[item.getVoiceFlag()];
		int ProVisible = item.isbDownloading() == true ? View.VISIBLE:View.INVISIBLE;
		int IMGBnAct = item.isDownloaded() == true ? R.drawable.button_enter:R.drawable.button_download;

		//设置View对应信息
		viewHolder.mTextDate.setText(Date);
		viewHolder.mTextTime.setText(Time);
		viewHolder.mCheckMode.setImageResource(IMGCheckMode);
		viewHolder.mIMGResult.setImageResource(IMGResult);
		viewHolder.mVoiceFlag.setImageResource(IMGVoiceFlag);
		viewHolder.mBnAct.setBackgroundResource(IMGBnAct);
		viewHolder.mProgressBar.setVisibility(ProVisible);
		viewHolder.mProgressBar.setProgress(DownProgress);
		//设置每个BnAct对应标签(pos)
		viewHolder.mBnAct.setTag(pos);
		return convertView;
	}

	public void SetDownProgress(int Progress){
		DownProgress = Progress;
		notifyDataSetChanged();
		if(DownProgress>=100)
			SetBnActToBnEnter();
	}

	public void SetBnActToBnEnter(){
		((ECGItem)getItem(PreClickBnAct)).setbDownloading(false);
		((ECGItem)getItem(PreClickBnAct)).setDownloaded(true);
		notifyDataSetChanged();
	}

	/**
	 * 设置显示进度条
	 * @param position
	 */
	public void SetItembDownloading(int position,boolean bDownloading){
		PreClickBnAct = position;//记录已点击的按键pos
		DownProgress = 0;//点击后进度条归零
		((ECGItem)getItem(position)).setbDownloading(bDownloading);
		notifyDataSetChanged();
	}


	public static class ViewHolder
	{
		public TextView mTextDate;
		public TextView mTextTime;
		public ImageView mCheckMode;
		public ImageView mIMGResult;
		public ImageView mVoiceFlag;
		public Button mBnAct;
		public ProgressBar mProgressBar;
	}
}

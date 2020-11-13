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
import com.checkme.azur.measurement.DailyCheckItem;
import com.checkme.azur.utils.LogUtils;

public class DailyCheckAdapter extends BaseAdapter{
	private LayoutInflater mInflater;
	private int DownProgress = 0;
	private int PreClickBnAct = 0;

	public DailyCheckAdapter(Context context){
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return Constant.curUser.getDlcList().size();
	}

	@Override
	public Object getItem(int pos) {
		return Constant.curUser.getDlcList().get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup arg2) {
		ViewHolder viewHolder;

		if (convertView == null) {
			LogUtils.println("convertView=null");
			convertView = mInflater.inflate(R.layout.list_daily_check, null);
			viewHolder = new ViewHolder();
			//获得View资源ID
			viewHolder.mTextDate = (TextView)convertView.findViewById(R.id.ListDailyCheckTextDate);
			viewHolder.mTextTime = (TextView)convertView.findViewById(R.id.ListDailyCheckTextTime);
			viewHolder.mVoiceFlag = (ImageView)convertView.findViewById(R.id.ListDailyCheckImgVoice);
			viewHolder.mIMGResult = (ImageView)convertView.findViewById(R.id.ListDailyCheckImgResult);
			viewHolder.mBnAct = (Button)convertView.findViewById(R.id.ListDailyCheckBnAct);
			viewHolder.mProgressBar = (ProgressBar)convertView.findViewById(R.id.ListDailyCheckPro);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		//获得View在列表中对应的信息
		String Date = StringMaker.makeDateString(((DailyCheckItem)getItem(pos)).getDate());
		String Time = StringMaker.makeTimeString(((DailyCheckItem)getItem(pos)).getDate());
		int IMGVoiceFlag = Constant.VOICE_IMG[((DailyCheckItem)getItem(pos)).getVoiceFlag()];
		int IMGResult = Constant.RESULT_IMG[((DailyCheckItem)getItem(pos)).getECGIMGResult()];

		int ProVisible = ((DailyCheckItem)getItem(pos)).isbDownLoading() == true ? View.VISIBLE:View.INVISIBLE;
		int IMGBnAct = ((DailyCheckItem)getItem(pos)).isDownloaded() == true ? R.drawable.button_enter:R.drawable.button_download;

		//设置View对应信息
		viewHolder.mTextDate.setText(Date);
		viewHolder.mTextTime.setText(Time);
		viewHolder.mVoiceFlag.setImageResource(IMGVoiceFlag);
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
		((DailyCheckItem)getItem(position)).setbDownLoading(bDownloading);
		notifyDataSetChanged();
	}

	/**
	 * 显示点击时反馈
	 * @param position
	 */
	public void SetItemPressedView(int position) {

	}

	public void SetDownProgress(int Progress) {
		DownProgress = Progress;
		notifyDataSetChanged();
		if(DownProgress>=100)
			SetBnActToBnEnter();
	}

	public void SetBnActToBnEnter() {
		((DailyCheckItem)getItem(PreClickBnAct)).setbDownLoading(false);
		((DailyCheckItem)getItem(PreClickBnAct)).setDownloaded(true);
		notifyDataSetChanged();
	}

	public static class ViewHolder {
		public TextView mTextDate;
		public TextView mTextTime;
		public ImageView mCheckMode;
		public ImageView mVoiceFlag;
		public ImageView mIMGResult;
		public Button mBnAct;
		public ProgressBar mProgressBar;
	}
}

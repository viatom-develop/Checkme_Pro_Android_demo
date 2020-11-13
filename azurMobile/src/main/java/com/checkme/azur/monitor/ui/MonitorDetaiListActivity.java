package com.checkme.azur.monitor.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.checkme.azur.activity.MainActivity;
import com.checkme.azur.monitor.element.CommonCreator;
import com.checkme.azur.monitor.element.Constant;
import com.checkme.azur.monitor.tools.FileCoder;
import com.checkme.azur.monitor.tools.MyMuluAdapter;
import com.checkme.azur.monitor.tools.PreferenceUtils;
import com.checkme.azur.monitor.utils.LogUtils;
import com.checkme.newazur.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 列表详情
 * @author tanshiyao
 *
 */
public class MonitorDetaiListActivity extends Activity{

	private TextView tv_file_name ;   //显示播放的文件名
	private SwipeMenuListView lv_mulu_list ;    //列表
	private RelativeLayout rl_video ; //整体的播放布局
	private ImageView iv_showw_pic ,iv_show;  // 显示图片
	private View rlView ;
	private LayoutInflater inflater ;
	private ArrayList<String> list = new ArrayList<String>(); // 存放文件进去的文件名，也就是录制完成的时间。
	private String filename ;

	private MyMuluAdapter muluAdapter ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_monitor_detail);
		super.onCreate(savedInstanceState);


		lv_mulu_list = (SwipeMenuListView) findViewById(R.id.lv_mulu_list);
		rl_video = (RelativeLayout) findViewById(R.id.rl_video);
		tv_file_name = (TextView) findViewById(R.id.tv_file_name);
		iv_showw_pic = (ImageView) findViewById(R.id.iv_show_pic);
		iv_show = (ImageView) findViewById(R.id.iv_show);
		rlView = findViewById(R.id.fl_bg);
		inflater = LayoutInflater.from(getApplicationContext());


		//查找所有的文件，图片和视屏

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				checkAllFile();
			}
		},0);


		iv_show.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//播放录制数据
				Intent mIntent = new Intent(MonitorDetaiListActivity.this,MonitorActivityPlay.class);

				mIntent.putExtra("filename", filename);

				startActivity(mIntent);

			}
		});

		iv_showw_pic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(lv_mulu_list.isShown()){

					Animation animation = AnimationUtils.loadAnimation(
							getApplicationContext(), R.anim.pic_change_alpha);
					//	animation.setFillAfter(true); 设为true相当于保存当前状态不在改变
					iv_showw_pic.startAnimation(animation);
					lv_mulu_list.setVisibility(View.GONE);

				}else{
					/*Animation animation = AnimationUtils.loadAnimation(
							getApplicationContext(), R.anim.luzhi_change_alpha);
				//	animation.setFillAfter(true); 设为true相当于保存当前状态不在改变
					iv_showw_pic.startAnimation(animation);*/
					lv_mulu_list.setVisibility(View.VISIBLE);
				}

			}
		});



	}

	int pos ;    //记录点击的是哪一个条目,记录删除这条是要清屏

	private void checkAllFile() {
		// TODO Auto-generated method stub
		//查询图片
		selectPicture();
		//查询视屏
		selectVideo();

		if(list.size()!=0){
			Collections.sort(list);
			Collections.reverse(list);
		}

		muluAdapter = new MyMuluAdapter(list,inflater,MonitorDetaiListActivity.this);
		lv_mulu_list.setAdapter(muluAdapter);
		lv_mulu_list.setMenuCreator(CommonCreator.makeSwipeMenuCreator(getApplicationContext()));
		lv_mulu_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				// TODO Auto-generated method stub

				String[] str = list.get(position).split("\\.");
				pos = position ;
				//用于播放文件时候用
				filename = str[0] ;


				if(str.length == 1){
					//视屏
					if(iv_showw_pic.isShown()){
						iv_showw_pic.setVisibility(View.GONE);
					}

					rl_video.setVisibility(View.VISIBLE);
					tv_file_name.setText(list.get(position));
				}else{
					//图片
					if(rl_video.isShown()){
						rl_video.setVisibility(View.GONE);
					}
					iv_showw_pic.setVisibility(View.VISIBLE);
					//查询当前图片的方法
					String path = selectShowPic(list.get(position));

					Bitmap bm = BitmapFactory.decodeFile(path, null);
					/*//强制计算当前view的宽和高
					int w = View.MeasureSpec.makeMeasureSpec(0,
							View.MeasureSpec.UNSPECIFIED);
					int h = View.MeasureSpec.makeMeasureSpec(0,
							View.MeasureSpec.UNSPECIFIED);
					iv_showw_pic.measure(w, h);
					int pic_height = rlView.getMeasuredHeight();
					int pic_width = rlView.getMeasuredWidth();
					int allWidth = MonitorActivity.instance.width;
					int allHeight = MonitorActivity.instance.height;

					float w1 = (pic_width/allWidth) ;
					float h1 = pic_height/allHeight ;

					  Matrix matrix = new Matrix();
					  matrix.postScale(0.70f,0.95f); //长和宽放大缩小的比例
					  Bitmap resizeBmp = Bitmap.createBitmap(bm,0,0,bm.getWidth(),bm.getHeight(),matrix,true);
					*/

					iv_showw_pic.setImageBitmap(bm);


				}

			}

		});

		lv_mulu_list.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
				// TODO Auto-generated method stub
				if (index == 0) {//如果是第0个按键按下
					String[] str = list.get(position).split("\\.");
					//			Toast.makeText(getApplicationContext(), "pos:"+pos+",postion:"+position+",select:", 1).show();

					if(str.length == 1){
						FileCoder.delFile(Constant.dir, list.get(position));
						if(pos == position){
							rl_video.setVisibility(View.GONE);
						}else if(pos > position){
							pos -- ;
							lv_mulu_list.setSelection(pos);
						}


					}else{
						FileCoder.delFile(Constant.pic_dir, list.get(position));
						if(pos == position){
							iv_showw_pic.setVisibility(View.GONE);
						}else if(pos > position){
							pos -- ;
							lv_mulu_list.setSelection(pos);
						}

					}
					list.remove(position);
					muluAdapter.notifyDataSetChanged();

					if (list.size() == 0) {
						//	refreshView();
					}
				}

				return false;
			}
		});


	}

	private String selectShowPic(String name) {
		// TODO Auto-generated method stub
		File[] files = Constant.pic_dir.listFiles();
		String str ;
		for (File f : files) {
			str = f.getAbsolutePath() ;
			String[] st = str.split("/");
			String filename = st[st.length - 1];
			if(filename.equals(name)){
				return str ;
			}
		}

		return null ;
	}


	private void selectVideo() {
		// TODO Auto-generated method stub
		File[] files = Constant.dir.listFiles();
		if(files == null){
			return ;
		}
		for (File f : files) {
			String[] str = f.getAbsolutePath().split("/");
			String filename = str[str.length - 1];
			if (!MonitorActivity.instance.flag) {
				list.add(filename);
			} else if (!filename.equals(MonitorActivity.instance.time)) {
				list.add(filename);
			}
		}

	}



	private void selectPicture() {
		// TODO Auto-generated method stub
		File[] files = Constant.pic_dir.listFiles();
		if(files == null){
			return ;
		}
		for (File f : files) {
			String[] str = f.getAbsolutePath().split("/");
			String filename = str[str.length - 1];
			list.add(filename);
		}
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				LogUtils.d("");
				finish();
				boolean b = PreferenceUtils.readBoolPreferences(getApplicationContext(), "BT_MODE");
				if(!b){
					if (MainActivity.instance.timer != null) {
						MainActivity.instance.timer.cancel(); // 退出程序时，也要取消计时器，不然一直在走。

					}
					if (MainActivity.instance.progressDialog != null) {
						MainActivity.instance.progressDialog.dismiss();
					}

					MainActivity.instance.finish();
				}

				break;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}


}

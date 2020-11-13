package com.viatom.azur.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.viatom.azur.EventBusEvent.FlushIvEvent;
import com.viatom.newazur.R;
import com.viatom.azur.bluetooth.BTConstant;
import com.viatom.azur.bluetooth.ReadFileListener;
import com.viatom.azur.bluetooth.WriteFileListener;
import com.viatom.azur.element.CommonCreator;
import com.viatom.azur.element.Constant;
import com.viatom.azur.measurement.CommonItem;
import com.viatom.azur.measurement.MeasurementConstant;
import com.viatom.azur.measurement.SPO2Item;
import com.viatom.azur.tools.NoInfoViewUtils;
import com.viatom.azur.tools.ShareUtils;
import com.viatom.azur.tools.StringMaker;
import com.viatom.azur.tools.ToastUtils;
import com.viatom.azur.utils.CommonItemFilter;
import com.viatom.azur.utils.FileDriver;
import com.viatom.azur.utils.FileUtils;
import com.viatom.azur.utils.JsonUtils;
import com.viatom.azur.utils.LogUtils;
import com.viatom.azur.utils.MsgUtils;
import com.viatom.azur.utils.TimeComparator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SPO2Main extends BaseActivity implements
		OnMenuItemClickListener, ReadFileListener {

	SimpleAdapter simpleAdapter;

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case Constant.CMD_TYPE_SPO2:
					readLocalSPO2List();
					break;
			}
		};
	};

	@Override
	public boolean isTheSameIntent(int menuTag) {
		return (menuTag == Constant.MSG_BNSPO2_CLICKED);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spo2_main);
		EventBus.getDefault().register(this);
		initUI();
		getSPO2List();
	}
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onFlushIvEvent(FlushIvEvent event) {
		initBtnCloud(event.getUploadState());
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		EventBus.getDefault().unregister(this);
	}

	/**
	 * UI初始化
	 */
	public void initUI() {
		reFreshActionBarBn(false,false);
		reFreshTitle(getResources().getString(R.string.title_oximeter));
	}

	/**
	 * 获取spo2列表
	 */
	public void getSPO2List() {
		// 先判断是否缓存有数据
		if (Constant.defaultUser.getSpo2list().size() == 0) {// 没有数据，读取
			if (Constant.btConnectFlag == true) {
				Constant.binder.interfaceReadFile(MeasurementConstant.FILE_NAME_SPO2_LIST
						, Constant.CMD_TYPE_SPO2, 5000, this);
			} else {
				readLocalSPO2List();
			}
		} else {// 有数据，直接显示
			refreshView();
		}
	}

	@Override
	public void onReadPartFinished(String fileName, byte fileType,
								   float percentage) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onReadSuccess(String fileName, byte fileType, byte[] fileBuf) {
		// TODO Auto-generated method stub
		//删除旧文件，保存新数据表到本地文件
		FileDriver.delFile(Constant.dir, fileName);
		FileDriver.write(Constant.dir,  fileName, fileBuf);
		MsgUtils.sendMsg(handler, fileType);
	}

	@Override
	public void onReadFailed(String fileName, byte fileType, byte errCode) {
		// TODO Auto-generated method stub
		//读失败同样删除旧文件
//		FileDriver.delFile(Constant.dir, fileName);
		MsgUtils.sendMsg(handler, fileType, errCode);
	}


	/**
	 * 离线模式时读取本地列表
	 */
	public void readLocalSPO2List() {

		//读取上次退出是存储的旧列表
		List<SPO2Item> oldSPO2List = FileUtils.readSPO2List(Constant.dir
				, MeasurementConstant.FILE_NAME_SPO2_LIST_OLD);
		if (oldSPO2List != null && oldSPO2List.size() != 0) {
			Constant.defaultUser.addSPO2List(oldSPO2List);
		}
		//读取新下载的列表，合并到旧列表
		List<SPO2Item> newSPO2List = FileUtils.readSPO2List(Constant.dir
				, MeasurementConstant.FILE_NAME_SPO2_LIST);
		if (newSPO2List != null && newSPO2List.size() != 0) {
			Constant.defaultUser.addSPO2List(newSPO2List);
		}
		//去除重复，并倒序排列
		CommonItemFilter.removeSameItems(Constant.defaultUser.getSpo2list());
		Collections.sort((List<CommonItem>)Constant.defaultUser
						.getList(MeasurementConstant.CMD_TYPE_SPO2)
				, new TimeComparator());

		refreshView();
	}


	public void refreshView() {
		((ProgressBar) findViewById(R.id.SPO2MainListPro))
				.setVisibility(View.INVISIBLE);
		processSPO2List();
		refreshNoInfoView();
	}

	/**
	 * 重新筛选列表
	 */
	public void processSPO2List() {
		if (Constant.defaultUser.getSpo2list().size()!=0) {
			List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
			ArrayList<SPO2Item> list = Constant.defaultUser.getSpo2list();
			for (int i = 0; i < list.size(); i++) {
				SPO2Item item = list.get(i);
				Map<String, Object> listItem = new HashMap<String, Object>();
				listItem.put("Times", StringMaker.makeTimeString(item.getDate()));
				listItem.put("Dates", StringMaker.makeDateString(item.getDate()));
				listItem.put("IMGResult", Constant.RESULT_IMG[item.getImgResult()]);
				listItem.put("Oxygen", item.getOxygen()==0?"--":(item.getOxygen()+"%"));
				listItem.put("PR", item.getPr()==0?"--":(item.getPr()+"/min"));
				listItem.put("PI", item.getPi()==0?"--":("PI "+item.getPi()));
				listItems.add(listItem);
			}
			SwipeMenuListView listView = (SwipeMenuListView) findViewById(R.id.SPO2MainList);
			simpleAdapter = new SimpleAdapter(SPO2Main.this,
					listItems, R.layout.list_spo2, new String[] { "Times", "Dates",
					"IMGResult", "Oxygen", "PR", "PI" }, new int[] {
					R.id.ListSPO2TextTime, R.id.ListSPO2TextDate,
					R.id.ListSPO2ImgResult, R.id.ListSPO2TextOxygen,
					R.id.ListSPO2TextPR, R.id.ListSPO2TextPI });
			listView.setAdapter(simpleAdapter);
			listView.setMenuCreator(CommonCreator.makeSmallSwipeMenuCreator(
					getApplicationContext()));
			listView.setOnMenuItemClickListener(this);

		}else{
			View view = findViewById(R.id.SPO2MainLiearList);
			view.setVisibility(View.INVISIBLE);
		}

	}

	private void refreshNoInfoView() {
		if (Constant.defaultUser.getSpo2list()==null || Constant.defaultUser.getSpo2list().size()==0) {
			NoInfoViewUtils.showNoInfoView(SPO2Main.this, findViewById(R.id.ImgNoInfo));
		}else {
			NoInfoViewUtils.hideNoInfoView(SPO2Main.this, findViewById(R.id.ImgNoInfo));
		}
	}

	@Override
	public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
		// TODO Auto-generated method stub
		if (index == 0) {//如果是第0个按键按下
			List<SPO2Item> list= Constant.defaultUser.getSpo2list();
			list.remove(position);
			FileUtils.saveListToFile(Constant.dir,MeasurementConstant.FILE_NAME_SPO2_LIST, (ArrayList<? extends CommonItem>) list);
			refreshView();
		}

		return false;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if (!hasFocus) {
            if (Constant.defaultUser==null)
                return;
			FileUtils.saveListToFile(Constant.dir
					, MeasurementConstant.FILE_NAME_SPO2_LIST_OLD
					, Constant.defaultUser.getSpo2list());
		}
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
			case R.id.WidgetActionbarBnMenu:
				if(getSupportFragmentManager().getBackStackEntryCount()==0){
					//在主界面
					toggle();
				}else{
					getSupportFragmentManager().popBackStack();
					reFreshActionBarBn(0,false,false);
				}
				break;
			case R.id.WidgetActionbarBnCloud:
				showSettingActivity();
				break;
			default:
				break;
		}
	}

	private void onBnShareClicked() {
		// TODO share
		if(Constant.defaultUser.getSpo2list().size()!=0){
			String SpO2=getJsonString();
			FileDriver.write(Constant.dir, "SPO2.txt", SpO2.getBytes());
			ShareUtils.shareToNet(getApplicationContext(), "SPO2.txt");
		}

	}
	private String getJsonString() {
		StringBuilder  builder=new StringBuilder();
		for (SPO2Item item : Constant.defaultUser.getSpo2list()) {
			builder.append(JsonUtils.makeSpo2String(item)+"\n");
		}
		return builder.toString();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return super.onKeyDown(keyCode, event);
		}
		return false;
	}
}

package com.checkme.azur.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.checkme.newazur.R;
import com.checkme.azur.bluetooth.ReadFileListener;
import com.checkme.azur.element.CommonCreator;
import com.checkme.azur.element.Constant;
import com.checkme.azur.fragment.RightMenuFragment;
import com.checkme.azur.measurement.CommonItem;
import com.checkme.azur.measurement.MeasurementConstant;
import com.checkme.azur.measurement.PedItem;
import com.checkme.azur.tools.NoInfoViewUtils;
import com.checkme.azur.tools.StringMaker;
import com.checkme.azur.utils.CommonItemFilter;
import com.checkme.azur.utils.FileDriver;
import com.checkme.azur.utils.FileUtils;
import com.checkme.azur.utils.LogUtils;
import com.checkme.azur.utils.MsgUtils;
import com.checkme.azur.utils.TimeComparator;

public class PedMain extends BaseActivity implements View.OnClickListener
		, OnMenuItemClickListener, ReadFileListener {

	private static final float ONE_KM_MILE = 0.6214f;
	private static final float ONE_G_OZ = 0.0353f;

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case Constant.MSG_USER_CHOSED:
					getSlidingMenu().showContent();
					new Handler().postDelayed(new Runnable() {
						public void run() {
							getPedList();
						}
					}, 300);
					break;
				case Constant.CMD_TYPE_PED:
					ReadLocalPedList();
					break;
			}
		};
	};

	@Override
	public boolean isTheSameIntent(int menuTag) {
		return (menuTag == Constant.MSG_BNPED_CLICKED);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ped_main);
		setSlidingMenu();
		getPedList();
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
	}


	/**
	 * 设置右边用户列表
	 *
	 */
	public void setSlidingMenu() {
		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT_RIGHT);
		sm.setSecondaryMenu(R.layout.menu_frame_two);
		sm.setSecondaryShadowDrawable(R.drawable.shadowright);

		RightMenuFragment rmf = new RightMenuFragment();
		rmf.setmHandler(handler);
		rmf.setUserList();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame_two, rmf).commit();
	}

	public void getPedList() {
		// 先判断是否缓存有数据
		if (Constant.curUser.getPedList().size() == 0) {// 没有数据，读取
			if (Constant.btConnectFlag == true) {
				Constant.binder.interfaceReadFile(Constant.curUser.getUserInfo().getID()
								+ MeasurementConstant.FILE_NAME_PED_LIST
						, Constant.CMD_TYPE_PED, 5000, this);
			} else {
				ReadLocalPedList();
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

	public void ReadLocalPedList() {

		//读取上次退出是存储的旧列表
		List<PedItem> oldPedItems = FileUtils.readPedList(Constant.dir
				, Constant.curUser.getUserInfo().getID()
						+ MeasurementConstant.FILE_NAME_PED_LIST_OLD);
		if (oldPedItems != null && oldPedItems.size() != 0) {
			Constant.curUser.addPedList(oldPedItems);
		}
		//读取新下载的列表，合并到旧列表
//		if (Constant.btConnectFlag) {
			List<PedItem> newPedItems = FileUtils.readPedList(Constant.dir
					, Constant.curUser.getUserInfo().getID()
							+ MeasurementConstant.FILE_NAME_PED_LIST);
			if (newPedItems != null && newPedItems.size() != 0) {
				Constant.curUser.addPedList(newPedItems);
			}
//		}
		//去除重复，并倒序排列
		CommonItemFilter.removeSameItems(Constant.curUser.getPedList());
		Collections.sort((List<CommonItem>)Constant.curUser
						.getList(MeasurementConstant.CMD_TYPE_PED)
				, new TimeComparator());

		refreshView();
	}

	public void refreshView() {
		((ProgressBar) findViewById(R.id.PedMainListPro))
				.setVisibility(View.INVISIBLE);

		if(Constant.curUser != null && !TextUtils.isEmpty(Constant.curUser.getUserInfo().getName().trim())) {
			reFreshTitle(getResources().getString(R.string.title_ped)
					+ " - " + Constant.curUser.getUserInfo().getName());
		} else {
			reFreshTitle(getResources().getString(R.string.title_ped));
		}

		reFreshActionBarBn(0, true, false);
		processPedList();
		refreshNoInfoView();
	}

	public void processPedList() {
		if (Constant.curUser.getPedList().size() != 0) {
			List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
			ArrayList<PedItem> list = Constant.curUser.getPedList();
			for (int i = 0; i < list.size(); i++) {
				PedItem item = list.get(i);
				Map<String, Object> listItem = new HashMap<String, Object>();
				listItem.put("Times",StringMaker.makeTimeString(item.getDate()));
				listItem.put("Dates",StringMaker.makeDateString(item.getDate()));
				listItem.put("Steps", item.getSteps() + Constant.getString(R.string.steps));
				//区分公制和英制
				if (Constant.unit == Constant.UNIT_METRIC) {
					listItem.put("Dis", item.getDistance() + "km");
					listItem.put("Speed", item.getSpeed() + "km/h");
					listItem.put("Fat", item.getFat() + "g");
				}else {
					listItem.put("Dis", String.format("%.2f", item.getDistance()*ONE_KM_MILE) + "mi");
					listItem.put("Speed",String.format("%.1f", item.getSpeed()*ONE_KM_MILE) + "mi/h");
					listItem.put("Fat", String.format("%.2f", item.getFat()*ONE_G_OZ ) + "oz");
				}
				listItem.put("Calorie", item.getCalorie() + "Kcal");
				listItem.put("TotalTime", StringMaker.makeSecondToMinute(item.getTotalTime()));
				listItems.add(listItem);
			}
			SwipeMenuListView listView = (SwipeMenuListView) findViewById(R.id.PedMainList);
			SimpleAdapter simpleAdapter = new SimpleAdapter(PedMain.this,
					listItems, R.layout.list_ped, new String[] { "Times",
					"Dates", "Steps", "Dis", "Calorie", "Speed", "Fat",
					"TotalTime" }, new int[] { R.id.ListPedTextTime,
					R.id.ListPedTextDate, R.id.ListPedTextSteps,
					R.id.ListPedTextDis, R.id.ListPedTextCalorie,
					R.id.ListPedTextSpeed, R.id.ListPedTextFat,
					R.id.ListPedTextTotalTime });
			listView.setAdapter(simpleAdapter);
			listView.setMenuCreator(CommonCreator.makeSwipeMenuCreator(
					getApplicationContext()));
			listView.setOnMenuItemClickListener(this);
			listView.setVisibility(View.VISIBLE);
		} else {
			ListView listView = (ListView) findViewById(R.id.PedMainList);
			listView.setVisibility(View.INVISIBLE);
//			listView.setAdapter(null);
		}

	}

	private void refreshNoInfoView(){
		if (Constant.curUser.getPedList()==null || Constant.curUser.getPedList().size()==0) {
			NoInfoViewUtils.showNoInfoView(PedMain.this, findViewById(R.id.ImgNoInfo));
		}else {
			NoInfoViewUtils.hideNoInfoView(PedMain.this, findViewById(R.id.ImgNoInfo));
		}
	}

	/**
	 * 保存当前用户dlc列表
	 */
	private void saveCurUserPedList() {
		LogUtils.d("保存ped列表，用户ID：" + Constant.curUser.getUserInfo().getID());
		FileUtils.saveListToFileWithoutUndownloaded(Constant.dir
				, Constant.curUser.getUserInfo().getID()
						+ MeasurementConstant.FILE_NAME_PED_LIST_OLD
				, Constant.curUser.getPedList());
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		super.onClick(view);
		switch (view.getId()) {
			case R.id.WidgetActionbarBnUser:
				showSecondaryMenu();
				//切换用户前保存当前ped列表
				saveCurUserPedList();
				break;
			default:
				break;
		}
	}

	@Override
	public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
		// TODO Auto-generated method stub
		if (index == 0) {//如果是第0个按键按下
			List<PedItem> list= Constant.curUser.getPedList();
			list.remove(position);
			FileUtils.saveListToFile(Constant.dir,Constant.curUser.getUserInfo().getID()
					+ MeasurementConstant.FILE_NAME_PED_LIST, (ArrayList<? extends CommonItem>) list);
			refreshView();
		}

		return false;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if (!hasFocus) {
			if (Constant.curUser==null)
				return;
			saveCurUserPedList();
		}
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

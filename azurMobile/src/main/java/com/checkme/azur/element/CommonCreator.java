package com.checkme.azur.element;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.checkme.newazur.R;

public class CommonCreator {

	private static Context mContext;

	/**
	 * 生成通用的SwipeMenuCreator
	 * @param context
	 * @return
	 */
	public static SwipeMenuCreator makeSwipeMenuCreator(Context context) {

		if (context == null) {
			return null;
		}
		mContext = context;
		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {
				// create "delete" item
				SwipeMenuItem deleteItem = new SwipeMenuItem(mContext);
				// set item background
				deleteItem.setBackground(new ColorDrawable(Color.TRANSPARENT));
				// set item width
				deleteItem.setWidth(190);//warning test 90
				// set a icon
				deleteItem.setIcon(R.drawable.delete);
				// add to menu
				menu.addMenuItem(deleteItem);
			}
		};
		return creator;
	}

	/**
	 * 生成通用的SwipeMenuCreator
	 * @param context
	 * @return
	 */
	public static SwipeMenuCreator makeSmallSwipeMenuCreator(Context context) {

		if (context == null) {
			return null;
		}
		mContext = context;
		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {
				// create "delete" item
				SwipeMenuItem deleteItem = new SwipeMenuItem(mContext);
				// set item background
				deleteItem.setBackground(new ColorDrawable(Color.TRANSPARENT));
				// set item width
				deleteItem.setWidth(130);//warning test 90
				// set a icon
				deleteItem.setIcon(R.drawable.delete_small);
				// add to menu
				menu.addMenuItem(deleteItem);
			}
		};
		return creator;
	}
}

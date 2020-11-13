package com.viatom.azur.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CommonViewPager extends ViewPager {

	private boolean isCanScroll = false; // 禁止滑动切换


	public CommonViewPager(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CommonViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isCanScroll == false) {
			return false;
		} else {
			return super.onTouchEvent(ev);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (isCanScroll == false) {
			return false;
		} else {
			return super.onInterceptTouchEvent(ev);
		}

	}

	public void setCanScroll(boolean isCanScroll) {
		this.isCanScroll = isCanScroll;
	}


}

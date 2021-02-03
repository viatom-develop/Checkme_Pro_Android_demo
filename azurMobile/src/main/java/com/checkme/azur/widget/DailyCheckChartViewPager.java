package com.checkme.azur.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class DailyCheckChartViewPager extends ViewPager {
	  
    private boolean isCanScroll = false;  
  
    public DailyCheckChartViewPager(Context context) {  
        super(context);  
    }  
  
    public DailyCheckChartViewPager(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    public void setScanScroll(boolean isCanScroll){  
        this.isCanScroll = isCanScroll;  
    }  
  
  
//    @Override  
//    public void scrollTo(int x, int y){  
//        if (isCanScroll){  
//            super.scrollTo(x, y);  
//        }  
//    }
    
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
}

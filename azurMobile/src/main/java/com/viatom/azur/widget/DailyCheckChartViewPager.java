package com.viatom.azur.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

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

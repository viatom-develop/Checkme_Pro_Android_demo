package com.checkme.azur.monitor.widget;

import android.content.Context;
import android.util.AttributeSet;

public class OxiWaveView extends RTWaveView {

	public OxiWaveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initGestureDetector(context);
	}

	public OxiWaveView(Context context, WaveViewParameters parameters) {
		super(context, parameters);
		// TODO Auto-generated constructor stub
		initGestureDetector(context);
	}

}

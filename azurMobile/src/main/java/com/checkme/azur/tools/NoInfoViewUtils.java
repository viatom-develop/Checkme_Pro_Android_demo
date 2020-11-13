package com.checkme.azur.tools;

import android.content.Context;
import android.view.View;

public class NoInfoViewUtils {
	
	public static void showNoInfoView(Context context, View view) {
		if (context==null || view==null) {
			return;
		}
		view.setVisibility(View.VISIBLE);
	}
	
	public static void hideNoInfoView(Context context, View view) {
		if (context==null || view==null) {
			return;
		}
		view.setVisibility(View.INVISIBLE);
	}
	
}

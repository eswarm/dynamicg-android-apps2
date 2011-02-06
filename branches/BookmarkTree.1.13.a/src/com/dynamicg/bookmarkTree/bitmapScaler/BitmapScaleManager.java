package com.dynamicg.bookmarkTree.bitmapScaler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.dynamicg.bookmarkTree.prefs.PreferencesWrapper;

public class BitmapScaleManager {

	public static final int DFLT_DENSITY = 160;
	
	private static boolean enabledForList; 
	
	public static void init() {
		enabledForList = PreferencesWrapper.scaleIcons.isOn();
	}
	
	// for list view provider 
	public static Bitmap getIcon(byte[] blob) {
		if (blob==null) {
			return null;
		}
		
		Bitmap b = BitmapFactory.decodeByteArray(blob, 0, blob.length);
		if (b==null) {
			// error report Dec 28, 2010 8:35:05 PM
			return null;
		}
		if (enabledForList) {
			b.setDensity(DFLT_DENSITY);
		}
		return b;
	}
	
}

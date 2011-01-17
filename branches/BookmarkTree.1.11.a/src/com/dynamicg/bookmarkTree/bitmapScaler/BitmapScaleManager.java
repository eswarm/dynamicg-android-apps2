package com.dynamicg.bookmarkTree.bitmapScaler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.dynamicg.bookmarkTree.prefs.PreferencesWrapper;

/*
 * Bitmap.setDensity() is API4 so we need to wrap
 */
public class BitmapScaleManager {

	private static ScaleWorker scaleWorker;
	private static boolean enabledForList; 
	private static boolean isApi3=false;
	
	public static interface ScaleWorker {
		public final int DFLT_DENSITY = 160;
		public Bitmap scaleForList(Bitmap b); // returns 'b' for chaining
		public void setDensity(Bitmap b, int density);
		public int getDensity(Bitmap b);
	}
	
	private static void createWorker() {
		if (scaleWorker!=null) {
			return;
		}
		try {
			Class<?> scaleWorkerClass = Class.forName("com.dynamicg.bookmarkTree.bitmapScaler.BitmapScalerAPI4");
			scaleWorker = (ScaleWorker)scaleWorkerClass.newInstance();
		}
		catch (Throwable e) {
			isApi3 = true;
			scaleWorker = new BitmapScalerAPI3();
		}
	}
	
	public static void init() {
		enabledForList = PreferencesWrapper.scaleIcons.isOn();
		if (enabledForList && scaleWorker==null) {
			createWorker();
		}
	}
	
	/*
	 * for list view provider 
	 */
	public static Bitmap getIcon(byte[] blob) {
		if (blob==null) {
			return null;
		}
		
		Bitmap b = BitmapFactory.decodeByteArray(blob, 0, blob.length);
		if (b==null) {
			// error report Dec 28, 2010 8:35:05 PM
			return null;
		}
		if (enabledForList && !isApi3) {
			return scaleWorker.scaleForList(b);
		}
		return b;
	}
	
	/*
	 * for shortcut creation
	 */
	public static void scale(Bitmap b, int density) {
		createWorker();
		scaleWorker.setDensity(b, density);
	}
	public static int getDensity(Bitmap b) {
		createWorker();
		return scaleWorker.getDensity(b);
	}
	
}

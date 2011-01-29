package com.dynamicg.bookmarkTree.bitmapScaler;

import android.graphics.Bitmap;

import com.dynamicg.bookmarkTree.bitmapScaler.BitmapScaleManager.ScaleWorker;

public class BitmapScalerAPI4 implements ScaleWorker {

	public BitmapScalerAPI4() {
	}
	
	@Override
	public Bitmap scaleForList(Bitmap b) {
		b.setDensity(DFLT_DENSITY);
		return b;
	}

	@Override
	public void setDensity(Bitmap b, int density) {
		b.setDensity(density);
	}
	
	@Override
	public int getDensity(Bitmap b) {
		return b.getDensity();
	}
	
}

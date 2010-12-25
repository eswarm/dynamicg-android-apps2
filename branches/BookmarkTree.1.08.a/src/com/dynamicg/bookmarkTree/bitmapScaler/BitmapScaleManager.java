package com.dynamicg.bookmarkTree.bitmapScaler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;

public class BitmapScaleManager {

	private static final ScaleWorker scaleWorker;
	
	static {
		if (!BookmarkTreeContext.preferencesWrapper.isScaleIcons()) {
			scaleWorker = null;
		}
		else {
			Class<?> scaleWorkerClass;
			ScaleWorker localScaleWorker;
			try {
				scaleWorkerClass = Class.forName("com.dynamicg.bookmarkTree.bitmapScaler.BitmapScalerAPI4");
				localScaleWorker = (ScaleWorker)scaleWorkerClass.newInstance();
			}
			catch (Throwable e) {
				localScaleWorker = null;
			}
			scaleWorker = localScaleWorker;
		}
	}

	public static Bitmap getIcon(byte[] blob) {
		if (blob==null) {
			return null;
		}
		
		Bitmap b = BitmapFactory.decodeByteArray(blob, 0, blob.length);
		if (scaleWorker!=null) {
			return scaleWorker.scale(b);
		}
		return b;
	}
	
	public static interface ScaleWorker {
		public Bitmap scale(Bitmap b);
	}

}

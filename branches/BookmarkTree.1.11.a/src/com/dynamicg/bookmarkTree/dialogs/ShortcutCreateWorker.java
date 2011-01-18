package com.dynamicg.bookmarkTree.dialogs;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.net.Uri;

import com.dynamicg.bookmarkTree.R;
import com.dynamicg.bookmarkTree.bitmapScaler.BitmapScaleManager;
import com.dynamicg.common.ContextUtil;

public class ShortcutCreateWorker {

	private static final String LAUNCH_ACTION = "com.android.launcher.action.INSTALL_SHORTCUT";
	
	private final Context context;
	
	public ShortcutCreateWorker(Context context) {
		this.context = context;
	}
	
	public Bitmap getIcon(Bitmap favicon, int bgcolor, int targetDensity) {
		
		// see http://developer.android.com/guide/topics/graphics/index.html
		final int size = ContextUtil.getDimension(context, R.dimen.iconSize);
		final Bitmap target = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas();
		
		canvas.setBitmap(target);
		
		// solid background
		RectF rect = new RectF(0,0,size,size);
		Paint paint = new Paint();
		paint.setColor(bgcolor);
		paint.setStyle(Style.FILL);
		canvas.drawRoundRect(rect, 6, 6, paint);
		
		// scale favicon
		int currentDensity = BitmapScaleManager.getDensity(favicon);
		BitmapScaleManager.scale(favicon, targetDensity);
		
		// draw centered
		float densityPatch = (float)currentDensity / (float)targetDensity;
		float xOffset = (size - favicon.getWidth()*densityPatch) / 2;
		float yOffset = (size - favicon.getHeight()*densityPatch) / 2;
//		System.err.println( "DENSITY-PATCH="+densityPatch);
//		System.err.println( "OFFSETS="+xOffset+"/"+yOffset);
		canvas.drawBitmap(favicon, xOffset, yOffset, null);
		
		return target;
		
	}
	
	public void create(Bitmap icon, String title, String url) {
		
		/*
		 * the actual shortcut action (i.e. open web link) 
		 */
		Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);
		shortcutIntent.setData(Uri.parse(url));
		
		/*
		 * shortcut creation intent
		 */
		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
		intent.setAction(LAUNCH_ACTION);
		context.sendBroadcast(intent);
		
	}

}

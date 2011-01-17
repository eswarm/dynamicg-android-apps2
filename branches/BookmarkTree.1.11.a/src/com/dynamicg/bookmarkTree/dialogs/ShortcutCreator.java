package com.dynamicg.bookmarkTree.dialogs;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Parcelable;

import com.dynamicg.bookmarkTree.bitmapScaler.BitmapScaleManager;
import com.dynamicg.bookmarkTree.model.Bookmark;
import com.dynamicg.common.ContextUtil;

public class ShortcutCreator {

	private static final String LAUNCH_ACTION = "com.android.launcher.action.INSTALL_SHORTCUT";
	private static final int SIZE = 50;
	
	private final Context context;
	private final Bookmark bookmark;
	
	public ShortcutCreator(Context context, Bookmark bookmark) {
		this.context = context;
		this.bookmark = bookmark;
	}
	
	private Parcelable getIcon(int bgcolor, int targetDensity) {
		
		// see http://developer.android.com/guide/topics/graphics/index.html
		final int size = ContextUtil.getScaledSizeInt(context,SIZE);
		final Bitmap target = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		
		final Bitmap favicon = bookmark.getFavicon();
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
	
	public void create() {
		/*
		 * the actual shortcut action (i.e. open web link) 
		 */
		Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);
		shortcutIntent.setData(Uri.parse(bookmark.getUrl()));
		
		/*
		 * shortcut items
		 * TODO - pick these from dialog
		 */
		String title = bookmark.getDisplayTitle();
		int density = 120;
		int bgcolor = Color.rgb(127,127,127);
		
		/*
		 * shortcut creation intent
		 */
		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, getIcon(bgcolor,density) );
		intent.setAction(LAUNCH_ACTION);
		context.sendBroadcast(intent);
	}

}

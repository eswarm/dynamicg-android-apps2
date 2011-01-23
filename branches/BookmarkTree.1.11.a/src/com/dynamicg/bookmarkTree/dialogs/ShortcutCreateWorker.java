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
import com.dynamicg.common.Logger;

public class ShortcutCreateWorker {
	
	private static final Logger log = new Logger(ShortcutCreateWorker.class);
	private static final String LAUNCH_ACTION = "com.android.launcher.action.INSTALL_SHORTCUT";
	
	private final Context context;
	
	public ShortcutCreateWorker(Context context) {
		this.context = context;
	}
	
	public Bitmap getIcon(Bitmap originalFavicon, int bgcolor, int targetDensity) {
		
		// see http://developer.android.com/guide/topics/graphics/index.html
		final int shortcutIconSize = ContextUtil.getDimension(context, R.dimen.shortcutIconSize);
		final Bitmap target = Bitmap.createBitmap(shortcutIconSize, shortcutIconSize, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas();
		
		canvas.setBitmap(target);
		
		// solid background
		RectF rect = new RectF(0,0,shortcutIconSize,shortcutIconSize);
		Paint paint = new Paint();
		paint.setColor(bgcolor);
		paint.setStyle(Style.FILL);
		canvas.drawRoundRect(rect, 6, 6, paint);
		
		// scale favicon
		// => note we copy the icon first as we're going to overwrite the density
		Bitmap favicon = originalFavicon.copy(originalFavicon.getConfig(), true);
		int currentDensity = BitmapScaleManager.getDensity(favicon);
		BitmapScaleManager.scale(favicon, targetDensity);
		
		// draw centered
		float densityPatch = (float)currentDensity / (float)targetDensity;
		float xOffset = (shortcutIconSize - favicon.getWidth()*densityPatch) / 2;
		float yOffset = (shortcutIconSize - favicon.getHeight()*densityPatch) / 2;
		if (log.debugEnabled) {
			log.debug("shortcutIconSize", shortcutIconSize);
			log.debug("favicon original width/height", favicon.getWidth(), favicon.getHeight());
			log.debug("densityPatch", densityPatch);
			log.debug("favicon patched width/height", favicon.getWidth()*densityPatch, favicon.getHeight()*densityPatch );
			log.debug("offset", xOffset, yOffset);
		}

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

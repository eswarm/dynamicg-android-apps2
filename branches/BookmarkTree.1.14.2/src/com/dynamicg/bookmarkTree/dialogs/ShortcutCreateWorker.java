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
import com.dynamicg.common.ContextUtil;
import com.dynamicg.common.Logger;
import com.dynamicg.common.SystemUtil;

public class ShortcutCreateWorker {
	
	private static final Logger log = new Logger(ShortcutCreateWorker.class);
	private static final String LAUNCH_ACTION = "com.android.launcher.action.INSTALL_SHORTCUT";
	private static final int CORNER_DIM = 8;
	
	private final Context context;
	
	public ShortcutCreateWorker(Context context) {
		this.context = context;
	}
	
	public Bitmap getIcon(Bitmap originalFavicon, int bgcolor, int targetDensity) {
		
		// see http://developer.android.com/guide/topics/graphics/index.html
		final int shortcutIconSize = ContextUtil.getDimension(context, R.dimen.shortcutIconSize);
		final Bitmap target = Bitmap.createBitmap ( shortcutIconSize, shortcutIconSize, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas();
		
		canvas.setBitmap(target);
		
		// solid background
		RectF rect = new RectF(0,0,shortcutIconSize,shortcutIconSize);
		Paint paint = new Paint();
		paint.setColor(bgcolor);
		paint.setStyle(Style.FILL);
		
		int roundedCorner = ContextUtil.getScaledSizeInt(context, CORNER_DIM);
		canvas.drawRoundRect(rect, roundedCorner, roundedCorner, paint);
		
		if (originalFavicon==null) {
			// prevent NPE
			return target;
		}
		
		// scale favicon
		// => note we copy the icon first as we're going to overwrite the density
		Bitmap favicon = originalFavicon.copy(originalFavicon.getConfig(), true);
		int currentDensity = favicon.getDensity();
		favicon.setDensity(targetDensity);
		
		// draw centered
		final float densityPatch = (float)currentDensity / (float)targetDensity;
//		final float iconW = ContextUtil.getScaledSizeInt(context, favicon.getWidth());
//		final float iconH = ContextUtil.getScaledSizeInt(context, favicon.getHeight());
		final float iconW = favicon.getWidth();
		final float iconH = favicon.getHeight();
		final float patchedIconW = iconW * densityPatch;
		final float patchedIconH = iconH * densityPatch;

//		final float xOffset = ContextUtil.getUnscaledSizeInt(context, (shortcutIconSize - patchedIconW)/2f );
//		final float yOffset = ContextUtil.getUnscaledSizeInt(context, (shortcutIconSize - patchedIconH)/2f );
		final float shortcutIconSizeUnscaled = ContextUtil.getUnscaledSizeInt(context, shortcutIconSize);
		final float xOffset = (shortcutIconSizeUnscaled - patchedIconW) / 2f;
		final float yOffset = (shortcutIconSizeUnscaled - patchedIconH) / 2f;
		
		if (log.debugEnabled) {
			log.debug("--> dimensions");
			log.debug("current/target density & patch", currentDensity, targetDensity, densityPatch);
			log.debug("shortcutIconSize", shortcutIconSize, shortcutIconSizeUnscaled);
			log.debug("favicon original width/height", iconW, iconH);
			log.debug("favicon scaled width/height", patchedIconW, patchedIconH);
//			log.debug("favicon patched width/height", patchedIconW, patchedIconH );
			log.debug("offset", xOffset, yOffset);
		}

		canvas.drawBitmap ( favicon
				, (float)ContextUtil.getScaledSizeInt(context, xOffset)
				, (float)ContextUtil.getScaledSizeInt(context, yOffset)
				, null
				);
		
		return target;
		
	}
	
	public Bitmap getIcon2(Bitmap originalFavicon, int bgcolor, int targetDensity) {
		
		// see http://developer.android.com/guide/topics/graphics/index.html
		final float shortcutIconSize = (float)ContextUtil.getDimension(context, R.dimen.shortcutIconSize);
		final Bitmap target = Bitmap.createBitmap ( (int)shortcutIconSize, (int)shortcutIconSize, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas();
		
		canvas.setBitmap(target);
		
		// solid background
		RectF rect = new RectF(0,0,shortcutIconSize,shortcutIconSize);
		Paint paint = new Paint();
		paint.setColor(bgcolor);
		paint.setStyle(Style.FILL);
		
		int roundedCorner = ContextUtil.getScaledSizeInt(context, CORNER_DIM);
		canvas.drawRoundRect(rect, roundedCorner, roundedCorner, paint);
		
		if (originalFavicon==null) {
			// prevent NPE
			return target;
		}
		
		// scale favicon
		// => note we copy the icon first as we're going to overwrite the density
		Bitmap favicon = originalFavicon.copy(originalFavicon.getConfig(), true);
		int currentDensity = favicon.getDensity();
		favicon.setDensity(targetDensity);
		
		// draw centered
		final float densityPatch = (float)currentDensity / (float)targetDensity;
		final float scaledIconW = ContextUtil.getScaledSizeInt(context, favicon.getWidth());
		final float scaledIconH = ContextUtil.getScaledSizeInt(context, favicon.getHeight());
		final float patchedIconW = scaledIconW * densityPatch;
		final float patchedIconH = scaledIconH * densityPatch;
		final float xOffset = (shortcutIconSize - patchedIconW) / 2f;
		final float yOffset = (shortcutIconSize - patchedIconH) / 2f;
		
//		float xOffsetUnscaled = ContextUtil.getUnscaledSizeInt(context, xOffset);
//		float yOffsetUnscaled = ContextUtil.getUnscaledSizeInt(context, xOffset);
		
		if (log.debugEnabled) {
			log.debug("--> dimensions");
			log.debug("shortcutIconSize", shortcutIconSize);
			log.debug("current/target density & patch", currentDensity, targetDensity, densityPatch);
			log.debug("favicon original width/height", favicon.getWidth(), favicon.getHeight());
			log.debug("favicon scaled width/height", scaledIconW, scaledIconH);
			log.debug("favicon patched width/height", patchedIconW, patchedIconH );
			log.debug("offset", xOffset, yOffset);
//			log.debug("unscaled offset", xOffsetUnscaled, yOffsetUnscaled);
		}

		canvas.drawBitmap ( favicon
				, xOffset
				, yOffset
				, null
				);
		
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

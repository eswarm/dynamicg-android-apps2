package com.dynamicg.bookmarkTree.data;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.Browser;

import com.dynamicg.bookmarkTree.model.BrowserBookmarkBean;
import com.dynamicg.common.Logger;

public class BrowserBookmarkLoader {

	private static final Logger log = new Logger(BrowserBookmarkLoader.class);

	public static final int FOR_BATCH = 0;
	public static final int FOR_BACKUP = 1;
	public static final int FOR_DISPLAY = 2;
	
	private static String EMPTY = "";
	
	private static Bitmap getFavicon(byte[] blob) {
		if (blob==null) {
			return null;
		}
		return BitmapFactory.decodeByteArray(blob, 0, blob.length); 
	}

	private static String nvl(String value) {
		return value==null?EMPTY:value;
	}
	
	public static ArrayList<BrowserBookmarkBean> loadBrowserBookmarks(Activity main, int what) {

		Uri bookmarksURI = android.provider.Browser.BOOKMARKS_URI;
		String[] columns = new String[] {
				Browser.BookmarkColumns._ID
				, Browser.BookmarkColumns.CREATED
				, Browser.BookmarkColumns.TITLE
				, Browser.BookmarkColumns.URL
				, Browser.BookmarkColumns.FAVICON 
		};
		String query = Browser.BookmarkColumns.BOOKMARK+"=1"; // query on bookmarks only, skip history
		Cursor crs = main.managedQuery ( bookmarksURI
				, columns
				, query
				, null
				, Browser.BookmarkColumns.TITLE
		);

		ArrayList<BrowserBookmarkBean> rows = new ArrayList<BrowserBookmarkBean>();
		
		// see error report "Aug 13, 2010 10:19:37 PM"
		if (crs==null) {
			return rows;
		}
		
		BrowserBookmarkBean row;
		while ( crs.moveToNext() ) {
			
			row = new BrowserBookmarkBean(); 
			row.id = crs.getInt(0);
			if (what==FOR_BACKUP) {
				row.created = crs.getLong(1);
			}
			
			// mask nulls - we got one error report with an NPE on bookmark title (?)
			row.fullTitle = nvl(crs.getString(2));
			row.url = nvl(crs.getString(3));
			
			if (what==FOR_DISPLAY) {
				row.favicon = getFavicon(crs.getBlob(4));
			}
			else if (what==FOR_BACKUP) {
				row.faviconData = crs.getBlob(4);
			}
			
			if (log.traceEnabled) {
				log.debug("loadBrowserBookmarks",row.getFullTitle(),row.getUrl());
			}
			rows.add(row);
		}
		
		if (!crs.isClosed()) {
			crs.close();
		}
		
		return rows;
	}
	
}

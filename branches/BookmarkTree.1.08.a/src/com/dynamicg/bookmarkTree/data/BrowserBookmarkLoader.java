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

	private static Bitmap getFavicon(byte[] blob) {
		if (blob==null) {
			return null;
		}
		return BitmapFactory.decodeByteArray(blob, 0, blob.length); 
	}

	public static ArrayList<BrowserBookmarkBean> loadBrowserBookmarks(Activity main, boolean resolveImage) {

		Uri bookmarksURI = android.provider.Browser.BOOKMARKS_URI;
		String[] columns = new String[] { Browser.BookmarkColumns._ID
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
			row.created = crs.getLong(1);
			row.fullTitle = crs.getString(2);
			row.url = crs.getString(3);
			if (resolveImage) {
				row.favicon = getFavicon(crs.getBlob(4));
			}
			else {
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

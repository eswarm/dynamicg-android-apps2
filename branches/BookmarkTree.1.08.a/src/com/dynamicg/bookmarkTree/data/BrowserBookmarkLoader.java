package com.dynamicg.bookmarkTree.data;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.Browser;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;
import com.dynamicg.bookmarkTree.model.BrowserBookmarkBean;
import com.dynamicg.bookmarkTree.model.RawDataBean;
import com.dynamicg.common.Logger;

public class BrowserBookmarkLoader {

	private static final Logger log = new Logger(BrowserBookmarkLoader.class);

	private static final int FOR_DISPLAY = 1;
	private static final int FOR_INTERNAL_OP = 2;
	private static final int FOR_BACKUP_RESTORE = 3;
	
	private static String EMPTY = "";
	
	private static Bitmap getFavicon(byte[] blob) {
		if (blob==null) {
			return null;
		}
		return BitmapFactory.decodeByteArray(blob, 0, blob.length); 
	}

	private static String nvl(String value) {
		// mask nulls - we got one error report with an NPE on bookmark title (?)
		return value==null?EMPTY:value;
	}
	
	@SuppressWarnings({ "unchecked" })
	public static ArrayList<BrowserBookmarkBean> forListAdapter(BookmarkTreeContext ctx) {
		return (ArrayList<BrowserBookmarkBean>)readBrowserBookmarks(ctx.activity, FOR_DISPLAY);
	}
	
	@SuppressWarnings({ "unchecked" })
	public static ArrayList<RawDataBean> forInternalOps(BookmarkTreeContext ctx) {
		return (ArrayList<RawDataBean>)readBrowserBookmarks(ctx.activity, FOR_INTERNAL_OP);
	}
	
	@SuppressWarnings({ "unchecked" })
	public static ArrayList<RawDataBean> forBackup(BookmarkTreeContext ctx) {
		return (ArrayList<RawDataBean>)readBrowserBookmarks(ctx.activity, FOR_BACKUP_RESTORE);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ArrayList readBrowserBookmarks(Activity main, int what) {

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

		ArrayList rows = new ArrayList();
		
		// see error report "Aug 13, 2010 10:19:37 PM"
		if (crs==null) {
			return rows;
		}
		
		if (what==FOR_BACKUP_RESTORE || what==FOR_INTERNAL_OP) {
			RawDataBean bean;
			while ( crs.moveToNext() ) {
				bean = new RawDataBean();
				bean.id = crs.getInt(0);
				bean.created = crs.getLong(1);
				bean.fullTitle = nvl(crs.getString(2));
				bean.url = nvl(crs.getString(3));
				bean.favicon = crs.getBlob(4);
				
				rows.add(bean);
				if (log.traceEnabled) {
					log.debug("loadBrowserBookmarks", bean.fullTitle, bean.url, bean.created);
				}
			}
		}
		else {
			// display
			BrowserBookmarkBean bean;
			while ( crs.moveToNext() ) {
				bean = new BrowserBookmarkBean(); 
				bean.id = crs.getInt(0);
				bean.fullTitle = nvl(crs.getString(2));
				bean.url = nvl(crs.getString(3));
				if (what==FOR_DISPLAY) {
					bean.favicon = getFavicon(crs.getBlob(4));
				}
				
				rows.add(bean);
				if (log.traceEnabled) {
					log.debug("loadBrowserBookmarks", bean.id, bean.fullTitle, bean.url);
				}
			}
		}
		
		if (!crs.isClosed()) {
			crs.close();
		}
		
		return rows;
	}
	
}

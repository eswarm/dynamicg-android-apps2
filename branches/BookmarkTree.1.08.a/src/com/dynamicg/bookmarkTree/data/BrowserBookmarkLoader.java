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
import com.dynamicg.bookmarkTree.model.RawBackupDataBean;
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
	public static ArrayList<BrowserBookmarkBean> forInternalOp(BookmarkTreeContext ctx) {
		return (ArrayList<BrowserBookmarkBean>)readBrowserBookmarks(ctx.activity, FOR_INTERNAL_OP);
	}
	
	@SuppressWarnings({ "unchecked" })
	public static ArrayList<RawBackupDataBean> forBackup(BookmarkTreeContext ctx) {
		return (ArrayList<RawBackupDataBean>)readBrowserBookmarks(ctx.activity, FOR_BACKUP_RESTORE);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ArrayList<?> readBrowserBookmarks(Activity main, int what) {

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

		ArrayList rows = what==FOR_BACKUP_RESTORE ? new ArrayList<RawBackupDataBean>() : new ArrayList<BrowserBookmarkBean>();
		
		// see error report "Aug 13, 2010 10:19:37 PM"
		if (crs==null) {
			return rows;
		}
		
		BrowserBookmarkBean databean;
		RawBackupDataBean backupbean;
		
		while ( crs.moveToNext() ) {
			
			if (what==FOR_BACKUP_RESTORE) {
				backupbean = new RawBackupDataBean();
				backupbean.created = crs.getLong(1);
				backupbean.fullTitle = nvl(crs.getString(2));
				backupbean.url = nvl(crs.getString(3));
				backupbean.favicon = crs.getBlob(4);
				rows.add(backupbean);
				if (log.traceEnabled) {
					log.debug("loadBrowserBookmarks", backupbean.fullTitle, backupbean.url, backupbean.created);
				}
			}
			else {
				// "batch" and "display"
				databean = new BrowserBookmarkBean(); 
				databean.id = crs.getInt(0);
				databean.fullTitle = nvl(crs.getString(2));
				databean.url = nvl(crs.getString(3));
				if (what==FOR_DISPLAY) {
					databean.favicon = getFavicon(crs.getBlob(4));
				}
				rows.add(databean);
				if (log.traceEnabled) {
					log.debug("loadBrowserBookmarks", databean.id, databean.fullTitle, databean.url);
				}
			}
			
		}
		
		if (!crs.isClosed()) {
			crs.close();
		}
		
		return rows;
	}
	
}

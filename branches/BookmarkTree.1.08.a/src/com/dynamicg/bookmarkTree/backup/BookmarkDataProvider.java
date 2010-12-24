package com.dynamicg.bookmarkTree.backup;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Browser;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;
import com.dynamicg.common.Logger;

public class BookmarkDataProvider {

	private static final Logger log = new Logger(BookmarkDataProvider.class);
	
	private static final Uri BOOKMARKS_URI = android.provider.Browser.BOOKMARKS_URI;
	
	public static ArrayList<RawDataBean> readBrowserBookmarks(BookmarkTreeContext ctx) {

		String[] columns = new String[] { Browser.BookmarkColumns._ID
				, Browser.BookmarkColumns.CREATED
				, Browser.BookmarkColumns.TITLE
				, Browser.BookmarkColumns.URL
				, Browser.BookmarkColumns.FAVICON 
		};
		String query = Browser.BookmarkColumns.BOOKMARK+"=1"; // query on bookmarks only, skip history
		Cursor crs = ctx.activity.managedQuery ( BOOKMARKS_URI
				, columns
				, query
				, null
				, Browser.BookmarkColumns.TITLE
		);

		ArrayList<RawDataBean> rows = new ArrayList<RawDataBean>();
		
		// see error report "Aug 13, 2010 10:19:37 PM"
		if (crs==null) {
			return rows;
		}
		
		RawDataBean b;
		while ( crs.moveToNext() ) {
			b = new RawDataBean(); 
			b.id = crs.getLong(0);
			b.created = crs.getLong(1);
			b.title = crs.getString(2);
			b.url = crs.getString(3);
			b.favicon = crs.getBlob(4);
			rows.add(b);
		}
		
		if (!crs.isClosed()) {
			crs.close();
		}
		
		return rows;

	}

	private static ContentValues[] transform(ArrayList<RawDataBean> rows) {
		ArrayList<ContentValues> list = new ArrayList<ContentValues>();
		ContentValues entry;
		for (RawDataBean b:rows) {
			entry = new ContentValues();
			
			entry.put(Browser.BookmarkColumns.BOOKMARK, 1);
			entry.put(Browser.BookmarkColumns.CREATED, b.created);
			entry.put(Browser.BookmarkColumns.TITLE, b.title);
			entry.put(Browser.BookmarkColumns.URL, b.url);
			entry.put(Browser.BookmarkColumns.FAVICON, b.favicon);
			
			if (log.debugEnabled) {
				log.debug("put item", b.title, b.url, b.id );
				log.debug("--- FAVICON", b.favicon!=null?b.favicon.length:"-1");
			}
			list.add(entry);
		}
		return list.toArray(new ContentValues[]{});
	}
	
	public static void replaceFull(BookmarkTreeContext ctx, ArrayList<RawDataBean> rows) 
	throws Exception {
		
		ContentResolver contentResolver = ctx.activity.getContentResolver(); 
		
		// delete existing entries
		contentResolver.delete ( BOOKMARKS_URI
				, Browser.BookmarkColumns.BOOKMARK+"=1"
				, new String[]{}
		);
		
		// insert from XML 
		ContentValues[] values = transform(rows);
		contentResolver.bulkInsert(BOOKMARKS_URI, values);
		
	}
	
}

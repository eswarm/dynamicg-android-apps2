package com.dynamicg.bookmarkTree.data.writer;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.Browser;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;
import com.dynamicg.common.Logger;

public class BookmarkWriter extends BookmarkWriterA {

	private static final Logger log = new Logger(BookmarkWriter.class);
	
	private final ContentValues values = new ContentValues();

	public BookmarkWriter(BookmarkTreeContext ctx) {
		super(ctx);
	}

	private void doUpdate(Integer bookmarkId) {
		contentResolver.update ( Browser.BOOKMARKS_URI
				, values
				, Browser.BookmarkColumns._ID+"=?"
				, new String[]{Integer.toString(bookmarkId)}
		);
	}
	
	public void updateTitle(Integer bookmarkId, String title) {
		values.put(Browser.BookmarkColumns.TITLE, title);
		doUpdate(bookmarkId);
	}

	public void updateTitleAndUrl(Integer bookmarkId, String title, String url) {
		values.put(Browser.BookmarkColumns.TITLE, title);
		values.put(Browser.BookmarkColumns.URL, url);
		doUpdate(bookmarkId);
	}
	
	public void insert(String title, String url) {
		if (log.debugEnabled) {
			log.debug("create bookmark", title, url);
		}
		values.put(Browser.BookmarkColumns.TITLE, title);
		values.put(Browser.BookmarkColumns.URL, url);
		values.put(Browser.BookmarkColumns.BOOKMARK, 1);
		Uri result = contentResolver.insert ( Browser.BOOKMARKS_URI, values );
		if (log.debugEnabled) {
			log.debug("row created", result);
		}
	}
	
/* TODO handle exception
java.lang.IllegalArgumentException: Unknown URL content://browser/bookmarks
at android.content.ContentResolver.insert(ContentResolver.java:583)
at com.dynamicg.bookmarkTree.data.writer.BookmarkWriter.insert(BookmarkWriter.java:46)
at com.dynamicg.bookmarkTree.dialogs.EditBookmarkDialog.saveBookmark(EditBookmarkDialog.java:204)
at com.dynamicg.bookmarkTree.dialogs.EditBookmarkDialog.access$100(EditBookmarkDialog.java:29)
at com.dynamicg.bookmarkTree.dialogs.EditBookmarkDialog$1.onPositiveButton(EditBookmarkDialog.java:86)
at com.dynamicg.bookmarkTree.util.DialogButtonPanelWrapper$1.onClick(DialogButtonPanelWrapper.java:42)
 */
	public void deleteBrowserBookmark(Integer id) {
		if (log.debugEnabled) {
			log.debug("delete bookmark", id);
		}
		contentResolver.delete ( Browser.BOOKMARKS_URI
				, Browser.BookmarkColumns._ID+"=?"
				, new String[]{Integer.toString(id)}
				);
	}
	
}

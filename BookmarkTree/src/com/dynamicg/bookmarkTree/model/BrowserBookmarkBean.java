package com.dynamicg.bookmarkTree.model;

import java.util.Collection;

import android.graphics.Bitmap;

public class BrowserBookmarkBean extends Bookmark {

	public Integer id;
	public String fullTitle;
	public String url;
	public Bitmap favicon;
	
	public BrowserBookmarkBean() {
		super();
	}
	
	public static BrowserBookmarkBean createNew() {
		BrowserBookmarkBean bean = new BrowserBookmarkBean();
		bean.id = -1;
		bean.fullTitle = "";
		bean.url = "";
		return bean;
	}

	public String toString() {
		return fullTitle + " {B}"; // for debug
	}
	
	public Integer getId() {
		return id;
	}

	public String getFullTitle() {
		return fullTitle;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public Bitmap getFavicon() {
		return favicon;
	}

	@Override
	public boolean isExpanded() {
		return false;
	}

	@Override
	public void setExpanded(boolean expanded) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Bookmark> getChildren() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getBookmarkType() {
		return TYPE_BROWSER_BOOKMARK;
	}
	
}

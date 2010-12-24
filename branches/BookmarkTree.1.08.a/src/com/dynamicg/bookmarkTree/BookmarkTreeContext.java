package com.dynamicg.bookmarkTree;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;

import com.dynamicg.bookmarkTree.data.BookmarkManager;
import com.dynamicg.bookmarkTree.prefs.PreferencesWrapper;
import com.dynamicg.bookmarkTree.ui.BookmarkListAdapter;

public class BookmarkTreeContext {

	private static final String PREFS_NAME = "dynamicg.bookmarkTree";
	
	public static SharedPreferences settings;
	
	public final Activity activity;
	public final BookmarkManager bookmarkManager;
	public final BookmarkListAdapter bookmarkListAdapter;
	public final PreferencesWrapper preferencesWrapper;
	private LayoutInflater layoutInflater;
	
	public BookmarkTreeContext(Activity activity) {
		if (settings==null) {
			settings = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		}
		
    	this.activity = activity;
    	this.preferencesWrapper = new PreferencesWrapper();
    	this.bookmarkManager = new BookmarkManager(this);
    	this.bookmarkListAdapter = new BookmarkListAdapter(this);
	}
	
	public String getFolderSeparator() {
		return preferencesWrapper.prefsBean.getFolderSeparator();
	}
	
	public String getNodeConcatenation() {
		return preferencesWrapper.prefsBean.getNodeConcatenation();
	}
	
	public LayoutInflater getLayoutInflater() {
		if (layoutInflater==null) {
			layoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		return layoutInflater;
	}

	public void reloadAndRefresh() {
		bookmarkManager.reloadData();
		bookmarkListAdapter.redraw();
	}

}

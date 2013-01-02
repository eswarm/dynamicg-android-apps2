package com.dynamicg.homebuttonlauncher.preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.dialog.SizePrefsHelper;

public class PrefSettings {

	private static final String KEY_LAYOUT = "layout";
	private static final String KEY_LABEL_SIZE = "labelSize";
	private static final String KEY_ICON_SIZE = "iconSize";

	public static final int NUM_LAYOUTS = 2;
	private static final int LAYOUT_PLAIN_2 = 1;

	private final SharedPreferences sharedPrefs;

	public PrefSettings(SharedPreferences settingsPrefs) {
		this.sharedPrefs = settingsPrefs;
	}

	public void writeAppSettings(int layout, int labelSize, int iconSize) {
		Editor edit = sharedPrefs.edit();
		edit.putInt(KEY_LAYOUT, layout);
		edit.putInt(KEY_LABEL_SIZE, labelSize);
		edit.putInt(KEY_ICON_SIZE, iconSize);
		edit.commit();
	}

	// this is "sp": 12, 14, ..., 24
	public int getLabelSize() {
		return sharedPrefs.getInt(KEY_LABEL_SIZE, SizePrefsHelper.DEFAULT_LABEL_SIZE);
	}

	// this is "dp": 32, 48, 64
	public int getIconSize() {
		return sharedPrefs.getInt(KEY_ICON_SIZE, SizePrefsHelper.DEFAULT_ICON_SIZE);
	}

	public int getLayoutType() {
		return sharedPrefs.getInt(KEY_LAYOUT, 0);
	}

	public int getListLayoutId() {
		switch (getLayoutType()) {
		case LAYOUT_PLAIN_2: return R.layout.applist_gridview2;
		default: return R.layout.applist_listview;
		}
	}

	public int getMinWidthDimen() {
		int layoutId = getLayoutType();
		if (layoutId==LAYOUT_PLAIN_2) {
			return R.dimen.widthWide;
		}
		return R.dimen.widthDefault;
	}

	public int getAppEntryLayoutId() {
		return R.layout.app_entry_default;
	}


}

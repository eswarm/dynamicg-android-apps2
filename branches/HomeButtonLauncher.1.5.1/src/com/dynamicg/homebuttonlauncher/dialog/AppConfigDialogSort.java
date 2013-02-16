package com.dynamicg.homebuttonlauncher.dialog;

import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.MenuGlobals;
import com.dynamicg.homebuttonlauncher.preferences.PreferencesManager;

public class AppConfigDialogSort extends AppConfigDialog {

	public AppConfigDialogSort(MainActivityHome activity, PreferencesManager preferences) {
		super(activity, preferences, MenuGlobals.APPS_SORT);
	}


}

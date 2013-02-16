package com.dynamicg.homebuttonlauncher.dialog;

import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.MenuGlobals;
import com.dynamicg.homebuttonlauncher.preferences.PreferencesManager;

public class AppConfigDialogRemove extends AppConfigDialog {

	public AppConfigDialogRemove(MainActivityHome activity, PreferencesManager preferences) {
		super(activity, preferences, MenuGlobals.APPS_REMOVE);
	}

	@Override
	public void attachHeader() {
		hideCustomHeader();
	}

}

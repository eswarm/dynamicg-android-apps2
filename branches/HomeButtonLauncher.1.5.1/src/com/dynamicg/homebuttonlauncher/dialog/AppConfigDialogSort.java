package com.dynamicg.homebuttonlauncher.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.MenuGlobals;
import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.preferences.PreferencesManager;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper;
import com.dynamicg.homebuttonlauncher.tools.PopupMenuWrapper;
import com.dynamicg.homebuttonlauncher.tools.PopupMenuWrapper.PopupMenuItemListener;

public class AppConfigDialogSort extends AppConfigDialog {

	public AppConfigDialogSort(MainActivityHome activity, PreferencesManager preferences) {
		super(activity, preferences, MenuGlobals.APPS_SORT);
	}

	@Override
	public void attachHeader() {
		final View anchor = DialogHelper.prepareCustomHeader(this, R.string.menuSort);
		final PopupMenuItemListener listener = new PopupMenuItemListener() {
			@Override
			public void popupMenuItemSelected(int id) {
				if (id==MENU_RESET) {
					confirmSortReset();
				}
			}
		};
		final PopupMenuWrapper menuWrapper = new PopupMenuWrapper(context, anchor, listener);
		menuWrapper.attachToAnchorClick();
		menuWrapper.addItem(MENU_RESET, R.string.menuReset);
	}

	private void confirmSortReset() {
		AlertDialog.Builder b = new AlertDialog.Builder(context);
		String label = context.getString(R.string.menuReset)+"?";
		b.setTitle(label);
		b.setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				prefShortlist.resetSortList();
				afterSave();
			}
		} );
		b.setNegativeButton(R.string.buttonCancel, null);
		b.show();
	}

}

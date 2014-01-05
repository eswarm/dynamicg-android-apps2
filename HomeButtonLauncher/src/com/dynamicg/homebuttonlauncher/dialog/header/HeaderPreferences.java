package com.dynamicg.homebuttonlauncher.dialog.header;

import android.widget.PopupMenu;

import com.dynamicg.homebuttonlauncher.HBLConstants;
import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.dialog.PreferencesDialog;
import com.dynamicg.homebuttonlauncher.tools.PopupMenuWrapper;
import com.dynamicg.homebuttonlauncher.tools.PopupMenuWrapper.PopupMenuItemListener;
import com.dynamicg.homebuttonlauncher.tools.drive.GoogleDriveBackupRestoreHelper;

public class HeaderPreferences extends HeaderAbstract {

	private final MainActivityHome activity;
	private final PreferencesDialog dialog;

	public HeaderPreferences(PreferencesDialog dialog, MainActivityHome activity) {
		super(dialog);
		this.activity = activity;
		this.dialog = dialog;
	}

	@Override
	public void attach() {
		final PopupMenuItemListener listener = new PopupMenuItemListener() {
			@Override
			public void popupMenuItemSelected(PopupMenu popupMenu, int id) {
				new GoogleDriveBackupRestoreHelper(activity, dialog).dispatch(id);
			}
		};
		final PopupMenuWrapper menuWrapper = new PopupMenuWrapper(context, iconNode, listener);
		menuWrapper.attachToAnchorClick();
		menuWrapper.addItem(HBLConstants.MENU_DRIVE_BACKUP, R.string.prefsDriveBackup);
		menuWrapper.addItem(HBLConstants.MENU_DRIVE_RESTORE, R.string.prefsDriveRestore);
	}

}
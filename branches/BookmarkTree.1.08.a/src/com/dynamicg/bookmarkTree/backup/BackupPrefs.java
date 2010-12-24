package com.dynamicg.bookmarkTree.backup;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;
import com.dynamicg.common.SimpleAlertDialog;

public class BackupPrefs {

	public static final int DAYS_BETWEEN = 20;
	
	private static final String KEY_LAST_BACKUP = "backup.last";
	private static final String KEY_INITIAL_CONFIRMATION = "backup.initConfirm";
	private static final String KEY_AUTO_ENABLED = "backup.auto";
	
	private static final SharedPreferences settings = BookmarkTreeContext.settings;
	
	public static void onStartup(BookmarkTreeContext ctx) {
		if (!settings.contains(KEY_INITIAL_CONFIRMATION)) {
			writePref(KEY_INITIAL_CONFIRMATION, 1);
			BackupPrefs.initialBackupConfirmation(ctx);
		}
		else {
			checkPeriodicBackup(ctx);
		}
	}
	
	private static void initialBackupConfirmation(final BookmarkTreeContext ctx) {
		new SimpleAlertDialog.OkCancelDialog(ctx.activity, Messages.brEnableAutoBackup) {
			@Override
			public void onPositiveButton() {
				writePref(KEY_AUTO_ENABLED, 1);
				BackupManager.createBackup(ctx, null); // no callback - "createBackup" will register this backup on its own
			}
		};
	}
	
	private static int getDayNr() {
		long now = System.currentTimeMillis();
		long miliPerDay = 60l * 60l * 24l * 1000l;
		return (int) (now/miliPerDay);
	}
	
	private static void checkPeriodicBackup(BookmarkTreeContext ctx) {
		if (!isAutoBackupEnabled()) {
			return;
		}
		int daynr = getDayNr();
		int lastBackup = settings.getInt(KEY_LAST_BACKUP, 0);
		if ( daynr-lastBackup > DAYS_BETWEEN ) {
			BackupManager.createBackup(ctx, null); // no callback - "createBackup" will register this backup on its own
		}
	}
	
	private static void writePref(String key, int value) {
		Editor edit = settings.edit();
		edit.putInt(key, value);
		edit.commit();
	}

	public static void registerBackup() {
		writePref(KEY_LAST_BACKUP, getDayNr());
	}
	
	public static boolean isAutoBackupEnabled() {
		return settings.getInt(KEY_AUTO_ENABLED, 0) == 1;
	}

	public static void writeAutoBackupEnabled(boolean isChecked) {
		writePref(KEY_AUTO_ENABLED, isChecked?1:0);
	}

}

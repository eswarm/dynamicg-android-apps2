package com.dynamicg.bookmarkTree.backup;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;
import com.dynamicg.bookmarkTree.prefs.PreferencesWrapper;
import com.dynamicg.common.SimpleAlertDialog;

public class BackupPrefs {

	public static final int DAYS_BETWEEN = 20;
	
	private static final String KEY_LAST_BACKUP = "backup.last";
	private static final String KEY_INITIAL_CONFIRMATION = "backup.initConfirm";
	private static final String KEY_AUTO_ENABLED = "backup.auto";
	
	private final BookmarkTreeContext ctx;
	private final Activity context;
	private final SharedPreferences prefs;
	
	public BackupPrefs(BookmarkTreeContext ctx) {
		this.ctx = ctx;
		context = ctx.activity;
		prefs = PreferencesWrapper.getSharedPrefs(context);
	}
	
	public void initialBackupConfirmation() {
		if (prefs.contains(KEY_INITIAL_CONFIRMATION)) {
			return;
		}
		// set flag
		writePref(prefs, KEY_INITIAL_CONFIRMATION, 1);
		
		new SimpleAlertDialog.OkCancelDialog(context, Messages.brEnableAutoBackup) {
			@Override
			public void onPositiveButton() {
				writePref(prefs, KEY_AUTO_ENABLED, 1);
				BackupManager.createBackup(ctx, null); // no callback
			}
		};
	}
	
	private static int getDayNr() {
		long now = System.currentTimeMillis();
		long miliPerDay = 60l * 60l * 24l * 1000l;
		return (int) (now/miliPerDay);
	}
	
	public void checkPeriodicBackup() {
		int daynr = getDayNr();
		int lastBackup = prefs.getInt(KEY_LAST_BACKUP, 0);
		if ( daynr-lastBackup > DAYS_BETWEEN ) {
			BackupManager.createBackup(ctx, null); // no callback
		}
	}
	
	private static void writePref(SharedPreferences prefs, String key, int value) {
		Editor edit = prefs.edit();
		edit.putInt(key, value);
		edit.commit();
	}

	public void registerBackup() {
		writePref(prefs, KEY_LAST_BACKUP, getDayNr());
	}
	
}

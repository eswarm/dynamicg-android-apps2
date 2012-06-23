package com.dynamicg.bookmarkTree.backup;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;
import com.dynamicg.bookmarkTree.prefs.PreferencesUpdater;
import com.dynamicg.bookmarkTree.prefs.PreferencesWrapper;
import com.dynamicg.common.Logger;

public class BackupPrefs {

	private static final Logger log = new Logger(BackupPrefs.class);
	
	private static final String KEY_LAST_BACKUP = "backup.last";
	private static final String LEGACY_AUTO_ENABLED = "backup.auto";
	
    private static final String KEY_AUTO_BACKUP = "backup.interval";
	
	private static final SharedPreferences settings = BookmarkTreeContext.settings;
	
	public static void onStartup(BookmarkTreeContext ctx) {
		migrate120();
		int autoEnabled = settings.getInt(KEY_AUTO_BACKUP, -1);
		if (autoEnabled>0) {
			checkPeriodicBackup(ctx);
		}
	}
	
	private static void migrate120() {
		// migrate from 1.20
		if (BookmarkTreeContext.settings.contains(LEGACY_AUTO_ENABLED)) {
			int value = BookmarkTreeContext.settings.getInt(LEGACY_AUTO_ENABLED,0);
			if (value==1) {
				writeBackupInterval(20);
			}
			Editor editor = BookmarkTreeContext.settings.edit();
			editor.remove(LEGACY_AUTO_ENABLED);
			editor.commit();
		}
	}
	
	private static int getDayNr() {
		long now = System.currentTimeMillis();
		long miliPerDay = 60l * 60l * 24l * 1000l;
		return (int) (now/miliPerDay);
	}
	
	private static void checkPeriodicBackup(BookmarkTreeContext ctx) {
		int daynr = getDayNr();
		int lastBackup = settings.getInt(KEY_LAST_BACKUP, 0);
		int daysBetween = PreferencesWrapper.autoBackupSpinner.value;
		boolean required = daynr-lastBackup >= daysBetween;
		if (log.debugEnabled) {
			log.debug("checkPeriodicBackup", daynr, lastBackup, required);
		}
		if (required) {
			startBackup(ctx);
		}
	}
	
	public static void registerBackup() {
		writePref(KEY_LAST_BACKUP, getDayNr());
	}
	
	private static void startBackup(BookmarkTreeContext ctx) {
		BackupManager.createBackup(ctx, null); // no callback
	}
	
	private static void writePref(String key, int value) {
		PreferencesUpdater.writeIntPref(key, value);
	}

	public static int getBackupInterval() {
		final int autoBackupValue = BookmarkTreeContext.settings.getInt(KEY_AUTO_BACKUP, 10);
		return autoBackupValue;
	}
	
	public static void writeBackupInterval(int newValue) {
		writePref(KEY_AUTO_BACKUP, newValue);
	}

}

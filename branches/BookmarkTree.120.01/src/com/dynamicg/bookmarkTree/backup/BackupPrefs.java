package com.dynamicg.bookmarkTree.backup;

import android.content.SharedPreferences;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;
import com.dynamicg.bookmarkTree.prefs.PreferencesUpdater;
import com.dynamicg.bookmarkTree.prefs.PreferencesWrapper;
import com.dynamicg.common.Logger;

public class BackupPrefs {

	private static final Logger log = new Logger(BackupPrefs.class);
	
	private static final String KEY_LAST_BACKUP = "backup.last";
	private static final String KEY_AUTO_ENABLED = "backup.auto"; // TODO migrate
	
    private static final String KEY_AUTO_BACKUP = "backup.interval";
	
	private static final SharedPreferences settings = BookmarkTreeContext.settings;
	
	public static void onStartup(BookmarkTreeContext ctx) {
		int autoEnabled = settings.getInt(KEY_AUTO_ENABLED, -1);
		if (autoEnabled==1) {
			checkPeriodicBackup(ctx);
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
	
	public static void writeAutoBackup(int newValue) {
		writePref(KEY_AUTO_ENABLED, newValue);
	}

	public static boolean isAutoBackupEnabled() {
		if (log.debugEnabled) {
			log.debug("isAutoBackupEnabled", settings.getInt(KEY_AUTO_ENABLED, 0) == 1 );
		}
		return settings.getInt(KEY_AUTO_ENABLED, 0) == 1;
	}

}

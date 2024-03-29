package com.dynamicg.homebuttonlauncher.tools.drive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.dynamicg.common.Logger;
import com.dynamicg.homebuttonlauncher.GlobalContext;
import com.dynamicg.homebuttonlauncher.HBLConstants;
import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.OnClickListenerDialogWrapper;
import com.dynamicg.homebuttonlauncher.dialog.PreferencesDialog;
import com.dynamicg.homebuttonlauncher.preferences.SettingsBackupHelper;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper;
import com.dynamicg.homebuttonlauncher.tools.ShortcutHelper;

public abstract class HBLBackupRestore {

	public static final String GROUP_ICONS = "icons";

	private static final Logger log = new Logger(HBLBackupRestore.class);

	protected final MainActivityHome activity;
	protected final Context context;
	private final PreferencesDialog dialog;

	public HBLBackupRestore(MainActivityHome activity, PreferencesDialog dialog) {
		this.activity = activity;
		this.context = activity;
		this.dialog = dialog;
	}

	public abstract File getBackupFile() throws IOException;
	public abstract boolean isReady();
	public abstract void executeUpload(File file);
	public abstract void triggerImport(Dialog dialog);
	public abstract int getTitleResId(int action);

	public static void create(MainActivityHome activity, PreferencesDialog dialog, int what) {
		HBLBackupRestore helper = null;
		if (what==HBLConstants.MENU_DRIVE_BACKUP || what==HBLConstants.MENU_DRIVE_RESTORE) {
			helper = new HBLBackupRestoreGoogleDrive(activity, dialog);
		}
		else if (what==HBLConstants.MENU_SDCARD_BACKUP || what==HBLConstants.MENU_SDCARD_RESTORE) {
			helper = new HBLBackupRestoreSdCard(activity, dialog);
		}
		if (!helper.isReady()) {
			return;
		}
		helper.dispatch(what);
	}

	private void dispatch(int what) {
		if (what==HBLConstants.MENU_DRIVE_BACKUP || what==HBLConstants.MENU_SDCARD_BACKUP) {
			OnClickListenerDialogWrapper okListener = new OnClickListenerDialogWrapper(context) {
				@Override
				public void onClickImpl(DialogInterface d, int which) {
					startBackup();
				}
			};
			DialogHelper.confirm(context, getTitleResId(what), okListener);
		}
		else {
			OnClickListenerDialogWrapper okListener = new OnClickListenerDialogWrapper(context) {
				@Override
				public void onClickImpl(DialogInterface d, int which) {
					triggerImport(dialog);
				}
			};
			DialogHelper.confirm(context, getTitleResId(what), okListener);
		}
	}

	private void exportToFile(File file) throws Exception {
		final ArrayList<String> localIcons = new ArrayList<String>();
		final ArrayList<String> sharedPrefNames = SettingsBackupHelper.getSharedPrefNames(activity);
		final XmlWriter writer = new XmlWriter(file);
		for (String entryGroup:sharedPrefNames) {
			SharedPreferences sharedPreferences = context.getSharedPreferences(entryGroup, Context.MODE_PRIVATE);
			Map<String, ?> all = sharedPreferences.getAll();
			for (String entryKey:all.keySet()) {
				Object item = entryKey!=null ? all.get(entryKey) : null;
				if (item==null) {
					// see https://mail.google.com/mail/u/0/?shva=1#inbox/141d5f61e6546497
					continue;
				}
				final String entryType = item.getClass().getSimpleName();
				final String entryValue = item.toString();
				log.debug("backup value", entryGroup, entryKey, entryType, entryValue);
				Map<String, String> entry = new TreeMap<String, String>();
				entry.put(XmlGlobals.ENTRY_GROUP, entryGroup);
				entry.put(XmlGlobals.ENTRY_KEY, entryKey);
				entry.put(XmlGlobals.ENTRY_TYPE, entryType);
				entry.put(XmlGlobals.ENTRY_VALUE, entryValue);
				writer.add(entry);
				if (ShortcutHelper.isShortcutWithLocalIcon(entryGroup, entryKey)) {
					localIcons.add(ShortcutHelper.getShortcutId(entryKey));
				}
			}
		}
		if (localIcons.size()>0) {
			ShortcutHelper.initIconDir(context);
			for (String shortcutId:localIcons) {
				Map<String, String> entry = new TreeMap<String, String>();
				entry.put(XmlGlobals.ENTRY_GROUP, GROUP_ICONS);
				entry.put(XmlGlobals.ENTRY_KEY, shortcutId);
				entry.put(XmlGlobals.ENTRY_ICON_DATA, ShortcutHelper.encodeIcon(shortcutId));
				writer.add(entry);
			}
		}

		writer.close();
	}

	private void startBackup() {
		try {
			File file = getBackupFile();
			exportToFile(file);
			log.debug("BACKUP", file);
			executeUpload(file);
		}
		catch (Throwable t) {
			DialogHelper.showCrashReport(context, t);
		}
	}

	private static boolean isEmpty(String s) {
		return s==null || s.length()==0;
	}

	protected static void restoreSettings(Context context, File file, boolean deleteAfterRestore)
			throws Exception {
		final HashMap<String, Editor> editors = new HashMap<String, Editor>();
		XmlReader reader = new XmlReader(file);
		List<Map<String, String>> content = reader.getContent(context);
		if (content==null||content.size()==0) {
			return;
		}

		log.trace("restoreSettings", content);

		for (Map<String, String> map:content) {
			String entryGroup = map.get(XmlGlobals.ENTRY_GROUP);
			String entryKey = map.get(XmlGlobals.ENTRY_KEY);
			String entryType = map.get(XmlGlobals.ENTRY_TYPE);
			String entryValue = map.get(XmlGlobals.ENTRY_VALUE);

			if (isEmpty(entryGroup) || isEmpty(entryKey)) {
				continue;
			}

			if (!editors.containsKey(entryGroup)) {
				SharedPreferences prefs = context.getSharedPreferences(entryGroup, Context.MODE_PRIVATE);
				Editor edit = prefs.edit();
				edit.clear(); // initially reset all existing entries for each shared pref
				editors.put(entryGroup, edit);
			}

			log.trace("restore value", entryGroup, entryType, entryKey, entryValue);
			Editor edit = editors.get(entryGroup);
			if ("Integer".equals(entryType)) {
				edit.putInt(entryKey, Integer.valueOf(entryValue));
			}
			else if ("Boolean".equals(entryType)) {
				edit.putBoolean(entryKey, Boolean.valueOf(entryValue));
			}
			else if ("String".equals(entryType)) {
				edit.putString(entryKey, entryValue);
			}
		}

		// commit all
		for (Editor edit:editors.values()) {
			edit.apply();
		}

		if (deleteAfterRestore) {
			file.delete();
		}
	}

	public static void restoreImpl(MainActivityHome activity, Dialog dialog, File file, boolean deleteAfterRestore) {
		try {
			restoreSettings(activity, file, deleteAfterRestore);
			GlobalContext.resetCache(); // make sure we don't retain icons scaled to previous size
			dialog.dismiss();
			activity.recreate();
		}
		catch (Throwable t) {
			DialogHelper.showCrashReport(activity, t);
		}
	}

}

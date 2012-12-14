package com.dynamicg.bookmarkTree.backup;

import java.io.File;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class GoogleDriveUtil {
	
	public static final String PLUGIN_APP = "com.dynamicg.timerec.plugin3";
	private static final String PLUGIN_ACTIVITY = "com.dynamicg.timerec.plugin3.gdrive.FileProviderActivity";
	
	public static void upload(Context context, File file) {

		final int requestCode = GoogleDriveGlobals.ACTION_BACKUP; // TODO new action for "bookmarks" (?)
		
		if (file==null) {
			return;
		}
		Intent intent = getBaseIntent(requestCode);
		intent.putExtra(GoogleDriveGlobals.KEY_FNAME_ABS, file.getAbsolutePath());

		try {
			context.startActivity(intent);
		}
		catch (ActivityNotFoundException e) {
			alertMissingPlugin(context);
		}
		catch (SecurityException e) {
			showPermissionError(context, e);
		}
	}
	
	public static void startDownload(Activity context) {
		
		final int requestCode = GoogleDriveGlobals.ACTION_RESTORE; // TODO new action for "bookmarks" (?)
		
		Intent intent = getBaseIntent(requestCode);
		intent.putExtra(GoogleDriveGlobals.KEY_FNAME_DRIVE, BackupManager.GOOGLE_DRIVE_FILE_NAME);
		intent.putExtra(GoogleDriveGlobals.KEY_FNAME_LOCAL, BackupManager.GOOGLE_DRIVE_FILE_NAME);

		try {
			context.startActivityForResult(intent, requestCode);
		}
		catch (ActivityNotFoundException e) {
			alertMissingPlugin(context);
		}
		catch (SecurityException e) {
			showPermissionError(context, e);
		}
	}

	private static void showPermissionError(Context context, SecurityException e) {
		// TODO Auto-generated method stub
		System.err.println("TODO - showPermissionError");
		
	}

	private static void alertMissingPlugin(Context context) {
		// TODO Auto-generated method stub
		System.err.println("TODO - alertMissingPlugin");
	}

	private static Intent getBaseIntent(int requestCode) {
		ComponentName component = new ComponentName(PLUGIN_APP, PLUGIN_ACTIVITY);
		Intent intent = new Intent();
		intent.setComponent(component);
		intent.putExtra(GoogleDriveGlobals.KEY_APP_INSTANCE, "F"); // free
		intent.putExtra(GoogleDriveGlobals.KEY_REQUEST_CODE, requestCode);
		return intent;
	}

}

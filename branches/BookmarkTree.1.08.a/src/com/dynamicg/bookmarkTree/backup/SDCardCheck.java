package com.dynamicg.bookmarkTree.backup;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.os.Environment;

import com.dynamicg.bookmarkTree.R;
import com.dynamicg.common.Logger;
import com.dynamicg.common.SimpleAlertDialog;
import com.dynamicg.common.SystemUtil;

public class SDCardCheck {

	private static final String exportSubdir = "dynamicg/bookmarks";
	private static final String TOUCH_FILE = ".touch";
	
	private final File backupdir = getBackupDir();
	private final String sdCardState = Environment.getExternalStorageState();
	private final Context context;
	
	private Throwable touchFileException;
	
	public SDCardCheck(Context context) {
		this.context = context;
	}
	
	public static final File getBackupDir() {
		File extdir = Environment.getExternalStorageDirectory();
		File backupdir = new File(extdir, exportSubdir);
		if (!backupdir.exists()) {
			try {
				backupdir.mkdirs();
			}
			catch (Throwable e) {
				Logger.dumpIfDevelopment(e);
			}
		}
		return backupdir;
	}
	
	private static void alert(Context context,String title, final String body) {
		new SimpleAlertDialog(context, title, R.string.commonClose) {
			@Override
			public String getPlainBodyText() {
				return body;
			}
		};
	}
	
	public static boolean readyForRead() {
		return true;
	}
	
	private boolean touchFileOkay() {
		try {
			File f = new File(backupdir,TOUCH_FILE);
			FileOutputStream out = new FileOutputStream(f);
			out.write(46); // dot
			out.flush();
			out.close();
			return true;
		}
		catch (Throwable e) {
			touchFileException = e;
			return false;
		}
	}
	
	public File readyForWrite() {
		String errorTitle = "Cannot write to SD Card";
		if (touchFileOkay()) {
			return backupdir;
		}
		else if (backupdir.exists() && !backupdir.canWrite()) {
			alert(context, errorTitle, "Directory "+backupdir+" is read only");
		}
		else if (!Environment.MEDIA_MOUNTED.equals(sdCardState)) {
			alert(context, errorTitle, "SD Card is not mounted.\nCurrent state is '"+sdCardState+"'");
		}
		else if (touchFileException!=null) {
			alert(context, errorTitle, "Write error: "+SystemUtil.getExceptionText(touchFileException) );
		}
		return null; // not okay
	}
	
}

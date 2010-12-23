package com.dynamicg.bookmarkTree.backup;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.TreeMap;

import android.content.Context;
import android.os.Environment;
import android.text.format.Time;
import android.widget.Toast;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;
import com.dynamicg.bookmarkTree.util.SimpleProgressDialog;
import com.dynamicg.common.Logger;
import com.dynamicg.common.StringUtil;

//TODO -- validation after backup
public class BackupManager {

	private static final Logger log = new Logger(BackupManager.class);
	
	private static final String exportSubdir = "dynamicg/bookmarks";
	
	private static final String FILE_PATTERN = "backup-{stamp}.xml";
	private static final String FMT_STAMP = "%Y-%m-%d.%H-%M-%S";
	
	public static final File getBackupDir() {
		File extdir = Environment.getExternalStorageDirectory();
		File backupdir = new File(extdir, exportSubdir);
		return backupdir;
	}
	
	private static String getFilename(Time t) {
		return StringUtil.replaceFirst(FILE_PATTERN, "{stamp}", t.format(FMT_STAMP));
	}
	private static String getFilename() {
		Time t = new Time();
		t.setToNow();
		return getFilename(t);
	}
	
	public static ArrayList<File> getBackupFiles() {
		final String filePrefix = "backup-";
		final String fileSuffix = ".xml";
		File dir = getBackupDir();
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.startsWith(filePrefix) && filename.endsWith(fileSuffix);
			}
		});
		
		if (log.isDebugEnabled()) {
			log.debug("backup list", dir, files!=null?files.length:-1);
		}
		
		if (files==null) {
			return new ArrayList<File>();
		}
		
		// sort a-z
		TreeMap<String, File> sortmap = new TreeMap<String, File>();
		for (File f:files) {
			sortmap.put(f.getName(), f);
		}
		
		// revert
		ArrayList<File> sortdesc = new ArrayList<File>();
		for (File f:sortmap.values()) {
			sortdesc.add(0, f);
		}
		
		return sortdesc;
	}
	
	private static void toast(BookmarkTreeContext ctx, String text) {
		Toast.makeText(ctx.activity, text, Toast.LENGTH_LONG).show();
	}
	
	public static interface BackupEventListener {
		public void backupDone();
		public void restoreDone();
	}
	
	public synchronized static void createBackup(final BookmarkTreeContext ctx, final BackupEventListener backupDoneListener) {
		
		final Context context = ctx.activity;
		
		new SimpleProgressDialog(context, Messages.brProgressCreateBackup) {
			
			String filename;
			int numberOfRows;
			
			@Override
			public void backgroundWork() {
				File backupdir = getBackupDir();
				backupdir.mkdirs();
				
				this.filename = getFilename();
				File xmlfileTemp = new File ( backupdir, filename+".tmp" );
				File xmlfileFinal = new File ( backupdir, filename );
				
				ArrayList<RawDataBean> bookmarks = BookmarkDataProvider.readBrowserBookmarks(ctx);
				numberOfRows = bookmarks.size();
				try {
					new XmlWriter(xmlfileTemp, bookmarks);
					xmlfileTemp.renameTo(xmlfileFinal);
				}
				catch (Exception e) {
					if (e instanceof RuntimeException) {
						throw (RuntimeException)e;
					}
					throw new RuntimeException(e);
				}
			}
			
			@Override
			public void done() {
				String text = Messages.brHintBackupCreated
				.replace("{1}", filename)
				.replace("{2}", Integer.toString(numberOfRows))
				;
				toast(ctx, text);
				backupDoneListener.backupDone();
			}
			
			@Override
			public void handleError(Throwable e) {
				super.handleError(e);
			}
			
		};
		
	}
	
	public synchronized static void restore ( final BookmarkTreeContext ctx
			, final File xmlfile
			, final BackupEventListener backupDoneListener
			) 
	{
		
		new SimpleProgressDialog(ctx.activity, Messages.brProgressRestoreBookmarks) {
			
			int numberOfRows;
			
			@Override
			public void backgroundWork() {
				try {
					ArrayList<RawDataBean> rows = new XmlReader(xmlfile).read();
					numberOfRows = rows.size();
					BookmarkDataProvider.replaceFull(ctx, rows);
				}
				catch (Exception e) {
					if (e instanceof RuntimeException) {
						throw (RuntimeException)e;
					}
					throw new RuntimeException(e);
				}
			}
			
			@Override
			public void done() {
				String text = Messages.brHintBookmarksRestored.replace("{1}", Integer.toString(numberOfRows));
				toast(ctx, text);
				backupDoneListener.restoreDone();
			}
			
			@Override
			public void handleError(Throwable e) {
				super.handleError(e);
			}
			
		};
		
	}

	private static void deleteImpl(ArrayList<File> backupFiles) {
		for (File f:backupFiles) {
			f.delete();
		}
	}
	public static void deleteOldFiles() {
		ArrayList<File> backupFiles = getBackupFiles();
		for (File f:backupFiles) {
			f.delete();
		}
	}

	public static void deleteFiles(int what) {
		if (what==BackupRestoreDialog.ACTION_DELETE_ALL) {
			deleteImpl(getBackupFiles());
		}
		else if (what==BackupRestoreDialog.ACTION_DELETE_OLD) {
			ArrayList<File> backupFiles = getBackupFiles();
			ArrayList<File> deletions = new ArrayList<File>();
			
			Time t = new Time();
			t.setToNow();
			t.monthDay = t.monthDay - BackupRestoreDialog.DELETION_DAYS_LIMIT;
			t.normalize(false);
			
			final String fnameStampLimit = getFilename(t);
			int comp;
			for (File f:backupFiles) {
				comp = f.getName().compareTo(fnameStampLimit);
				if (log.isDebugEnabled()) {
					log.debug("check old files", fnameStampLimit, f.getName(), comp, comp<=0?"***":"-");
				}
				if (comp<=0) {
					deletions.add(f);
				}
			}
			deleteImpl(deletions);
		}
	}

}

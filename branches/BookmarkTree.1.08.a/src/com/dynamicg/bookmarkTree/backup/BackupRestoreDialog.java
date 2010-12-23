package com.dynamicg.bookmarkTree.backup;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;
import com.dynamicg.bookmarkTree.R;
import com.dynamicg.bookmarkTree.backup.BackupManager.BackupEventListener;
import com.dynamicg.bookmarkTree.util.DialogButtonPanelWrapper;
import com.dynamicg.bookmarkTree.util.DialogHelper;
import com.dynamicg.common.SimpleAlertDialog;
import com.dynamicg.common.StringUtil;

public class BackupRestoreDialog extends Dialog
implements BackupEventListener {

	private final BookmarkTreeContext ctx;
	private final Activity context;

	public BackupRestoreDialog(BookmarkTreeContext ctx) {
		super(ctx.activity);
		this.ctx = ctx;
		this.context = ctx.activity;
		
		DialogHelper.expandContent(this, R.layout.backup_restore_body);
		
		this.show();
		
		// TODO check sd card availability
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setTitle(Messages.brDialogTitle);
		
		Button backup = (Button)findViewById(R.id.brBackup);
		backup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createBackup();
			}
		});
		
		setupRestoreList();
		
		new DialogButtonPanelWrapper(this, DialogButtonPanelWrapper.TYPE_CLOSE) {
			@Override
			public void onPositiveButton() {
				dismiss();
			}
		};
		
		((TextView)findViewById(R.id.brStorageHint1)).setText(Messages.brStorageHint);
		((TextView)findViewById(R.id.brStorageHint2)).setText(BackupManager.getBackupDir().toString());
		
	}
	
	private void setupRestoreList() {
		
		final RadioGroup backupListGroup = (RadioGroup)findViewById(R.id.brRestoreList);
		backupListGroup.removeAllViews(); // for repeated calls
		
		final ArrayList<File> backupFiles = BackupManager.getBackupFiles();
		if (backupFiles.size()==0) {
			TextView hint = SimpleAlertDialog.createTextView(context, Messages.brNoFilesForRestore);
			backupListGroup.addView(hint);
		}
		
		RadioButton rb;
		String filename;
		for ( int pos=0 ; pos<backupFiles.size() ; pos++ ) {
			rb = new RadioButton(context);
			filename = backupFiles.get(pos).getName().replace(".xml", ""); // skip extension on display items
			rb.setText(filename);
			rb.setId(pos);
			backupListGroup.addView(rb);
		}
		
		backupListGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId>=0) {
					RadioButton rb = (RadioButton)group.getChildAt(checkedId);
					// "clear()" fires the "changed" event for the current selection
					if (rb.isChecked()) {
						File f = backupFiles.get(checkedId);
						restore(group, f);
					}
				}
			}
		});
	}
	
	private void restore(final RadioGroup group, final File backupFile) {
		new SimpleAlertDialog(context, Messages.brRestoreConfirmation, R.string.commonOK, R.string.commonCancel) {
			
			@Override
			public void onPositiveButton() {
				BackupManager.restore(ctx, backupFile, BackupRestoreDialog.this);
			}

			@Override
			public void onNegativeButton() {
				group.clearCheck();
			}

			@Override
			public String getPlainBodyText() {
				return StringUtil.replaceFirst(Messages.brSelectedFileLabel, "{1}", backupFile.getName() );
			}

		};
	}

	private void createBackup() {
		BackupManager.createBackup(ctx, this);
	}
	
	@Override
	public void backupDone() {
		setupRestoreList();
	}

	@Override
	public void restoreDone() {
		// restore ok - refresh, close backup dialog
		ctx.reloadAndRefresh();
		dismiss();
	}
	
}

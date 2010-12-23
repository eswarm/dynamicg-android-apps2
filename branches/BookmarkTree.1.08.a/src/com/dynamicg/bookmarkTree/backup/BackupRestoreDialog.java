package com.dynamicg.bookmarkTree.backup;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;
import com.dynamicg.bookmarkTree.R;
import com.dynamicg.bookmarkTree.backup.BackupManager.BackupEventListener;
import com.dynamicg.bookmarkTree.util.DialogButtonPanelWrapper;
import com.dynamicg.bookmarkTree.util.DialogHelper;
import com.dynamicg.common.ui.SimpleAlertDialog;

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
		
		setTitle("Backup and restore");
		
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
		
	}
	
	private void setupRestoreList() {
		
		final ArrayList<File> backupFiles = BackupManager.getBackupFiles();
		
		RadioGroup backupListGroup = (RadioGroup)findViewById(R.id.brRestoreList);
		backupListGroup.removeAllViews(); // for repeated calls
		
		RadioButton rb;
		for ( int pos=0 ; pos<backupFiles.size() ; pos++ ) {
			rb = new RadioButton(context);
			rb.setText(backupFiles.get(pos).getName());
			rb.setId(pos);
			backupListGroup.addView(rb);
		}
		
		// TODO - hint if no files found
		// TODO - add "files stored in" hint
		
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
		final String title = "Restore bookmarks?";
		final String includeIconsLabel = "Include icons";
		new SimpleAlertDialog(context, title, R.string.commonOK, R.string.commonCancel) {
			
			CheckBox includeIcons;
			
			@Override
			public void onPositiveButton() {
				BackupManager.restore(ctx, backupFile, includeIcons.isChecked(), BackupRestoreDialog.this);
			}

			@Override
			public void onNegativeButton() {
				group.clearCheck();
			}



			@Override
			public View getBody() {
				
				TextView selectedFile = new TextView(context);
				selectedFile.setText("Selected file:\n"+backupFile.getName());
				
				CheckBox box = new CheckBox(context);
				box.setText(includeIconsLabel);
				box.setChecked(true); // default on
				includeIcons = box;

				LinearLayout layout = new LinearLayout(context);
				layout.setOrientation(LinearLayout.VERTICAL);
				layout.addView(selectedFile);
				layout.addView(box);
				layout.setPadding(5,5,5,5); // TODO -- scaling
				
				return layout;
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

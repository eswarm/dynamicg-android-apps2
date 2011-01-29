package com.dynamicg.bookmarkTree.prefs;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;
import com.dynamicg.bookmarkTree.R;
import com.dynamicg.bookmarkTree.data.BrowserBookmarkLoader;
import com.dynamicg.bookmarkTree.data.writehandler.SeparatorChangedHandler;
import com.dynamicg.bookmarkTree.data.writer.AlphaSortWriter;
import com.dynamicg.bookmarkTree.dialogs.AboutDialog;
import com.dynamicg.bookmarkTree.model.RawDataBean;
import com.dynamicg.bookmarkTree.prefs.SpinnerUtil.KeyValue;
import com.dynamicg.bookmarkTree.util.DialogButtonPanelWrapper;
import com.dynamicg.bookmarkTree.util.DialogHelper;
import com.dynamicg.bookmarkTree.util.SimpleProgressDialog;
import com.dynamicg.common.SimpleAlertDialog;
import com.dynamicg.common.SystemUtil;

public class PreferencesDialog extends Dialog {

    public static final int ACTION_DUMP_BOOKMARKS = 1;
    public static final int ACTION_SHOW_DISCLAIMER = 2;
    
	private final BookmarkTreeContext ctx;
	private final String currentSeparator;
	private final SpinnerUtil spinnerUtil;

	private EditText separatorItem;
	private CheckBox doFullUpdateCheckbox;
	private CheckBox optimiseLayout;
	private CheckBox scaleIconsCheckbox;

	private boolean dataRefreshRequired;
	
	private HashMap<PrefEntryInt, Integer> prefToViewMap = new HashMap<PrefEntryInt, Integer>(); 

	public PreferencesDialog(BookmarkTreeContext ctx) {
		super(ctx.activity);
		this.ctx = ctx;
		
		PrefEntryInt.resetUpdatedValue();
		
		this.spinnerUtil = new SpinnerUtil(this);
		
		DialogHelper.expandContent(this, R.layout.prefs_body);

		currentSeparator = ctx.getFolderSeparator();
		this.show();
	}

	private String getText(int res) {
		return getContext().getString(res);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setTitle(R.string.commonPreferences);

		separatorItem = (EditText)findViewById(R.id.prefsSeparator);
		separatorItem.setText(currentSeparator);
		separatorItem.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				checkForChangedSeparator();
			}
		});

		doFullUpdateCheckbox = (CheckBox)findViewById(R.id.prefsFullUpdateOnChange);
		checkForChangedSeparator(); // inactivate intially
		
		Button sortAlpha = (Button)findViewById(R.id.prefsActionSortAlpha);
		sortAlpha.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new SimpleAlertDialog.OkCancelDialog(ctx.activity, R.string.actionSortBookmarksConfirm) {
					@Override
					public void onPositiveButton() {
						new SimpleProgressDialog(ctx.activity, getText(R.string.commonPleaseWait) ) {
							@Override
							public void backgroundWork() {
								new AlphaSortWriter(ctx);
							}
							@Override
							public void done() {
								ctx.reloadAndRefresh(); // needs to be done by main thread
								SystemUtil.toastShort(getContext(), getText(R.string.actionSortBookmarksDone));
							}
						};
					}
				};
			}
		});
		
		// bind checkboxes
		optimiseLayout = bindCheckbox(R.id.prefsOptimiseLayout, PreferencesWrapper.optimisedLayout);
		scaleIconsCheckbox = bindCheckbox(R.id.prefsScaleIcons, PreferencesWrapper.scaleIcons);
		bindCheckbox(R.id.prefsKeepState, PreferencesWrapper.keepState);
		bindCheckbox(R.id.prefsSortCaseInsensitive, PreferencesWrapper.sortCaseInsensitive);
		
		// bind spinners
		bindSpinner ( R.id.prefsListStyle, PreferencesWrapper.listStyle, SpinnerUtil.getListStyleItems(getContext()), R.string.prefsListStyle );
		bindSpinner ( R.id.prefsSortOption, PreferencesWrapper.sortOption, SpinnerUtil.getSortOptionItems(getContext()), R.string.prefsSortLabel );
		
		// save/cancel panel
		new DialogButtonPanelWrapper(this, DialogButtonPanelWrapper.TYPE_SAVE_CANCEL) {
			@Override
			public void onPositiveButton() {
				saveClicked();
			}
		};

	}
	
	private void bindSpinner(int spinnerResId, PrefEntryInt prefEntry, ArrayList<KeyValue> items, int prompt) {
		prefToViewMap.put(prefEntry, spinnerResId);
		spinnerUtil.bind(spinnerResId, prefEntry, items, prompt);
	}
	
	private CheckBox bindCheckbox(int id, PrefEntryInt prefEntry) {
		prefToViewMap.put(prefEntry, id);
		CheckBox box = (CheckBox)findViewById(id);
		box.setChecked(prefEntry.isOn());
		return box;
	}
	
	private void checkForChangedSeparator() {
		String newValue = separatorItem.getText().toString();
		boolean hasChanges = newValue!=null && !newValue.equals(currentSeparator);
		doFullUpdateCheckbox.setEnabled(hasChanges);
		doFullUpdateCheckbox.setTextColor(hasChanges?Color.WHITE:Color.LTGRAY);
	}
	
	private void saveClicked() {
		
		boolean massUpdateTitles = this.doFullUpdateCheckbox.isChecked();
		if (!massUpdateTitles) {
			saveMain();
			savePostprocessing();
		}
		else {
			new SimpleProgressDialog(ctx.activity, getText(R.string.commonPleaseWait)) {
				@Override
				public void backgroundWork() {
					saveMain();
				}
				@Override
				public void done() {
					savePostprocessing();
				}
			};
		}
			
	}
	
	private void saveMain() {
		
		boolean toastForReopen =
			PreferencesWrapper.listStyle.value != spinnerUtil.getCurrentValue(R.id.prefsListStyle)
			|| PreferencesWrapper.optimisedLayout.isOn() != optimiseLayout.isChecked()
			|| PreferencesWrapper.scaleIcons.isOn() != scaleIconsCheckbox.isChecked()
			;
		
		processSeparatorUpdate();
		
		for (PrefEntryInt entry:prefToViewMap.keySet()) {
			push(entry, prefToViewMap.get(entry));
		}
		
		// see if "refresh" is required
		if ( spinnerUtil.isChanged(R.id.prefsListStyle)
				|| spinnerUtil.isChanged(R.id.prefsSortOption)
				) 
		{
			dataRefreshRequired = true;
		}
		
		PreferencesUpdater.write();
		
		if (toastForReopen) {
			SystemUtil.toastShort(getContext(), "Please restart the app");
		}
	}

	private void push(PrefEntryInt item, int id) {
		View view = findViewById(id);
		if (view instanceof Spinner) {
			item.setNewValue(spinnerUtil.getCurrentValue(id));
		}
		else if (view instanceof CheckBox) {
			item.setNewValue ( ((CheckBox)view).isChecked() ? 1 : 0 );
		}
	}
	
	private void savePostprocessing() {
		if (dataRefreshRequired) {
			ctx.reloadAndRefresh(); // needs to be done by main thread
		}
		dismiss(); // close dialog
	}
	
	private void processSeparatorUpdate() {

		String newSeparator = separatorItem.getText().toString();
		if (newSeparator==null || newSeparator.trim().length()==0 || newSeparator.equals(currentSeparator) ) {
			// no changes, cancel
			return;
		}

		PreferencesUpdater.setNewSeparator(newSeparator);

		if (doFullUpdateCheckbox.isChecked()) {
			new SeparatorChangedHandler(ctx, currentSeparator, newSeparator);
		}

		dataRefreshRequired = true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ACTION_SHOW_DISCLAIMER, 0, "About ...");
		menu.add(0, ACTION_DUMP_BOOKMARKS, 0, "Internal bookmarks ...");
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int id = item.getItemId();
		if (id==ACTION_DUMP_BOOKMARKS) {
			dumpBrowserBookmarks();
		}
		else if (id==ACTION_SHOW_DISCLAIMER) {
			AboutDialog.show(ctx);
		}
		return true;
	}

	private void dumpBrowserBookmarks() {
		ctx.reloadAndRefresh(); // so that the gui is really in sync with what we display here
		final ArrayList<RawDataBean> rows = BrowserBookmarkLoader.forInternalOps(ctx);
		
		final StringBuffer sb = new StringBuffer();
		for (RawDataBean row:rows) {
			sb.append(row.fullTitle + "\n" );
		}
		
		new SimpleAlertDialog(ctx.activity, "Browser Bookmarks", "Close") {
			public View getBody() {
				TextView text = new TextView(ctx.activity);
				text.setText(sb.toString());

				HorizontalScrollView xscroll = new HorizontalScrollView(ctx.activity);
				xscroll.addView(text);
				
				ScrollView yscroll = new ScrollView(ctx.activity);
				yscroll.addView(xscroll);
				yscroll.setPadding(10, 0, 10, 0);
				
				return yscroll;
			}
		};
	}
	
}
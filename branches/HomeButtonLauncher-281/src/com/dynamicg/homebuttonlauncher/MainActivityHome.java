package com.dynamicg.homebuttonlauncher;

import java.util.Arrays;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TabHost;

import com.dynamicg.common.Logger;
import com.dynamicg.common.SystemUtil;
import com.dynamicg.homebuttonlauncher.adapter.AppListAdapterMain;
import com.dynamicg.homebuttonlauncher.adapter.AppListAdapterMainStatic;
import com.dynamicg.homebuttonlauncher.dialog.AboutDialog;
import com.dynamicg.homebuttonlauncher.dialog.AppConfigDialog;
import com.dynamicg.homebuttonlauncher.dialog.PreferencesDialog;
import com.dynamicg.homebuttonlauncher.preferences.PreferencesManager;
import com.dynamicg.homebuttonlauncher.tab.TabHelperMain;
import com.dynamicg.homebuttonlauncher.tools.AppHelper;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper;
import com.dynamicg.homebuttonlauncher.tools.PopupMenuWrapper;
import com.dynamicg.homebuttonlauncher.tools.PopupMenuWrapper.PopupMenuItemListener;
import com.dynamicg.homebuttonlauncher.tools.ShortcutHelper;
import com.dynamicg.homebuttonlauncher.tools.StatusLineHelper;
import com.dynamicg.homebuttonlauncher.tools.SwipeHelper;
import com.dynamicg.homebuttonlauncher.tools.WidgetHelper;
import com.dynamicg.homebuttonlauncher.tools.drive.GoogleDriveBackupRestoreHelper;
import com.dynamicg.homebuttonlauncher.tools.drive.GoogleDriveGlobals;

/*
 * Copyright 2012,2013 DynamicG (dynamicg.android@gmail.com)
 * Distributed under the terms of the GNU General Public License
 * http://www.gnu.org/licenses/gpl-3.0.txt
 */

// see https://plus.google.com/104570711580136846518/posts/QpqfXXigAWW
//note we cannot filter the app on "needs soft home button" (i.e. a non-physical home button like galaxy nexus)
//http://developer.android.com/guide/topics/manifest/uses-feature-element.html
public class MainActivityHome extends Activity {

	private static final Logger log = new Logger(MainActivityHome.class);

	private static final int MAX_STATIC_THRESHOLD = 64;

	private Context context;
	private PreferencesManager preferences;
	private TabHost tabhost;

	private AppWidgetManager appWidgetManager;
	private AppWidgetHost appWidgetHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		context = this;

		try {
			main();
		}
		catch (Throwable t) {
			DialogHelper.showCrashReport(context, t);
		}
	}

	public void setBackgroundTransparency(boolean setTheme) {
		if (setTheme) {
			setTheme(R.style.ThemeSemiTransparent);
		}
		int alpha = preferences.prefSettings.getTransparencyAlpha();
		Drawable drawable = new ColorDrawable(Color.argb(alpha, 0x00, 0x00, 0x00));
		getWindow().setBackgroundDrawable(drawable);
	}

	private void main() {

		if (forwardToGoogleNow()) {
			return;
		}

		preferences = new PreferencesManager(this);

		if (preferences.prefSettings.isSemiTransparent()) {
			setBackgroundTransparency(true);
		}

		setContentView(R.layout.activity_main);

		if (isAutoStartSingleSuccessful()) {
			return;
		}
		attachContextMenu();
		if (preferences.prefSettings.getNumTabs()>0) {
			tabhost = new TabHelperMain(this, preferences).bindTabs();
		}
		setListAdapter();
		setMinWidth();

		if (preferences.prefSettings.isShowStatusLine()) {
			StatusLineHelper.addStatus(this);
		}

	}

	private boolean forwardToGoogleNow() {
		Bundle bundle = getIntent().getExtras();
		if (bundle!=null
				&& bundle.containsKey(HBLConstants.GOOGLE_NOW_EXTRA)
				&& bundle.getInt(HBLConstants.GOOGLE_NOW_EXTRA,0)==1
				)
		{
			try {
				Intent googlenow = AppHelper.getStartIntent(PreferencesManager.DFLT_GOOGLE_SEARCH[0]);
				googlenow.putExtras(bundle); // copy extras passed by the widget
				AppHelper.flagAsNewTask(googlenow);
				startActivity(googlenow);
				finish();
				return true;
			}
			catch (Throwable t) {
				DialogHelper.showCrashReport(context, t);
			}
		}
		return false;
	}

	private boolean isAutoStartSingleSuccessful() {
		if (!preferences.prefSettings.isAutoStartSingle()) {
			log.debug("autoStart", "disabled");
			return false;
		}

		if (getIntent().getBooleanExtra(MainActivityOpen.KEY, false) == true) {
			// called through "OpenActivity" (i.e. app drawer or homescreen icon), not through swipe
			// -> skip, otherwise we will lock ourselves out
			log.debug("autoStart", "from OpenActivity");
			return false;
		}

		if (preferences.prefShortlist.getComponentsMap().size()!=1) {
			// shortcut - this is faster than "getSelectedAppsList" just below
			log.debug("autoStart", "getComponentsMap", "size!=1");
			return false;
		}

		final AppListContainer appList = AppHelper.getSelectedAppsList(preferences.prefShortlist, true);
		if (appList.size()!=1) {
			log.debug("autoStart", "getSelectedAppsList", "size!=1");
			return false;
		}

		AppEntry entry = appList.get(0);
		log.debug("autoStart", entry.getComponent());
		boolean started = startAppAndClose(entry);
		return started;
	}

	public void refreshList() {
		try {
			setListAdapter();
			setMinWidth();
		}
		catch (Throwable t) {
			DialogHelper.showCrashReport(context, t);
		}
	}

	private void setMinWidth() {
		int minWidth = DialogHelper.getDimension(preferences.prefSettings.getMinWidthDimension());
		findViewById(R.id.headerContainer).setMinimumWidth(minWidth);
	}

	private AbsListView getListView() {
		final int[] layout = preferences.prefSettings.getMainLayout();
		int layoutResId = layout[0];
		int numGridColumns = layout[1];
		final View listview = findViewById(R.id.mainListView);

		if (listview instanceof ViewStub) {
			// first call
			ViewStub stub = (ViewStub)listview;
			stub.setLayoutResource(layoutResId);
			AbsListView absListView = (AbsListView)stub.inflate();
			if (numGridColumns>0) {
				((GridView)absListView).setNumColumns(numGridColumns);
			}
			return absListView;
		}

		// replace existing list on refresh
		ViewGroup parent = (ViewGroup)listview.getParent();
		AbsListView replacementListView = (AbsListView)getLayoutInflater().inflate(layoutResId, null);
		if (numGridColumns>0) {
			((GridView)replacementListView).setNumColumns(numGridColumns);
		}
		parent.addView(replacementListView, parent.indexOfChild(listview));
		parent.removeView(listview);
		return replacementListView;
	}

	private void setListAdapter() {
		final AbsListView listview = getListView();
		listview.setId(R.id.mainListView);

		final AppListContainer appList = AppHelper.getSelectedAppsList(preferences.prefShortlist, true);

		final BaseAdapter adapter;
		if (appList.size()<=MAX_STATIC_THRESHOLD) {
			// use "keep textviews" adapter when less then [max] rows - since we have lightweight views should make startup faster.
			// also, with a typical setup most selected apps will be visible all times anyway
			adapter = new AppListAdapterMainStatic(this, appList);
		}
		else {
			adapter = new AppListAdapterMain(this, appList);
		}

		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnAdapterItemClickListenerWrapper() {
			@Override
			public void onItemClickImpl(AdapterView<?> parent, View view, int position, long id) {
				startAppAndClose(appList.get(position));
			}
		});
		new AppListContextMenu(this, preferences.prefShortlist).attach(listview, appList);

		if (tabhost!=null) {
			SwipeHelper.attach(this, preferences, tabhost, listview);
		}
	}

	private boolean startAppAndClose(AppEntry entry) {
		Intent intent=null;
		try {
			if (entry.isWidget()) {
				// no action when list cell background is clicked
				return false;
			}
			else if (entry.isShortcut()) {
				intent = ShortcutHelper.getIntent(entry);
			}
			else {
				intent = AppHelper.getStartIntent(entry.getComponent());
			}

			if (intent.getComponent()!=null && HBLConstants.SELF.equals(intent.getComponent().flattenToString())) {
				// for those that want to disable the swipe (i.e. set "auto start mode" and only select HBL)
				log.debug("close self", HBLConstants.SELF);
				finish();
				return true;
			}

			AppHelper.flagAsNewTask(intent);
			startActivity(intent);
			finish();
			return true;
		}
		catch (Throwable t) {
			SystemUtil.dumpError(t);

			String appinfo = entry.getComponent();
			if (entry.isShortcut() && intent!=null && intent.getComponent()!=null) {
				// "entry.getComponent()" for shortcuts is "sc-<nn>|#<label>" so we show the shortcut intent instead
				appinfo = intent.getComponent().flattenToString();
			}

			String title = "Error - cannot open";
			String details = "\u2022 Exception: "+t.getClass().getSimpleName()
					+"\n\u2022 Component:\n{"+appinfo+"}";
			if (t instanceof SecurityException) {
				details += "\n\n\u2022 Details: "+t.getMessage();
			}
			DialogHelper.showError(context, title, details);
			return false;
		}
	}

	private void attachContextMenu() {
		final View anchor = findViewById(R.id.headerIcon);
		anchor.setOnClickListener(new OnClickListenerWrapper() {
			private PopupMenuWrapper wrapper;
			@Override
			public void onClickImpl(View v) {
				// create the popup only if actually required
				if (wrapper==null) {
					wrapper = bindMenu(anchor);
				}
				wrapper.showMenu();
			}
		});
	}

	private PopupMenuWrapper bindMenu(final View anchor) {
		final PopupMenuItemListener listener = new PopupMenuItemListener() {
			@Override
			public void popupMenuItemSelected(int id) {
				final MainActivityHome activity = MainActivityHome.this;
				switch (id) {
				case HBLConstants.MENU_APPS_ADD:
					AppConfigDialog.showAddDialog(activity, preferences);
					break;
				case HBLConstants.MENU_APPS_REMOVE:
					new AppConfigDialog(activity, preferences, HBLConstants.MENU_APPS_REMOVE).show();
					break;
				case HBLConstants.MENU_APPS_SORT:
					new AppConfigDialog(activity, preferences, HBLConstants.MENU_APPS_SORT).show();
					break;
				case HBLConstants.MENU_ABOUT:
					new AboutDialog(activity).show();
					break;
				case HBLConstants.MENU_PREFERENCES:
					new PreferencesDialog(activity, preferences).show();
					break;
				}
			}
		};

		final PopupMenuWrapper menuWrapper = new PopupMenuWrapper(context, anchor, listener);
		menuWrapper.addItem(HBLConstants.MENU_APPS_ADD, R.string.menuAdd, android.R.drawable.ic_menu_add);
		menuWrapper.addItem(HBLConstants.MENU_APPS_REMOVE, R.string.menuRemove, android.R.drawable.ic_menu_close_clear_cancel);
		menuWrapper.addItem(HBLConstants.MENU_APPS_SORT, R.string.menuSort, android.R.drawable.ic_menu_sort_by_size);
		menuWrapper.addItem(HBLConstants.MENU_ABOUT, R.string.menuAbout, android.R.drawable.ic_menu_info_details);
		menuWrapper.addItem(HBLConstants.MENU_PREFERENCES, R.string.preferences, android.R.drawable.ic_menu_preferences);
		return menuWrapper;
	}

	public void updateOnTabSwitch(int tabindex) {
		log.debug("updateOnTabSwitch", tabindex);
		preferences.updateCurrentTabIndex(tabindex);
		refreshList();
	}

	public void redrawTabContainer() {
		log.debug("redrawTabContainer");
		tabhost = new TabHelperMain(this, preferences).redraw();
	}

	public void forceTabSwitch(int tabindex) {
		updateOnTabSwitch(tabindex);
		redrawTabContainer();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode==GoogleDriveGlobals.ACTION_CUSTOM_GET) {
			GoogleDriveBackupRestoreHelper.restoreFromFile(data);
		}
		else if (requestCode==HBLConstants.SHORTCUT_RC) {
			ShortcutHelper.shortcutSelected(this, ShortcutHelper.getDialogRef(), data);
		}
		else if (requestCode==R.id.REQUEST_PICK_APPWIDGET || requestCode==R.id.REQUEST_CREATE_APPWIDGET) {
			new WidgetHelper(this, ShortcutHelper.getDialogRef()).processResult(requestCode, resultCode, data);
		}
	}

	public void saveShortcutComponent(String component) {
		preferences.prefShortlist.add(Arrays.asList(component));
	}

	public AppWidgetManager getAppWidgetManager() {
		if (appWidgetManager==null) {
			appWidgetManager = AppWidgetManager.getInstance(this);
		}
		return appWidgetManager;
	}

	public AppWidgetHost getAppWidgetHost() {
		if (appWidgetHost==null) {
			appWidgetHost = new AppWidgetHost(this, R.id.APPWIDGET_HOST_ID);
			appWidgetHost.startListening();
		}
		return appWidgetHost;
	}

	//	@Override
	//	public void onStart() {
	//		super.onStart();
	//		getAppWidgetHost();
	//		appWidgetHost.startListening();
	//	}

	@Override
	public void onStop() {
		super.onStop();
		if (appWidgetHost!=null) {
			appWidgetHost.stopListening();
		}
	}

}
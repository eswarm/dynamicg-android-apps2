package com.dynamicg.homebuttonlauncher.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.dynamicg.common.Logger;
import com.dynamicg.homebuttonlauncher.AppEntry;
import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.MenuGlobals;
import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.preferences.PreferencesManager;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper;

public class AppConfigDialogAdd extends AppConfigDialog {

	private static final Logger log = new Logger(AppConfigDialogAdd.class);

	private final List<AppEntry> baseAppList;
	private final String[] baseSearchLabels;

	private TextView titleNode;

	public AppConfigDialogAdd(MainActivityHome activity, PreferencesManager preferences) {
		super(activity, preferences, MenuGlobals.APPS_ADD);
		this.baseAppList = appList.getApps();
		this.baseSearchLabels = new String[baseAppList.size()];
		for (int i=0;i<baseAppList.size();i++) {
			baseSearchLabels[i] = baseAppList.get(i).label.toLowerCase(Locale.getDefault());
		}
	}

	@Override
	public void attachHeader() {

		DialogHelper.setCustomHeaderWidth(this);
		titleNode = ((TextView)findViewById(R.id.headerTitle));
		titleNode.setText(R.string.menuAddApps);

		OnQueryTextListener onQueryTextListener = new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return true;
			}
			@Override
			public boolean onQueryTextChange(String newText) {
				log.debug("onQueryTextChange", newText);
				updateAppList(newText);
				return true;
			}
		};

		OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
			private boolean titleVisible = true;
			@Override
			public void onFocusChange(View view, boolean queryTextFocused) {
				log.debug("onFocusChange", queryTextFocused);
				if (titleVisible) {
					titleNode.setVisibility(View.GONE);
					titleVisible = false;
				}
			}
		};

		final SearchView search = new SearchView(context);
		search.setOnQueryTextListener(onQueryTextListener);
		search.setOnQueryTextFocusChangeListener(onFocusChangeListener);

		View menuIcon = findViewById(R.id.headerIcon);
		menuIcon.setVisibility(View.GONE);
		ViewGroup container = ((ViewGroup)menuIcon.getParent());
		container.addView(search, container.indexOfChild(menuIcon));
	}

	private void updateAppList(String query) {

		log.debug("updateAppList", query);

		if (query==null || query.length()==0) {
			super.updateAppList(baseAppList);
			return;
		}

		final String searchstr = query.toLowerCase(Locale.getDefault());
		List<AppEntry> matchingApps = new ArrayList<AppEntry>();
		for (int i=0;i<baseSearchLabels.length;i++) {
			if (baseSearchLabels[i].contains(searchstr)) {
				log.debug("matching app", baseSearchLabels[i]);
				matchingApps.add(baseAppList.get(i));
			}
		}
		super.updateAppList(matchingApps);
	}

}

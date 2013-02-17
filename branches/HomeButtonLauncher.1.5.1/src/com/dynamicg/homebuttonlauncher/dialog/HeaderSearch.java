package com.dynamicg.homebuttonlauncher.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.dynamicg.common.Logger;
import com.dynamicg.homebuttonlauncher.AppEntry;
import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.dialog.AppConfigDialog.CustomHeader;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper;

public class HeaderSearch implements CustomHeader {

	private static final Logger log = new Logger(HeaderSearch.class);

	private final AppConfigDialog opener;
	private final Context context;
	private final List<AppEntry> baseAppList;
	private String[] baseSearchLabels = null; // lazy

	private TextView titleNode;


	public HeaderSearch(AppConfigDialog dialog) {
		this.opener = dialog;
		this.context = dialog.getContext();
		this.baseAppList = dialog.appList.getApps();
	}

	private void initSearchLabels() {
		baseSearchLabels = new String[baseAppList.size()];
		for (int i=0;i<baseAppList.size();i++) {
			baseSearchLabels[i] = baseAppList.get(i).label.toLowerCase(Locale.getDefault());
		}
	}

	@Override
	public void attach() {

		DialogHelper.setCustomHeaderWidth(opener);
		titleNode = ((TextView)opener.findViewById(R.id.headerTitle));
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

		View menuIcon = opener.findViewById(R.id.headerIcon);
		menuIcon.setVisibility(View.GONE);
		ViewGroup container = ((ViewGroup)menuIcon.getParent());
		container.addView(search, container.indexOfChild(menuIcon));
	}

	private void updateAppList(String query) {

		log.debug("updateAppList", query);

		if (baseAppList.size()==0) {
			return;
		}

		if (query==null || query.length()==0) {
			opener.updateAppList(baseAppList);
			return;
		}

		if (baseSearchLabels==null) {
			initSearchLabels();
		}

		final String searchstr = query.toLowerCase(Locale.getDefault());
		List<AppEntry> matchingApps = new ArrayList<AppEntry>();
		for (int i=0;i<baseSearchLabels.length;i++) {
			if (baseSearchLabels[i].contains(searchstr)) {
				log.debug("matching app", baseSearchLabels[i]);
				matchingApps.add(baseAppList.get(i));
			}
		}
		opener.updateAppList(matchingApps);
	}

}
package com.dynamicg.homebuttonlauncher.adapter;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dynamicg.homebuttonlauncher.AppListContainer;
import com.dynamicg.homebuttonlauncher.preferences.PrefSettings;

public class AppListAdapterMainStatic extends AppListAdapter {

	private final List<TextView> list;

	public AppListAdapterMainStatic(Activity activity, AppListContainer apps, PrefSettings settings) {
		super(activity, apps, settings);
		list = Arrays.asList(new TextView[apps.size()]);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (list.get(position)==null) {
			final TextView row = getOrCreateTextView(null); // make sure we don't recycle views
			bindView(applist.get(position), row);
			list.set(position, row);
		}
		return list.get(position);
	}

}
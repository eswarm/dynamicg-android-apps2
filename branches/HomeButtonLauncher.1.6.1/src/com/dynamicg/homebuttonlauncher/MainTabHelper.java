package com.dynamicg.homebuttonlauncher;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dynamicg.homebuttonlauncher.preferences.PreferencesManager;

public class MainTabHelper {

	private final Context context;
	private final MainActivityHome activity;
	private final PreferencesManager preferences;

	private int numTabs;
	private TextView[] tabs;

	public MainTabHelper(MainActivityHome activity, PreferencesManager preferences) {
		this.activity = activity;
		this.context = activity;
		this.preferences = preferences;
		this.numTabs = preferences.prefSettings.getNumTabs();
	}

	public void bindTabs() {
		final int selectedTabIndex = preferences.getTabIndex();
		final LayoutInflater inflater = activity.getLayoutInflater();
		final ViewGroup container = (ViewGroup)inflater.inflate(R.layout.tabs_container, null);

		View.OnClickListener clickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int tabIndex = (Integer)v.getTag();
				decorateTabs(container, tabIndex);
				activity.updateOnTabSwitch(tabIndex);
			}
		};

		View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				int tabIndex = (Integer)v.getTag();
				editLabel((TextView)v, tabIndex);
				return true;
			}
		};

		int height = (int)context.getResources().getDimension(R.dimen.tabHeight);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, height, 1f/numTabs);
		tabs = new TextView[numTabs];
		for (int i=0;i<numTabs;i++) {
			// for 1px margin between tabs:
			//			if (i>0) {
			//				lp.setMargins(0, 0, 1, 0);
			//			}
			TextView node = new TextView(context);
			node.setLayoutParams(lp);
			node.setOnClickListener(clickListener);
			node.setLongClickable(true);
			node.setOnLongClickListener(longClickListener);
			node.setSingleLine();
			node.setTag(i);
			setLabel(node, preferences.getTabTitle(i));
			container.addView(node);
			// done:
			tabs[i] = node;
		}

		decorateTabs(container, getValidatedTabNum(selectedTabIndex, numTabs));

		// attach after header
		View header = activity.findViewById(R.id.headerContainer);
		ViewGroup main = (ViewGroup)header.getParent();
		main.addView(container, main.indexOfChild(header)+1);

	}

	protected void editLabel(final TextView labelNode, final int tabIndex) {
		final String currentLabel = preferences.getTabTitle(tabIndex);

		final EditText editor = new EditText(context);
		editor.setText(currentLabel);
		editor.setSingleLine();
		if (currentLabel.length()>0) {
			editor.setSelection(currentLabel.length());
		}
		editor.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		DialogInterface.OnClickListener okListener = new OnClickListenerDialogWrapper(context) {
			@Override
			public void onClickImpl(DialogInterface dialog, int which) {
				String newLabel = editor.getText().toString();
				newLabel = newLabel!=null?newLabel:"";
				setLabel(labelNode, newLabel);
				preferences.writeTabTitle(tabIndex, newLabel);
			}
		};
		builder.setPositiveButton(R.string.buttonOk, okListener );
		builder.setNegativeButton(R.string.buttonCancel, null);
		builder.setView(editor);
		AlertDialog dialog = builder.show();

		// auto open keyboard
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	private void setLabel(TextView tab, String label) {
		tab.setTextSize(12);
		tab.setGravity(Gravity.CENTER);
		tab.setText(label);
		tab.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
	}

	public void redraw() {
		View tabContainer = activity.findViewById(R.id.mainTabContainer);
		if (tabContainer!=null) {
			((ViewGroup)tabContainer.getParent()).removeView(tabContainer);
		}
		bindTabs();
	}

	private void decorateTabs(ViewGroup container, int selectedTab) {
		for (int i=0;i<numTabs;i++) {
			View tab = container.getChildAt(i);
			int bgres = i==selectedTab ? R.drawable.tab_active_shape : R.drawable.tab_inactive_selector;
			tab.setBackgroundResource(bgres);
		}
	}

	public static int getValidatedTabNum(int selectedTabIndex, int numTabs) {
		return selectedTabIndex>=numTabs?0:selectedTabIndex;
	}

}

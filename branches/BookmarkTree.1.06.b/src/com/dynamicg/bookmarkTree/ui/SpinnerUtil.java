package com.dynamicg.bookmarkTree.ui;

import java.util.ArrayList;

import android.app.Dialog;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.dynamicg.bookmarkTree.PreferencesWrapper;

public class SpinnerUtil {

	private final Dialog dialog;

	public SpinnerUtil(Dialog dialog) {
		this.dialog = dialog;
		
	}
	
	public static class KeyValue {
		public int key;
		public String value;
		public KeyValue(int key, String value) {
			this.key = key;
			this.value = value;
		}
		public String toString() {
			return value; // for spinner
		}
	}
	
	public void bind(int spinnerResId, int currentKey, ArrayList<KeyValue> items) {
		Spinner spinner = (Spinner)dialog.findViewById(spinnerResId);
		ArrayAdapter<KeyValue> adapter = new ArrayAdapter<KeyValue> ( spinner.getContext()
				, android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner.setAdapter(adapter);
		
		for (int i=0;i<items.size();i++) {
			if (items.get(i).key==currentKey) {
				spinner.setSelection(i);
				break;
			}
		}
		
	}
	
	public int getCurrentValue(int spinnerResId) {
		Spinner spinner = (Spinner)dialog.findViewById(spinnerResId);
		KeyValue item = (KeyValue)spinner.getSelectedItem();
		return item==null ? 0 : item.key;
	}
	
	
	public static ArrayList<KeyValue> getListStyleItems() {
		ArrayList<KeyValue> list = new ArrayList<KeyValue>();
		list.add(new KeyValue(PreferencesWrapper.LIST_STYLE_CLASSIC, "Classic"));
		list.add(new KeyValue(PreferencesWrapper.LIST_STYLE_COMPACT, "Compact"));
		return list;
	}
	
	public static ArrayList<KeyValue> getSortOptionItems() {
		ArrayList<KeyValue> list = new ArrayList<KeyValue>();
		list.add(new KeyValue(PreferencesWrapper.SORT_ALPHA, "Alpha overall"));
		list.add(new KeyValue(PreferencesWrapper.SORT_FOLDERS_BEFORE_BM, "Folders before bookmarks"));
		list.add(new KeyValue(PreferencesWrapper.SORT_BM_BEFORE_FOLDERS, "Bookmarks before folders"));
		return list;
	}
	
}

package com.dynamicg.bookmarkTree.prefs;

import java.util.ArrayList;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;

public class PrefEntryInt {

	protected static ArrayList<PrefEntryInt> cache = new ArrayList<PrefEntryInt>();

	public final String name;
	private final int defValue;
	public int value;
	public int updatedValue;

	public PrefEntryInt(String name, int defValue) {
		this.name = name;
		this.defValue = defValue;
		this.value = BookmarkTreeContext.settings.getInt(name, defValue);
		cache.add(this);
	}

	public boolean isOn() {
		return value==1;
	}

	public void setNewValue(int i) {
		this.updatedValue = i;
	}

	public static void resetUpdatedValue() {
		for (PrefEntryInt item:cache) {
			item.updatedValue = item.value;
		}
	}

	public static void pushNewValue() {
		for (PrefEntryInt item:cache) {
			item.value = item.updatedValue;
		}
	}

	public void reload() {
		this.value = BookmarkTreeContext.settings.getInt(name, defValue);
	}

}

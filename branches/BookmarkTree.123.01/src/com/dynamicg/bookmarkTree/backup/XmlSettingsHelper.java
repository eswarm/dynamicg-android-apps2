package com.dynamicg.bookmarkTree.backup;

import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;

import org.xmlpull.v1.XmlSerializer;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;

public class XmlSettingsHelper {

	public static void append(XmlSerializer serializer)
			throws IOException {
		Map<String, ?> prefs = BookmarkTreeContext.settings.getAll();
		TreeSet<String> sortedKeys = new TreeSet<String>(prefs.keySet());
		for (String key : sortedKeys) {
			Object o = prefs.get(key);
			serializer.startTag(null, Tags.PREF_ENTRY);
			serializer.attribute(null, Tags.PREF_NAME, key);
			serializer.attribute(null, Tags.PREF_TYPE, o.getClass().getSimpleName());
			serializer.attribute(null, Tags.PREF_VALUE, o.toString());
			serializer.endTag(null, Tags.PREF_ENTRY);
		}
	}

}

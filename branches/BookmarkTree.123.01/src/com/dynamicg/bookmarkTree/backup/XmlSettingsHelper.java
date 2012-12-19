package com.dynamicg.bookmarkTree.backup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;
import com.dynamicg.common.Logger;

public class XmlSettingsHelper {

	private static final Logger log = new Logger(XmlSettingsHelper.class);

	public static void writeSettings(XmlSerializer serializer)
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

	public static class Settings {
		public String name;
		public String value;
		public String datatype;
	}

	public static void read(XmlPullParser parser, ArrayList<Settings> settings) {
		Settings entry = new Settings();
		entry.name = parser.getAttributeValue(null, Tags.PREF_NAME);
		entry.value = parser.getAttributeValue(null, Tags.PREF_VALUE);
		entry.datatype = parser.getAttributeValue(null, Tags.PREF_TYPE);
		settings.add(entry);

		if (log.debugEnabled) {
			log.debug("pref entry loaded", entry.name, entry.value, entry.datatype);

		}
	}

}
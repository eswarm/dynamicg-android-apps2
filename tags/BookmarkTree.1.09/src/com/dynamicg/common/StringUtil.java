package com.dynamicg.common;

import java.util.regex.Pattern;

import android.content.Context;

public class StringUtil {

	/*
	 * RE-safe split
	 */
	public static String[] split(String value, String expr) {
		return value.split(Pattern.quote(expr));
	}
	
	/*
	 * RE-safe replace
	 */
	public static String replaceAll(String value, String search, String replace) {
		return value.replaceAll(Pattern.quote(search),replace);
	}
	public static String replaceFirst(String value, String search, String replace) {
		return value.replaceFirst(Pattern.quote(search),replace);
	}

	public static String textWithParam(Context context, int res, int p1) {
		return context.getString(res).replace("{1}", Integer.toString(p1));
	}
	public static String textWithParam(String text, int p1) {
		return text.replace("{1}", Integer.toString(p1) );
	}
	
}
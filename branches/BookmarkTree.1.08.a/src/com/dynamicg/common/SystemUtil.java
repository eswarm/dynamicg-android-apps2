package com.dynamicg.common;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.Context;
import android.provider.Settings.Secure;
import android.view.LayoutInflater;

public class SystemUtil {

	private static final String MY_HTC_HERO = "2001459694cb4e56";
	private static final String EMULATOR_2_2 = "9774d56d682e549c";

	public static boolean development;
	
	public static void init(Context maincontext) {
		String androidId = Secure.getString(maincontext.getContentResolver(), Secure.ANDROID_ID);
		boolean emulator = androidId==null || androidId.equals(EMULATOR_2_2);
		development = emulator || MY_HTC_HERO.equals(androidId);
	}
	
	public static String getExceptionText(Throwable exception) {
		final int limit = 1200;
		
		if (exception==null) {
			return "<no exception>";
		}
		
		StringWriter sw = new StringWriter() ;
		exception.printStackTrace(new PrintWriter(sw)) ;
		String text = sw.getBuffer().toString();

		return text.length()>limit ? text.substring(0,limit)+"..." : text;
		
	}
	
	public static LayoutInflater getLayoutInflater(Context context) {
		return (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

}

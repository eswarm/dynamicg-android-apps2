package com.dynamicg.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Log;

public class SystemUtil {

	private static final String MY_HTC_HERO = "2001459694cb4e56";
	private static final String MY_HTC_DESIRE = "200145745b9424b8";
	private static final String EMULATOR_2_2 = "9774d56d682e549c";

	private static String androidId;
	private static boolean emulator;
	
	public static void init(Context maincontext) {
		androidId = Secure.getString(maincontext.getContentResolver(), Secure.ANDROID_ID);
		emulator = androidId==null || androidId.equals(EMULATOR_2_2);
		Log.i("dynamicG", "SystemUtil - initInstance done: androidid=["+androidId+"]");
	}
	
	public static String getAndroidId() {
		return androidId;
	}
	
	public static String getAllSystemProps() {
		Properties props = System.getProperties();
		StringBuffer sb = new StringBuffer();
		for ( Object key:props.keySet()) {
			sb.append("["+key+"]=["+props.getProperty((String)key)+"]\n") ;
		}
		return sb.toString();
	}
	
	public static boolean isDevelopmentOrDevDevice() {
		return emulator || MY_HTC_HERO.equals(androidId);
	}
	
	public static boolean isMyProdDevice() {
		return MY_HTC_DESIRE.equals(androidId);
	}
	
	public static void sleep(long mili) {
		try { 
			Thread.sleep(mili);
		}
		catch (Exception e) {}
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
	
	public static void dumpIfDevelopment(Throwable e) {
		if (SystemUtil.isDevelopmentOrDevDevice()) {
			e.printStackTrace();
		}
	}

}

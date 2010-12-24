package com.dynamicg.common;

import android.util.Log;

public class Logger {

	private static boolean TRACE_ENABLED ;
	private static boolean DEBUG_ENABLED ;

	static {
		resetLogging();
	}

	public final boolean debugEnabled = DEBUG_ENABLED;
	public final boolean traceEnabled = TRACE_ENABLED;
	
	public static void resetLogging() {
		if ( SystemUtil.isDevelopmentOrDevDevice() ) {
			TRACE_ENABLED = false;
			DEBUG_ENABLED = true;
		}
		else {
			TRACE_ENABLED = false;
			DEBUG_ENABLED = false;
		}
	}
	
	private final String textPrefix;

	public Logger(Class<?> cls) {
		if (DEBUG_ENABLED) {
			String clsname = cls.getName();
			textPrefix = "DG/"+clsname.substring(clsname.lastIndexOf(".")+1);
		}
		else {
			textPrefix = null;
		}
	}

	private static StringBuffer append(Object... args) {
		StringBuffer sb = new StringBuffer(args[0].toString());
		for ( int i=1;i<args.length;i++ ) {
			sb.append(" [");
			sb.append(args[i]);
			sb.append("]");
		}
		return sb;
	}
	
	public void warn(String text, Object... args) {
		StringBuffer sb = append(text, args);
		Log.w(textPrefix, sb.toString());
	}

	public void debug(String text, Object... args) {
		if (!DEBUG_ENABLED) {
			return;
		}
		StringBuffer sb = append(text, args);
		Log.d(textPrefix, sb.toString());
	}

	public void trace(String text, Object... args) {
		if (!TRACE_ENABLED ) {
			return;
		}
		StringBuffer sb = append(text, args);
		Log.v(textPrefix, sb.toString());
	}

}

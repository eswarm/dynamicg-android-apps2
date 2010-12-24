package com.dynamicg.common;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.Context;
import android.view.LayoutInflater;

public class SystemUtil {

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

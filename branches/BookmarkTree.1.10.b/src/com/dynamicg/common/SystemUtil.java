package com.dynamicg.common;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

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

	public static void toastShort(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}
	public static void toastLong(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
	}
	
	public static void underline(TextView view) {
		SpannableString content = new SpannableString(view.getText());
	    content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
	    view.setText(content);
	}
	
}

package com.dynamicg.common;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;

import com.dynamicg.bookmarkTree.ui.AboutDialog;

public class MailSender {

	private static void createIntent ( Context context, String title, String body) {
		Intent msg = new Intent(Intent.ACTION_SEND);
		msg.setType("text/plain");
		msg.putExtra(Intent.EXTRA_SUBJECT, title);
		msg.putExtra(Intent.EXTRA_TEXT, body);
		msg.putExtra(Intent.EXTRA_EMAIL, new String[]{AboutDialog.AUTHOR} );
		context.startActivity(Intent.createChooser(msg, "Send error report"));
	}
	
	public static void emailError(Context context, Throwable exception) {
		String title = "Bookmark Tree - Error ("+Locale.getDefault().getLanguage()+")";
		String body = SystemUtil.getExceptionText(exception);
		createIntent(context, title, body);
	}
	
}

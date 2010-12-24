package com.dynamicg.common;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;

import com.dynamicg.bookmarkTree.ui.AboutDialog;

public class MailSender {

	private static void prepareBasicSendMailIntent ( Context context, String title, String body) {
		
		final String targetIntent;
		targetIntent = Intent.ACTION_SEND;
		Intent msg = new Intent(targetIntent);
		
		msg.putExtra(Intent.EXTRA_SUBJECT, title);
		msg.putExtra(Intent.EXTRA_TEXT, body);
		
		// email recipient
		String emailRecipient = AboutDialog.AUTHOR;
		msg.putExtra(Intent.EXTRA_EMAIL, new String[]{emailRecipient} );
		
		context.startActivity(Intent.createChooser(msg, title));
	}
	
	public static void emailError(Context context, Throwable exception) {
		String title = "Bookmark Tree Manager ("+Locale.getDefault().getLanguage()+")";
		String body = SystemUtil.getExceptionText(exception);
		prepareBasicSendMailIntent(context, title, body);
	}
	
}

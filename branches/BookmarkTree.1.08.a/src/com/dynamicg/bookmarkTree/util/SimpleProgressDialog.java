package com.dynamicg.bookmarkTree.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.dynamicg.bookmarkTree.R;
import com.dynamicg.common.Logger;
import com.dynamicg.common.MailSender;
import com.dynamicg.common.SimpleAlertDialog;
import com.dynamicg.common.SystemUtil;

public abstract class SimpleProgressDialog {

	private final Context context;

	public SimpleProgressDialog(Context context, String title) {
		
		this.context = context;
		final ProgressDialog progressDialog;
		progressDialog = new ProgressDialog(context);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(title);
		progressDialog.setCancelable(false);
		progressDialog.show();

		final Handler doneHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what==0) {
					done();
					progressDialog.dismiss();
				}
				else if (msg.what==1) {
					progressDialog.dismiss();
					handleError ( (Throwable)msg.obj );
				}
			}
		};

		Thread progressThread = new Thread(new Runnable() {
			public void run() {
				try {
					backgroundWork();
					doneHandler.sendEmptyMessage(0);
				}
				catch (Throwable e) {
					Message msg = new Message();
					msg.what=1;
					msg.obj=e;
					doneHandler.sendMessage(msg);
				}
			}
		}) ;

		progressThread.start();

	}
	
	public abstract void backgroundWork();
	public abstract void done();
	
	/*
	 * override in implementations if required
	 */
	public void handleError(final Throwable e) {
		new SimpleAlertDialog(context, "Error", "Email DEV", context.getString(R.string.commonClose) ) {
			
			@Override
			public String getScrollViewText() {
				return SystemUtil.getExceptionText(e);
			}

			@Override
			public void onPositiveButton() {
				MailSender.emailError(context, e);
			}
			
		};
		Logger.dumpIfDevelopment(e);
	}
	
}

package com.dynamicg.bookmarkTree.ui;

import android.view.View;
import android.widget.TextView;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;
import com.dynamicg.bookmarkTree.R;
import com.dynamicg.common.ContextUtil;
import com.dynamicg.common.SimpleAlertDialog;
import com.dynamicg.common.StringUtil;
import com.dynamicg.common.SystemUtil;

public class AboutDialog {

	public static final String AUTHOR = "dynamicg.android@gmail.com";
	
	public static void show(final BookmarkTreeContext ctx) {

		final String[] appinfo = ContextUtil.getVersion(ctx.activity);

		new SimpleAlertDialog(ctx.activity, "About", "Close") {
			@Override
			public View getBody() {
				View body = SystemUtil.getLayoutInflater(ctx.activity).inflate(R.layout.about, null);

				TextView titleItem = (TextView)body.findViewById(R.id.disclaimerTitle);
				String title = titleItem.getText().toString();
				title = StringUtil.replaceFirst(title, "{version}", appinfo[0]);
				titleItem.setText(title);
				
				TextView revisionItem = (TextView)body.findViewById(R.id.disclaimerSvnRevision);
				String revisionText = revisionItem.getText().toString();
				revisionText = StringUtil.replaceFirst(revisionText, "${revision}", appinfo[1]);
				revisionItem.setText(revisionText.trim());
				
				return body;
			}
		};
	}


}

package com.dynamicg.bookmarkTree.dialogs;

import android.view.View;
import android.widget.TextView;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;
import com.dynamicg.bookmarkTree.R;
import com.dynamicg.common.ContextUtil;
import com.dynamicg.common.SimpleAlertDialog;
import com.dynamicg.common.SystemUtil;

public class AboutDialog {

	public static final String AUTHOR = "dynamicg.android@gmail.com";
	
	public static void show(final BookmarkTreeContext ctx) {

		final String[] appinfo = ContextUtil.getVersion(ctx.activity);

		new SimpleAlertDialog(ctx.activity, "About", "Close") {
			@Override
			public View getBody() {
				View body = SystemUtil.getLayoutInflater(ctx.activity).inflate(R.layout.about, null);

				String title = "Bookmark Tree Manager "+appinfo[0];
				TextView titleItem = (TextView)body.findViewById(R.id.aboutSubTitle);
				titleItem.setText(title);
				
				String revisionText = "\nThis app is open source:"
					+ "\nhttps://dynamicg-android-apps2.googlecode.com/svn/trunk/BookmarkTree"
					+ "\n"
					+ "\nProgrammed by "+AUTHOR
					+ "\nSVN Revision: " + (appinfo[1])
					+ "\n"
					+ "\n"
					;
				TextView revisionItem = (TextView)body.findViewById(R.id.aboutBody);
				revisionItem.setText(revisionText);
				
				return body;
			}
		};
	}


}

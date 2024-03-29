package com.dynamicg.bookmarkTree.dialogs;

import java.util.ArrayList;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.dynamicg.bookmarkTree.BookmarkTreeContext;
import com.dynamicg.bookmarkTree.R;
import com.dynamicg.bookmarkTree.data.writehandler.BookmarkDeletionHandler;
import com.dynamicg.bookmarkTree.data.writehandler.BookmarkUpdateHandler;
import com.dynamicg.bookmarkTree.data.writer.BookmarkWriter;
import com.dynamicg.bookmarkTree.model.Bookmark;
import com.dynamicg.bookmarkTree.model.BrowserBookmarkBean;
import com.dynamicg.bookmarkTree.model.FolderBean;
import com.dynamicg.bookmarkTree.util.BookmarkUtil;
import com.dynamicg.bookmarkTree.util.DialogButtonPanelWrapper;
import com.dynamicg.bookmarkTree.util.DialogHelper;
import com.dynamicg.common.ErrorNotification;
import com.dynamicg.common.Logger;
import com.dynamicg.common.SimpleAlertDialog;
import com.dynamicg.common.StringUtil;
import com.dynamicg.common.SystemUtil;

public class EditBookmarkDialog extends Dialog {

	private static final Logger log = new Logger(EditBookmarkDialog.class);
	public static final BrowserBookmarkBean NEW_BOOKMARK = BrowserBookmarkBean.createNew();

	private final BookmarkTreeContext ctx;
	private final Bookmark bookmark;
	private final boolean forCreateBookmark;

	private EditText newNodeTitleItem;
	private Spinner parentFolderSpinner;
	private EditText addToNewFolderItem;
	private EditText urlItem;

	public EditBookmarkDialog(BookmarkTreeContext ctx, Bookmark bookmark) {
		super(ctx.activity);
		this.ctx = ctx;
		this.bookmark = bookmark;
		this.forCreateBookmark = bookmark==NEW_BOOKMARK;
		DialogHelper.expandContent(this, R.layout.edit_body);
		this.show();
	}

	public EditBookmarkDialog(BookmarkTreeContext ctx) {
		this(ctx, NEW_BOOKMARK);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		final int alertTitle = forCreateBookmark ? R.string.commonNewBookmark
				: bookmark.isBrowserBookmark() ? R.string.commonEditBookmark : R.string.commonEditFolder;
		setTitle(alertTitle);

		final String bookmarkTitle = bookmark.getDisplayTitle();
		newNodeTitleItem = (EditText)findViewById(R.id.editBookmarkNewTitle);
		newNodeTitleItem.setText(bookmarkTitle);

		if (bookmark.isBrowserBookmark()) {
			urlItem = (EditText)findViewById(R.id.editBookmarkUrl);
			urlItem.setText(bookmark.getUrl());
		}
		else {
			findViewById(R.id.editBookmarkUrlContainer).setVisibility(View.GONE);
		}

		parentFolderSpinner = (Spinner)findViewById(R.id.editBookmarkParentFolder);
		prepareParentFolderSpinner(bookmark);

		addToNewFolderItem = (EditText)findViewById(R.id.editBookmarkAddToNewFolder);

		int dialogType = forCreateBookmark ? DialogButtonPanelWrapper.TYPE_CREATE_CANCEL : DialogButtonPanelWrapper.TYPE_SAVE_CANCEL;
		new DialogButtonPanelWrapper(this, dialogType) {
			@Override
			public void onPositiveButton() {
				saveBookmark(bookmark);
			}
		};

		/*
		 * delete
		 */
		if (forCreateBookmark) {
			removePanel(R.id.editBookmarkDeletePanel);
		}
		else {
			TextView deleteLabel = (TextView)findViewById(R.id.editBookmarkDeleteText);
			View deleteIcon = findViewById(R.id.editBookmarkDeleteIcon);
			deleteLabel.setText(bookmark.isFolder() ? R.string.editLinkDeleteFolder : R.string.editLinkDeleteBookmark);
			View.OnClickListener deleteAction = (new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					deleteConfirmation();
				}
			});
			deleteIcon.setOnClickListener(deleteAction);
			deleteLabel.setOnClickListener(deleteAction);
		}

		/*
		 * create shortcut
		 */
		if (forCreateBookmark || bookmark.isFolder()) {
			removePanel(R.id.editBookmarkCreateShortcutPanel);
		}
		else {
			View.OnClickListener action = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					new ShortcutCreateDialog(EditBookmarkDialog.this, ctx, bookmark);
				}
			};
			findViewById(R.id.editCreateShortcutIcon).setOnClickListener(action);
			findViewById(R.id.editCreateShortcutText).setOnClickListener(action);
		}

	}

	private void removePanel(int id) {
		View view = findViewById(id);
		view.setVisibility(View.INVISIBLE);
		view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,0));
	}

	private void prepareParentFolderSpinner(final Bookmark bookmark) {

		/*
		 * .copy the "folders" cache
		 * .remove all folders below the given bookmark (we don't want to move the folder to one of its children)
		 * .add <no folder> to top
		 */
		ArrayList<FolderBean> folders = new ArrayList<FolderBean>(ctx.bookmarkManager.getAllFolders());
		if (bookmark.isFolder()) {
			// this is a folder, so we remove all children from the 'new parent folder' list
			folders.removeAll(bookmark.getTree(Bookmark.TYPE_FOLDER));
			folders.remove(bookmark); // remove 'self' from list
		}
		folders.add(0, FolderBean.ROOT);

		ArrayAdapter<FolderBean> adapter = new ArrayAdapter<FolderBean>(ctx.activity
				, android.R.layout.simple_spinner_item, folders);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		parentFolderSpinner.setAdapter(adapter);

		// sync position
		int pos = bookmark.getParentFolder()!=null ? folders.indexOf(bookmark.getParentFolder()) : -1 ;
		if (log.isDebugEnabled) {
			log.debug("parent folder Spinner", bookmark.getParentFolder(), pos );
		}
		if (pos>=0) {
			parentFolderSpinner.setSelection(pos);
		}

	}

	private static String getEditTextValue(EditText item) {
		return item!=null ? item.getText().toString().trim() : null;
	}

	private void saveBookmark(Bookmark bookmark) {

		String newNodeTitle = getEditTextValue(newNodeTitleItem) ;
		String addToNewFolderTitle = getEditTextValue(addToNewFolderItem);
		String newUrl = getEditTextValue(urlItem);

		if (newNodeTitle.length()==0) {
			SimpleAlertDialog.plainInfo(getContext(), R.string.hintTitleMissing);
			return;
		}
		if (bookmark.isBrowserBookmark() && newUrl.length()==0) {
			SimpleAlertDialog.plainInfo(getContext(), R.string.hintUrlMissing);
			return;
		}

		if (addToNewFolderTitle!=null && addToNewFolderTitle.length()>0) {
			newNodeTitle = addToNewFolderTitle + ctx.getNodeConcatenation() + newNodeTitle;
		}

		FolderBean newParentFolder = (FolderBean)parentFolderSpinner.getSelectedItem();
		if (newParentFolder==FolderBean.ROOT) {
			newParentFolder=null;
		}

		if (log.isDebugEnabled) {
			log.debug("saveBookmark", newNodeTitle, newParentFolder, addToNewFolderTitle );
		}

		try {
			if (bookmark==NEW_BOOKMARK) {
				if (newParentFolder!=null) {
					// prepend the folder path
					newNodeTitle = newParentFolder.getFullTitle() + ctx.getNodeConcatenation() + newNodeTitle;
				}
				new BookmarkWriter(ctx).insert(newNodeTitle, BookmarkUtil.patchProtocol(newUrl) );
			}
			else {
				BookmarkUpdateHandler upd = new BookmarkUpdateHandler(ctx);
				upd.update ( bookmark, newNodeTitle, newParentFolder, newUrl );
			}
		}
		catch (final Throwable exception) {
			String errortext = "Cannot save bookmark";
			if (SystemUtil.isInvalidBrowserContentUrl(exception)) {
				errortext = "Cannot save. Default Web Browser disabled?";
			}
			ErrorNotification.notifyError(getContext(), errortext, exception);
		}

		ctx.reloadAndRefresh();
		this.dismiss();

	}

	private String getText(int res) {
		return getContext().getString(res);
	}

	private void deleteConfirmation() {

		String alertTitle;
		if (bookmark.isBrowserBookmark()) {
			alertTitle = getText(R.string.actionDeleteBookmark);
		}
		else {
			int num = bookmark.getTree(Bookmark.TYPE_BROWSER_BOOKMARK).size();
			if (num<=1) {
				alertTitle = getText(R.string.actionDeleteFolderOne);
			}
			else {
				alertTitle = StringUtil.textWithParam(getContext(), R.string.actionDeleteFolderMany, num );
			}
		}

		new SimpleAlertDialog.OkCancelDialog(ctx.activity, alertTitle) {
			@Override
			public void onPositiveButton() {
				deleteBookmark();
			}
		};
	}

	private void deleteBookmark() {
		try {
			new BookmarkDeletionHandler(ctx,bookmark);
			ctx.reloadAndRefresh();
		}
		catch (final Throwable exception) {
			ErrorNotification.notifyError(getContext(), "Cannot delete bookmark", exception);
		}
		this.dismiss();
	}

}

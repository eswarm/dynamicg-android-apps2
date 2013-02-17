package com.dynamicg.homebuttonlauncher.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.dynamicg.homebuttonlauncher.MenuGlobals;
import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.dialog.AppConfigDialog.CustomHeader;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper;
import com.dynamicg.homebuttonlauncher.tools.PopupMenuWrapper;
import com.dynamicg.homebuttonlauncher.tools.PopupMenuWrapper.PopupMenuItemListener;

public class HeaderSortReset implements CustomHeader {

	private final AppConfigDialog dialog;
	private final Context context;

	public HeaderSortReset(AppConfigDialog dialog) {
		this.dialog = dialog;
		this.context = dialog.getContext();
	}

	@Override
	public void attach() {
		final View anchor = DialogHelper.getAnchor(dialog);
		final PopupMenuItemListener listener = new PopupMenuItemListener() {
			@Override
			public void popupMenuItemSelected(int id) {
				if (id==MenuGlobals.RESET) {
					confirmSortReset();
				}
			}
		};
		final PopupMenuWrapper menuWrapper = new PopupMenuWrapper(context, anchor, listener);
		menuWrapper.attachToAnchorClick();
		menuWrapper.addItem(MenuGlobals.RESET, R.string.menuReset);
	}

	private void confirmSortReset() {
		AlertDialog.Builder b = new AlertDialog.Builder(context);
		String label = context.getString(R.string.menuReset)+"?";
		b.setTitle(label);
		b.setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				HeaderSortReset.this.dialog.doSortReset();
			}
		} );
		b.setNegativeButton(R.string.buttonCancel, null);
		b.show();
	}

}

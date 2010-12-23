package com.dynamicg.bookmarkTree.backup;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;

import com.dynamicg.bookmarkTree.R;

public class BackupRestoreUtil {

	public static String breakToLines(String s, int linesize) {
		
		final StringBuffer sb = new StringBuffer();
		final int strlen = s.length();
		
		int counter=0, startpos, endpos;
		while (true) {
			startpos = counter*linesize;
			endpos = startpos+linesize;
			if (strlen>endpos) {
				sb.append(s.substring(startpos,endpos));
				sb.append("\n");
			}
			else if (s.length() > startpos) {
				sb.append(s.substring(startpos));
			}
			else {
				break;
			}
			counter++;
		}
		
		return sb.toString();
	}
	
	public static ArrayList<byte[]> getIcons(Context context) {
		
		Resources resources = context.getResources();
		
		int[] icons = new int[] {
				R.drawable.menu_prefs
				, R.drawable.menu_reload
				, R.drawable.menu_create
				, R.drawable.menu_expand
				, R.drawable.menu_collapse
				, R.drawable.menu_save
		};
		
		ArrayList<byte[]> images = new ArrayList<byte[]>();
		
		for (int icon:icons) {
			Bitmap bitmap = BitmapFactory.decodeResource(resources, icon);
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bitmap.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
			byte[] bitmapdata = bos.toByteArray();
			
			images.add(bitmapdata);
		}
		
		return images;
	}
	
}

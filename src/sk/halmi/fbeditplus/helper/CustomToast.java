/*
 * Frozen Bubble Level Editor Plus
 *
 * Edit and load custom level packs to Frozen Bubble for Android.
 *
 * Copyright (C) 2016 Rudo Halmi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package sk.halmi.fbeditplus.helper;

import sk.halmi.fbeditplus.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast extends Toast {
	private static Toast toast;
	private static View textEntryView; 


	private CustomToast(Context context) {
		super(context);
		textEntryView = LayoutInflater.from(context).inflate(R.layout.custom_toast, null);
	}

	public static Toast makeText(Context context, int message, int length) {
		if (null == toast) {
			toast = new CustomToast(context);
		}
		toast.setDuration(length);
		((TextView)textEntryView.findViewById(R.id.toast_text)).setText(message);
		toast.setView(textEntryView);
		return toast;
	}

	public static Toast makeText(Context context, CharSequence message, int length) {
		if (null == toast) {
			toast = new CustomToast(context);
		}
		toast.setDuration(length);
		((TextView)textEntryView.findViewById(R.id.toast_text)).setText(message);
		toast.setView(textEntryView);
		return toast;
	}

}

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

package com.zzz.ecity.android.applibrary.view;

import com.zzz.ecity.android.applibrary.R;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ActionSheet {
	/**
	 * 
	 * @Title: show
	 * @Description: 弹出actionsheet
	 * @param context
	 *            activity context
	 * @param title
	 *            actionsheet title
	 * @param choices
	 *            actionsheet inner choices
	 * @param listener
	 *            actionsheet click listener
	 * @return
	 */
	private static Dialog dialog = null;
	
	public static Dialog show(Context context, String title, String[] choices,
			OnItemClickListener listener) {
		return show(context, title, choices, listener, null);
	}

	/**
	 * 
	 * @Title: show
	 * @Description: 弹出actionsheet
	 * @param context
	 *            activity context
	 * @param title
	 *            actionsheet title
	 * @param choices
	 *            actionsheet inner choices
	 * @param listener
	 *            actionsheet click listener
	 * @param cancelListener
	 *            actionsheet cancel listener
	 * @return
	 */
	public static Dialog show(Context context, String title, String[] choices,
			OnItemClickListener listener, OnClickListener cancelListener) {
		if(null != dialog)
		{
			if(dialog.isShowing())
				dialog.dismiss();
		}
		
		dialog = new Dialog(context, R.style.ActionSheet);
		// inflat layout
		LinearLayout layout = (LinearLayout) LayoutInflater.from(context)
				.inflate(R.layout.actionsheet, null);
		// set title
		TextView titleTv = (TextView) layout.findViewById(R.id.title);
		titleTv.setText(title);
		// set choices
		ListView listView = (ListView) layout.findViewById(R.id.sheetList);
		listView.setAdapter(new ArrayAdapter<String>(context,
				R.layout.sheet_item, choices));
		listView.setOnItemClickListener(listener);
		// set cancel
		if (cancelListener != null) {
			layout.findViewById(R.id.cancel).setOnClickListener(cancelListener);
		} else {
			layout.findViewById(R.id.cancel).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
			});
		}
		// set window param
		layout.setMinimumWidth(context.getResources().getDisplayMetrics().widthPixels);
		Window window = dialog.getWindow();
		LayoutParams lp = window.getAttributes();
		lp.x = 0;
		lp.gravity = Gravity.BOTTOM;
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(layout);
		dialog.show();
		return dialog;
	}

	public static void dismiss()
	{
		if(null != dialog)
		{
			if(dialog.isShowing())
				dialog.dismiss();
		}
	}
}

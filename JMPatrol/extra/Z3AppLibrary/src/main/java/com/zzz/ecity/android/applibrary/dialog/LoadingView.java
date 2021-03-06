package com.zzz.ecity.android.applibrary.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

import com.zzz.ecity.android.applibrary.R;

public class LoadingView extends Dialog {

	@SuppressWarnings("unused")
	private Context context = null;
	public LoadingView(Context context) {
		super(context);
		this.context = context;
	}
	public LoadingView(Context context, int theme) {
		super(context, theme);
	}
	public static LoadingView createDialog(Context context) {
		LoadingView loadingView = new LoadingView(context,
				R.style.AlertView);
		loadingView.setContentView(R.layout.panel_loading);
		loadingView.getWindow().getAttributes().gravity = Gravity.CENTER;
		loadingView.setCanceledOnTouchOutside(false);
		return loadingView;
	}
	public void onWindowFocusChanged(boolean hasFocus) {
//		ProgressBar progressbar = (ProgressBar) this
//				.findViewById(R.id.progressbar);
//		if(progressbar == null)
//			return;
//		AnimationDrawable animationDrawable = (AnimationDrawable) progressbar
//				.getBackground();
//		animationDrawable.start();
	}
	/***
	 * [Summary] setTitile 设置标题
	 * @param strTitle
	 * @return
	 */
	public LoadingView setTitile(String strTitle) {
		TextView tvMsg = (TextView) this
				.findViewById(R.id.id_tv_loadingmsg);
		if (tvMsg != null) {
			tvMsg.setText(strTitle);
		}
		return this;
	}
	/**
	 * [Summary] setMessage 信息
	 * @param strMessage
	 * @return
	 * 
	 */
	public LoadingView setMessage(String strMessage) {
		TextView tvMsg = (TextView) this
				.findViewById(R.id.id_tv_loadingmsg);
		if (tvMsg != null) {
			tvMsg.setText(strMessage);
		}
		return this;
	}
}

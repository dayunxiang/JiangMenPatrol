package com.zzz.ecity.android.applibrary.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.z3app.android.util.FileUtil;
import com.zzz.ecity.android.applibrary.R;
import com.zzz.ecity.android.applibrary.dialog.AlertView;
import com.zzz.ecity.android.applibrary.dialog.AlertView.AlertStyle;
import com.zzz.ecity.android.applibrary.dialog.AlertView.OnAlertViewListener;
import com.zzz.ecity.android.applibrary.dialog.ProgressDialog;
import com.zzz.ecity.android.applibrary.dialog.ProgressDialog.OnCancleListener;

public class AutomaticUpdate { /* 下载中 */
	private static final int DOWNLOAD = 1;
	/* 下载结束 */
	private static final int DOWNLOAD_FINISH = 2;
	private static final int DOWNLOADFAILED = 3;
	/* 保存解析的XML信息 */
	HashMap<String, String> mHashMap;
	/* 下载保存路径 */
	private String mSavePath;
	/* 记录进度条数量 */
	private int progress;
	/* 是否取消更新 */
	private boolean cancelUpdate = false;

	private Context mContext;
	/* 更新进度条 */
	private ProgressDialog mDownloadDialog;
	private int type = 0;//
	/***
	 * 初始化一个更新
	 * 
	 * @param activity
	 * @param type
	 *            0 optional 1 forcibly
	 */

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// 正在下载
			case DOWNLOADFAILED:
				// 设置进度条位置
				if (null != mDownloadDialog) {
					mDownloadDialog.setTitle("下载失败!");
				}
				break;

			// 正在下载
			case DOWNLOAD:
				// 设置进度条位置
				if (null != mDownloadDialog) {
					mDownloadDialog.setProgress(progress);
				}
				break;
			case DOWNLOAD_FINISH:
				// 安装文件
				installApk();
				break;
			default:
				break;
			}
		};
	};

	public AutomaticUpdate(Context context) {
		this.mContext = context;
	}

	/**
	 * 检测软件更新
	 * @param apkUrl apk下载路径
	 * @param serviceCode 服务端版本
	 * @param type 更新类型 1强制更新
	 * @param msg 更新说明
	 */
	public void checkUpdate(String apkUrl, int serviceCode, int type,String msg) {
		mSavePath = FileUtil.getInstance(mContext).getRootPath();
		if (mSavePath == null || mSavePath.isEmpty()){
			return;
		}

		File file = new File(mSavePath);
		// 判断文件目录是否存在
		if (!file.exists()) {
			file.mkdirs();
		}

		String apkfileName = "z3app_mobile_5ef8aaedf32-" + serviceCode;
		mHashMap = new HashMap<String, String>();
		mHashMap.put("version", String.valueOf(serviceCode));
		mHashMap.put("name", apkfileName);
		mHashMap.put("url", apkUrl);
		mHashMap.put("msg", ""+msg);
		
		if (type != 0 && type != 1){
			this.type = 0;
		} else {
			this.type = type;
		}

		if (isUpdate(serviceCode)) {
			if (this.type == 1) {
				// 显示下载对话框
				showDownloadDialog();
			} else
				showNoticeDialog();
		} else {
			// Toast.makeText(mContext, R.string.str_soft_update_no,
			// Toast.LENGTH_LONG).show();
		}
	}

	/***
	 * 弹出提示框
	 */
	private void showAlertDialog(String title, String msg, AlertStyle style, AlertView.OnAlertViewListener listener) {
		AlertView dialog = new AlertView(mContext, title, msg, listener, style);
		dialog.show();
	}

	/**
	 * 检查软件是否有更新版本
	 * 
	 * @return
	 */
	public boolean isUpdate(int serviceCode) {
		int versionCode = getVersionCode(mContext);
		if (serviceCode > versionCode)
			return true;
		return false;
	}

	/**
	 * 获取软件版本号
	 * 
	 * @param context
	 * @return
	 */
	public int getVersionCode(Context context) {
		if (null == context)
			return 0;
		int versionCode = 0;
		try {
			// 获取软件版本号，对应AndroidManifest.xml下android:versionCode
			versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	public String getVersionName(Context context) {
		if (null == context)
			return "";
		String versionName = "";
		try {
			// 获取软件版本号，对应AndroidManifest.xml下android:versionCode
			versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}

	/**
	 * 显示软件更新对话框
	 */
	private void showNoticeDialog() {
		AlertView.OnAlertViewListener listener = new OnAlertViewListener() {

			@Override
			public void back(boolean result) {
				// TODO Auto-generated method stub
				if (result) {
					// 显示下载对话框
					showDownloadDialog();
				}
			}
		};
		
		String msg = String.valueOf(mHashMap.get("msg")) + "\n" + mContext.getResources().getString(R.string.str_soft_update_info);
		showAlertDialog(mContext.getResources().getString(R.string.str_soft_update_title),
				msg, AlertView.AlertStyle.OK_CANCEL,
				listener);
	}

	/**
	 * 显示软件下载对话框
	 */
	private void showDownloadDialog() {
		if (null == mHashMap)
			return;
		File apkfile = new File(mSavePath, mHashMap.get("name"));
		if (apkfile.exists()) {
			OnAlertViewListener listener = new OnAlertViewListener() {
				@Override
				public void back(boolean result) {
					// TODO Auto-generated method stub
					if (result) {
						// 显示下载对话框
						downloadApk();
					} else {
						installApk();
					}
				}
			};
			showAlertDialog(mContext.getResources().getString(R.string.str_soft_installapp),
					mContext.getResources().getString(R.string.str_soft_installapp_redownload), AlertStyle.OK_CANCEL,
					listener);
		} else {
			downloadApk();
		}
	}

	/**
	 * 下载apk文件
	 */
	private void downloadApk() {
		mDownloadDialog = new ProgressDialog(mContext, mContext.getResources().getString(R.string.str_soft_updating),
				new OnCancleListener() {
					@Override
					public void back(boolean result) {
						cancelUpdate = true;
					}
				});
		mDownloadDialog.setCanceledOnTouchOutside(false);
		mDownloadDialog.setCancelable(false);
		mDownloadDialog.show();
		// 启动新线程下载软件
		new downloadApkThread().start();
	}

	/***
	 * 
	 * @author ZiZhengzhuan
	 *
	 */
	private class downloadApkThread extends Thread {
		@Override
		public void run() {
			try {
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && mSavePath != null
						&& !mSavePath.isEmpty()) {
					URL url = new URL(mHashMap.get("url"));
					// 创建连接
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(15 * 1000);
					conn.connect();
					if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {

						return;
					}
					// 获取文件大小
					int length = conn.getContentLength();
					// 创建输入流
					InputStream is = conn.getInputStream();

					File file = new File(mSavePath);
					// 判断文件目录是否存在
					if (!file.exists()) {
						file.mkdir();
					} else {
						for (File tmpFile : file.listFiles()) {
							if (tmpFile.isFile())
								tmpFile.delete();
						}
					}
					File apkFile = new File(mSavePath, mHashMap.get("name"));
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do {
						int numread = is.read(buf);
						count += numread;
						// 计算进度条位置
						progress = (int) (((float) count / length) * 100);
						// 更新进度
						mHandler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0) {
							// 下载完成
							mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// 取消下载对话框显示
			mDownloadDialog.dismiss();
		}
	};

	/**
	 * 安装APK文件
	 */
	private void installApk() {
		File apkfile = new File(mSavePath, mHashMap.get("name"));
		if (!apkfile.exists()) {
			return;
		}
		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
		mContext.startActivity(i);
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
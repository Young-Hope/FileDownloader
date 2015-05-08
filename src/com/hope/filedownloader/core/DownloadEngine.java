package com.hope.filedownloader.core;

import android.util.Log;

public class DownloadEngine implements Runnable{

	private String mUrl;
	private String mPath;
	private FileDownloaderConfiguration mConfiguration;
	private FileDownloadListener mListener;
	
	private FileDownloadExecutor mExecutor;
	public DownloadEngine(String url,
			String path,
			FileDownloaderConfiguration configuration,
			FileDownloadListener listener) {
		mUrl = url;
		mPath = path;
		mConfiguration = configuration;
		mListener = listener;
	}

	@Override
	public void run() {
		mExecutor = new FileDownloadExecutor(mUrl, mPath, mConfiguration, mListener);
		// start downloading.
		try {
			Log.d("debug", "start download");
			mExecutor.download();
		} catch (Exception e) {
			if (mListener != null) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						mListener.onDownloadFail();
					}
				};
				mConfiguration.getHandler().post(r);
			}
			e.printStackTrace();
		}
		
	}
	
	public void exit(){
		if (mExecutor != null) {
			mExecutor.exit();
		}
	}

}

package com.hope.filedownloader.core;


public class FileDownloader {
	
	private static final String ERROR_INIT_CONFIG_WITH_NULL = "FileDownloader configuration can not be initialized with null";
	
	private FileDownloaderConfiguration mConfiguration;
	private DownloadEngine mEngine;
	
	/**
	 * Initializes fileDownloader instance with configuration.
	 */
	public FileDownloader(FileDownloaderConfiguration configuration) {
		if (configuration == null) {
			throw new IllegalArgumentException(ERROR_INIT_CONFIG_WITH_NULL);
		}
		if (this.mConfiguration == null) {
			this.mConfiguration = configuration;
		}
	}
	
	public void downloadFile(String url, String path) {
		downloadFile(url, path, null);
	}

	public void downloadFile(String url, String path, FileDownloadListener listener) {
		mEngine = new DownloadEngine(url, path, mConfiguration, listener);
		
		// create a new thread to download.
		new Thread(mEngine).start();
	}
	
	public void exit(){
		if (mEngine != null) {
			mEngine.exit();
		}
	}
}

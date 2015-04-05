package com.hope.filedownloader.core;

public interface FileDownloadListener {

	/**
	 * Is called when download task starts.
	 * @param total total size in bytes.
	 */
	void onDownloadStart(long total);
	/**
	 * Is called when file loading progress changed.
	 * @param current downloaded size in bytes.
	 * @param total total size in bytes.
	 */
	void onProgressUpdate(long current, long total);
	
	/**
	 * Is called when file is downloaded successfully.
	 */
	void onDownloadComplete();
	
	/**
	 * Is called when download fails.
	 */
	void onDownloadFail();
}

package com.hope.filedownloader.core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import android.util.Log;

public class FileDownloadExecutor {

	private static final String SERVER_RESPOND_FAILURE = "server respond error";
	private static final String UNKNOWN_FILE_SIZE = "unknown file size";
	private static final String UNKNOWN_DIR_PATH = "unknown directory path";
	private static final int TIMEOUT = 5 * 1000;
	/*Url(String) for the file*/
	private String mUrlStr;
	/*directory for the file*/
	private String mDirPath;
	private FileDownloaderConfiguration mConfiguration;
	/*FileDownloadListener*/
	private FileDownloadListener mListener;
	
	/*Stop download*/
	private boolean mExit = false;
	/*Size in bytes that has been downloaded*/
	private long mDownloadedSize = 0;
	/*Size of the file in bytes*/
	private long mFileSize = 0;
	/*Size in bytes that each thread should download*/
	private long mBlockSize;
	/*Save size in bytes that each thread has downloaded*/
	Map<Integer, Long> mCache = new ConcurrentHashMap<Integer, Long>();
	/*File to save the content*/
	private File mSaveFile;
	/*Task to download the file*/
	private DownloadTask[] mTasks;
	/*operate database*/
	private FileService mFileService;
	
	public FileDownloadExecutor(String urlStr,String path,
			FileDownloaderConfiguration configuration,
			FileDownloadListener listener) {
		mUrlStr = urlStr;
		mDirPath = path;
		mConfiguration = configuration;
		mListener = listener;
		
		mTasks = new DownloadTask[mConfiguration.getmThreadPoolsize()];
		mFileService = new FileService(mConfiguration.getmContext());
		
		HttpURLConnection conn = null;
		try {
			URL url = new URL(mUrlStr);
			conn = (HttpURLConnection) url.openConnection();
			// configure connection
			conn.setConnectTimeout(TIMEOUT);
			conn.setRequestMethod("GET");
			conn.connect();
			if (conn.getResponseCode() == 200) {
				this.mFileSize = conn.getContentLength();
				if (this.mFileSize <= 0) {
					throw new RuntimeException(UNKNOWN_FILE_SIZE);
				}
				
				createFile();
				
				Map<Integer, Long> log = mFileService.getData(mUrlStr);
				// if db has download record
				if (log.size() > 0) {
					for (Map.Entry<Integer, Long> entry : log.entrySet()) {
						mCache.put(entry.getKey(), entry.getValue());
					}
				}
				int threadPoolSize = mConfiguration.getmThreadPoolsize();
				if (mCache.size() == threadPoolSize) {
					for (int i = 0; i < threadPoolSize; i++) {
						mDownloadedSize += mCache.get(i);
					}
				}
				// calculate block size.
				mBlockSize = mFileSize % threadPoolSize == 0
						? mFileSize / threadPoolSize
						: mFileSize / threadPoolSize + 1;
				
			} else {
				if (mListener != null) {
					Runnable r = new Runnable() {
						@Override
						public void run() {
							mListener.onDownloadFail();
						}
					};
					mConfiguration.getHandler().post(r);
				}
				throw new RuntimeException(SERVER_RESPOND_FAILURE);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
	
	public void download() throws Exception {
		if (!mSaveFile.exists()) {
			RandomAccessFile raf = new RandomAccessFile(mSaveFile, "rw");
			raf.setLength(mFileSize);
			raf.close();
		}
		
		int threadPoolSize = mConfiguration.getmThreadPoolsize();
		if (mCache.size() != threadPoolSize) {
			mCache.clear();
			mDownloadedSize = 0;
			for (int i = 0; i < threadPoolSize; i++) {
				mCache.put(i, (long) 0);
			}
		}
		
		if (mListener != null) {
			Runnable start = new Runnable() {
				
				@Override
				public void run() {
					mListener.onDownloadStart(mFileSize);
				}
			};
			mConfiguration.getHandler().post(start);
		}
		
		Executor executor = mConfiguration.getmExecutor();
		for (int i = 0; i < threadPoolSize; i++) {
			Long downloadLength = mCache.get(i); 
			if (downloadLength < mBlockSize && downloadLength < mFileSize) {
				mTasks[i] = new DownloadTask(this, mUrlStr, i, mSaveFile, mBlockSize, mCache.get(i));  
				executor.execute(mTasks[i]);
			} else {
				mTasks[i] = null;
			}
		}
		
		mFileService.deleteData(mUrlStr);
		mFileService.insertData(mUrlStr, mCache);
		
		boolean finished = false;
		while (!finished) {
			Thread.sleep(700);
			// suppose all task has finished the work.
			finished = true;
			for (int i = 0; i < threadPoolSize; i++) {
				if (mTasks[i] != null && !mTasks[i].isFinish()) {
					finished = false;
					if (mTasks[i].getDownLoadLength() == -1) {
						Log.d("debug", "Thread:" + i + " restart");
						mTasks[i].setFinish(true);
						mTasks[i] = null;
						mTasks[i]= new DownloadTask(this, mUrlStr, i, mSaveFile, mBlockSize, mCache.get(i));
						executor.execute(mTasks[i]);
					}
				}
				
			}
			if (mListener != null) {
				Runnable update = new Runnable() {
					
					@Override
					public void run() {
						mListener.onProgressUpdate(mDownloadedSize, mFileSize);
					}
				};
				mConfiguration.getHandler().post(update);
			}
		}
		
		// download is finish.
		if (mDownloadedSize >= mFileSize) {
			mFileService.deleteData(mUrlStr);
		}
		
		if (mListener != null) {
			Runnable complete = new Runnable() {
				
				@Override
				public void run() {
					mListener.onDownloadComplete();
				}
			};
			mConfiguration.getHandler().post(complete);
		}
	}
	
	protected synchronized void update(int threadId, long length){
		mCache.put(threadId, length);
		mFileService.updateData(mUrlStr, threadId, length);
	}
	
	protected synchronized void append(long length) {
		mDownloadedSize += length;
	}
	
	public void exit(){
		this.mExit = true;
		Log.d("debug", "exit");
	}
	
	public boolean isExit() {
		return mExit;
	}
	/**
	 * create a file to save the target file.
	 */
	private void createFile() {
		String fileName = getFileName();
		File dir = new File(mDirPath);
		if (dir != null && !dir.isDirectory()) {
			throw new RuntimeException(UNKNOWN_DIR_PATH);
		}
		if (!dir.exists()) {
			dir.mkdirs();
		}
		mSaveFile = new File(dir, fileName);
	}
	
	private String getFileName() {
		String fileName = mUrlStr.substring(mUrlStr.lastIndexOf("/") + 1);
		if (fileName == null || "".equals(fileName.trim())) {
			fileName = UUID.randomUUID() + ".tmp";
		}
		return fileName;
	}
}

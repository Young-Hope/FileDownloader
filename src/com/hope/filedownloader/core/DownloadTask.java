package com.hope.filedownloader.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;


public class DownloadTask implements Runnable{

	private static final int TIMEOUT = 5 * 1000;
	
	private int mThreadId;
	private long mBlockSize;
	private long mDownloadLength = 0;
	private boolean mFinish = false;
	private String mUrl;
	
	private File mFile;
	private FileDownloadExecutor mExecutor;
	
	public DownloadTask(FileDownloadExecutor executor, String url,
			int threadId, File file, long blockSize) {
		mExecutor = executor;
		mUrl = url;
		mThreadId = threadId;
		mFile = file;
		mBlockSize = blockSize;
	}
	
	@Override
	public void run() {
		Log.d("debug", "thread:" + mThreadId);
		if (mDownloadLength >= mBlockSize) {
			return;
		}
		
		URL url = null;
		HttpURLConnection conn = null;
		RandomAccessFile raf = null;
		InputStream in = null;
		try {
			url = new URL(mUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(TIMEOUT);
			conn.setRequestMethod("GET");
			
			long startPos = mBlockSize * mThreadId + mDownloadLength;
			long endPos = mBlockSize * (mThreadId+ 1) - 1;
			Log.d("debug", "Thread:" + mThreadId + ". Range: " + startPos + "~~" + endPos);
			// get the specific stream.
			conn.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
			in = conn.getInputStream();
			byte bytes[] = new byte[1024];
			int offset = 0;
			raf = new RandomAccessFile(mFile, "rwd");
			raf.seek(startPos);
			while (!mExecutor.isExit()
					&& (offset = in.read(bytes, 0, 1024)) != -1) {
				raf.write(bytes, 0, offset);
				mDownloadLength += offset;
				mExecutor.update(mThreadId, mDownloadLength);
				mExecutor.append(offset);
			}
			setFinish(true);
		} catch (Exception e) {
			mDownloadLength = -1;
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void setFinish(boolean finish) {
		mFinish = finish;
	}
	
	public boolean isFinish() {
		return mFinish;
	}
	
	public long getDownLoadLength() {
		return mDownloadLength;
	}

}

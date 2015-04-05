package com.hope.filedownloader.core;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Handler;

/**
 * Presents configuration for FileDownloader
 * @author hope
 *
 */
public final class FileDownloaderConfiguration {

	private static final String ERROR_EXECUTOR_WITH_NULL = "executor should not be null, you should call this method after init()";
	private static final String ERROR_CONTEXT_WITH_NULL = "context should not be null, you should call this method after init()";
	
	private Context mContext;
	private Executor mExecutor;
	private int mThreadPoolsize;
	private Handler mHandler;
	
	private FileDownloaderConfiguration(Builder builder) {
		mContext = builder.context;
		mExecutor = builder.executor;
		mThreadPoolsize = builder.threadPoolSize;
	}
	
	
	public Context getmContext() {
		if (mContext == null) {
			throw new IllegalArgumentException(ERROR_CONTEXT_WITH_NULL);
		}
		return mContext;
	}

	public Handler getHandler() {
		if (mContext == null) {
			throw new IllegalArgumentException(ERROR_CONTEXT_WITH_NULL);
		}
		if (mHandler == null) {
			mHandler = new Handler(mContext.getMainLooper());
		}
		return mHandler;
	}
	
	public Executor getmExecutor() {
		if (mExecutor == null) {
			throw new IllegalArgumentException(ERROR_EXECUTOR_WITH_NULL);
		}
		return mExecutor;
	}


	public int getmThreadPoolsize() {
		return mThreadPoolsize;
	}

	public static class Builder {
		private static  final int DEFAULT_THREAD_POOL_SIZE = 3; 
		
		private Context context;
		private Executor executor = null;
		
		private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
		
		public Builder(Context context) {
			this.context = context;
		}
		
		/**
		 * Sets custom executor for file download task.
		 */
		public Builder customExecutor(Executor executor) {
			this.executor = executor;
			return this;
		}
		
		/**
		 * Sets thread pool size for file download task.
		 * Default value - {@link #DEFAULT_THREAD_POOL_SIZE this}
		 */
		public Builder SetThreadPoolSize(int size) {
			this.threadPoolSize = size;
			return this;
		}
		
		
		public FileDownloaderConfiguration build(){
			initEmptyFieldsWithDefaultValues();
			return new FileDownloaderConfiguration(this);
		}

		private void initEmptyFieldsWithDefaultValues() {
			if (executor == null) {
				executor = Executors.newFixedThreadPool(threadPoolSize);
			}
		}
	}
}

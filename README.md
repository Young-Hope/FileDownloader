# FileDownloader

##1.功能介绍

###1.1概述
多线程断点续传框架

###1.2基本使用

####1.2.1 构造`FileDownloader`
在构造`FileDownloader`时得先初始化`FileDownloaderConfiguration`，比如:
```
configuration = new FileDownloaderConfiguration.Builder(getApplicationContext())
    .build();
downloader = new FileDownloader(configuration);
```
####1.2.2 下载文件:
```
// url为文件地址，path为保存的目录
downloader.downloadFile(url, path)

// FileDownloadListener为回调函数
downloader.downloadFile(url, path, new FileDownloadListener(){
	@Override
	public void onDownloadStart(long total) {
		Log.d("debug", "onDownloadStart");
		progressBar.setMax(100);
	}

	@Override
	public void onProgressUpdate(long current, long total) {
	}

	@Override
	public void onDownloadComplete() {
	}

	@Override
	public void onDownloadFail() {
	}
});

```


##2已知bug
1. 下载还未完成的时候停止任务会调用`onDownloadComplete`回调函数。
2. 连续两次调用下载任务会出现问题


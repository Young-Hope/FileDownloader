package com.hope.filedownloader.core;

import java.io.File;

import com.hope.filedownloader.R;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

	private EditText editText;
	private Button startDownload;
	private Button stopDownload;
	private ProgressBar progressBar;
	private TextView resultView;
	
	private FileDownloader downloader;
	private FileDownloaderConfiguration configuration;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.edit);
        startDownload = (Button) findViewById(R.id.download);
        stopDownload = (Button) findViewById(R.id.stop);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        resultView = (TextView) findViewById(R.id.result);
        
        configuration = new FileDownloaderConfiguration.Builder(getApplicationContext())
        	.build();
        downloader = new FileDownloader(configuration);
        
        editText.setText("http://192.168.191.1:8080/ServerForPicture/music.mp3");
        
        startDownload.setOnClickListener(new StartListener());
        stopDownload.setOnClickListener(new StopListener());
        
    }

    private class DownloadListener implements FileDownloadListener {

		@Override
		public void onDownloadStart(long total) {
			Log.d("debug", "onDownloadStart");
			progressBar.setMax(100);
		}

		@Override
		public void onProgressUpdate(long current, long total) {
//			Log.d("debug", "onProgressUpdate");
			int progress = (int) ((current * 1.0 * 100) / total);
			progressBar.setProgress(progress);
			resultView.setText(progress+"%");
		}

		@Override
		public void onDownloadComplete() {
			Log.d("debug", "onDownloadComplete");
//			progressBar.setProgress(100);
//			resultView.setText(100+"");
//			Toast.makeText(getApplicationContext(), "download success", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onDownloadFail() {
			Log.d("debug", "onDownloadFail");
		}
    	
    }

    private class StartListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			String url = editText.getText().toString();
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File dir = Environment.getExternalStorageDirectory();
				String path = dir.getPath();
				downloader.downloadFile(url, path, new DownloadListener());
			}
		}
    	
    }
    
    private class StopListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			downloader.exit();
		}
    	
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
}

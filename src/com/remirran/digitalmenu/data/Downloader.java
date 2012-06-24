package com.remirran.digitalmenu.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import android.os.AsyncTask;
import android.util.Log;

public class Downloader extends AsyncTask<String, Void, FileCache>{
	private static final String LOG_TAG="Downloader";
	private DlCallbacks listener;
	private String uri;

	public Downloader(DlCallbacks listener) {
		this.listener = listener;
	}

	@Override
	protected FileCache doInBackground(String... params) {
		try {
			uri = params[0];
			FileCache cache = new FileCache(uri, params.length > 1);
			return cache;
		} catch (FileNotFoundException e) {
			cancel(true);
			Log.w(LOG_TAG, "File not found: ", e);
		} catch (IOException e) {
			cancel(true);
			Log.w(LOG_TAG, "IO error: ", e);
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(FileCache result) {
		super.onPostExecute(result);
		/*TODO: check file status, not call this line in case of download fails*/
		listener.onFileReceived(result);
	}	
}

package com.remirran.digitalmenu.data;

import com.remirran.digitalmenu.data.FileCache.CacheEntry;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class Downloader extends AsyncTask<CacheEntry, Void, Bitmap>{
	private static final String LOG_TAG="Downloader";
	private DlCallbacks listener;
	private CacheEntry task;

	public Downloader(DlCallbacks listener) {
		this.listener = listener;
	}
	
	public void Setup(CacheEntry task) {
		if (task.isUriValid() && (!task.exists() || task.hasImageView())) 
			execute(task);
	}

	@Override
	protected Bitmap doInBackground(CacheEntry... params) {
		task = params[0];
		
		task.process();
		
		if (task.isParceable() ) {
			listener.cleanOldValues();
			listener.doParseXML(task);
		} else {
			if ( task.exists()) {
				if ( task.isResizeable() ) {
					return task.getResizedBitmap();
				} else {
					return task.getBitmap();
				}
			}
		}

		return null;
	}
	
	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		if ( result == null && task.isParceable() && task.isParsed() ) {
			listener.onXMLParsed();
		}
		if (result != null) {
			ImageView iv = task.getImageView();
			if (iv != null) {
				iv.setImageBitmap(result);
				task.clearImageView();
			}
		}
	}	
}

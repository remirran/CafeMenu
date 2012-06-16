package com.remirran.cafemenu.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.remirran.cafemenu.R;

public class Downloader extends AsyncTask<String, Void, Void>{
	private static final String LOG_TAG="Downloader";
	private DlCallbacks listener;

	public Downloader(DlCallbacks listener) {
		this.listener = listener;
	}
	
	private void getAllData(){
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void...params) {
				/*Read the Master XML*/
				try {
					FileCache cache = new FileCache(cacheDir, XML_URI, false);
					parseXML(cache.getInputStream());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				/*Update the master XML*/
				queue.add(XML_URI);
				fetch();
				return null;
			};
			protected void onPostExecute(Void result) {
				
			};
		}.execute(new Void[]{});
	}
	
	private boolean fetch() {
		try {
			String uri;
			FileCache cache;
			while (queue.size() > 0) {
				uri = queue.remove();
				cache = new FileCache(cacheDir, uri, uri.equals(XML_URI));
				if (cache.isXml()) {
					parseXML(cache.getInputStream());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	protected Void doInBackground(String... params) {
		// TODO Auto-generated method stub
		return null;
	}
}

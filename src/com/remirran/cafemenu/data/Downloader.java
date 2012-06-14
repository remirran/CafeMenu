package com.remirran.cafemenu.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.os.AsyncTask;

import com.remirran.cafemenu.R;

public class Downloader {
	private static String XML_URI;
	private static File cacheDir;
	private static final Vector<String> queue = new Vector<String>();

	public Downloader(Context context) {
		XML_URI = context.getString(R.string.xml_uri);
		cacheDir = context.getCacheDir();
		
		getAllData();
	}
	
	private void getAllData(){
		new AsyncTask<Void, Void, Boolean>() {
			protected Boolean doInBackground(Void...params) {
				boolean retval = false;
				retval = fetch();
				return retval;
			};
			protected void onPostExecute(Boolean result) {
				
			};
		}.execute(new Void[]{});
	}
	
	private boolean fetch() {
		File xmlCmdFileCache = new File(cacheDir, FileCache.md5(XML_URI) + ".cache");
		try {
			FileCache.fileSave(new URL(XML_URI).openStream(), new FileOutputStream(xmlCmdFileCache));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean parseXML() throws XmlPullParserException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser parser = factory.newPullParser();
		return false;
	}
}

package com.remirran.cafemenu.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.os.AsyncTask;
import com.remirran.cafemenu.R;

public class Downloader {
	private static String XML_URI;
	private static File cacheDir;
	private static final ArrayDeque<String> queue = new ArrayDeque<String>();

	public Downloader(Context context) {
		XML_URI = context.getString(R.string.xml_uri);
		cacheDir = context.getCacheDir();
		queue.add(XML_URI);
		getAllData();
	}
	
	private void getAllData(){
		new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void...params) {
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
	
	private void parseXML(InputStream is) throws XmlPullParserException, IOException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser parser = factory.newPullParser();
		
		ExtData data = new ExtData();
		String currentTag = "";
		String newUri;
		
		parser.setInput(is, null);
		synchronized (ExtData.lockObject) {
			while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				switch(parser.getEventType()) {
				case XmlPullParser.START_TAG:
					currentTag = parser.getName();
					if (currentTag.toLowerCase().equals("adv")) {
						data.setState(ExtData.STATE_ADV);
					} else if (currentTag.toLowerCase().equals("dategeneration")) {
						data.setState(ExtData.STATE_UPDATE);
					}
					break;
				case XmlPullParser.END_TAG:
					data.commit();
					break;
				case XmlPullParser.TEXT:
					newUri = data.setPair(currentTag, parser.getText());
					if (newUri != null) {
						queue.add(newUri);
					}
					break;
				default:
					break;
				}
				parser.next();
			}
		}
	}
}

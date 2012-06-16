package com.remirran.cafemenu.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.remirran.cafemenu.R;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

public class ExtData implements DlCallbacks {
	public static final int STATE_NONE		= 0;
	public static final int STATE_UPDATE	= 1;
	public static final int STATE_ADV		= 2;
	public static final int STATE_SECTION	= 3;
	public static final int STATE_DISH		= 4;
	public static final int STATE_COMMAND	= 5;
	public static final int STATE_LAST		= 5;
	
	private static String XML_URI;
	private static File cacheDir;
	private static final String LOG_TAG = "ExtData";
	
	private static String lastUpdate;
	private static String advUri;
	private static final Vector<Section> sections = new Vector<Section>();
	private static final HashMap<String, Vector<Section>> subs = new HashMap<String, Vector<Section>>();
	private static final HashMap<String, Vector<Dish>> dishes = new HashMap<String, Vector<Dish>>();
	
	private static final HashMap<Integer, ImageView> delayed = new HashMap<Integer, ImageView>();
	
	private int state;
	
	public ExtData(Context context) {
		XML_URI = context.getString(R.string.xml_uri);
		cacheDir = context.getCacheDir();
		state = STATE_NONE;
		lastUpdate = "";
		advUri = "";
	}
	
	public void setState(int stateNew) {
		state = stateNew;
		if (state < STATE_NONE || state > STATE_LAST) {
			throw new IndexOutOfBoundsException("State is out of bounds");
		}
	}
	
	/* Return URI if it is passed as value, otherwise null */
	public String setPair(String key, String value) throws IndexOutOfBoundsException {
		switch (state) {
		case STATE_NONE: 
			break;
		case STATE_ADV:
			synchronized (delayed) {
				advUri = value;
				if (delayed.containsKey(state)) {
					try {
						FileCache.fillImageFromCache(advUri, delayed.get(state));
						delayed.remove(state);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return value;
		case STATE_UPDATE:
			break;
		default: 
			throw new IndexOutOfBoundsException("State is out of bounds");
		}
		return null;
	}
	
	public void commit() {
		state = STATE_NONE;
	}
	
	public static void fillAdv(ImageView iv) {
		synchronized (delayed) {		
			try {
				FileCache.fillImageFromCache(advUri, iv);
			} catch (FileNotFoundException e) {
				delayed.put(STATE_ADV, iv);
			}

		}

	}
	
	private void parseXML(InputStream is) throws XmlPullParserException, IOException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser parser = factory.newPullParser();
		
		String currentTag = "";
		String newUri;
		
		parser.setInput(is, null);
		while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
			switch(parser.getEventType()) {
			case XmlPullParser.START_TAG:
				currentTag = parser.getName();
				if (currentTag.toLowerCase().equals("adv")) {
					setState(STATE_ADV);
					for (int i = 0; i < parser.getAttributeCount(); i++) {
						Log.d(LOG_TAG, parser.getAttributeName(i) + " " + parser.getAttributeValue(i) );
						if (parser.getAttributeName(i).toLowerCase().equals("url")) {
							newUri = setPair(currentTag, parser.getAttributeValue(i));
							if (newUri != null) {
								queue.add(newUri);
							}
						}
					}
				} else if (currentTag.toLowerCase().equals("dategeneration")) {
					setState(STATE_UPDATE);
				}
				break;
			case XmlPullParser.END_TAG:
				commit();
				break;
			case XmlPullParser.TEXT:
				newUri = setPair(currentTag, parser.getText());
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

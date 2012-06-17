package com.remirran.cafemenu.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.remirran.cafemenu.R;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class ExtData implements DlCallbacks {
	/* Constants */
	private static final int STATE_NONE		= 0;
	private static final int STATE_UPDATE	= 1;
	private static final int STATE_ADV		= 2;
	private static final int STATE_SECTION	= 3;
	private static final int STATE_DISH		= 4;
	private static final int STATE_COMMAND	= 5;
	private static final int STATE_LAST		= 5;
	
	public static String XML_URI;
	public static File CACHE_DIR;
	
	private static final String LOG_TAG = "ExtData";
	
	/* Members */
	private static String lastUpdate;
	private static String advUri;
	private static final Vector<Section> sections = new Vector<Section>();
	private static final HashMap<String, Vector<Section>> subs = new HashMap<String, Vector<Section>>();
	private static final HashMap<String, Vector<Dish>> dishes = new HashMap<String, Vector<Dish>>();
	
	private Section currentObj;
	
	private static class ResourseRequest {
		private WeakReference<ImageView> iv;
		private Handler hl;
		public ResourseRequest(Handler hl, ImageView iv) {
			this.hl = hl;
			this.iv = new WeakReference<ImageView>(iv);
		}
		public Handler getHl() {
			return hl;
		}
		public ImageView getIv() {
			return iv.get();
		}
	}
	private static final HashMap<String, ResourseRequest> delayed = new HashMap<String, ResourseRequest>();
	
	private int state;
	
	/* Methods */
	public ExtData(Context context) {
		XML_URI = context.getString(R.string.xml_uri);
		CACHE_DIR = context.getCacheDir();
		state = STATE_NONE;
		/* Spawn a new task to update the structures */
		new Downloader(this).execute(new String[] {XML_URI});
	}
	
	public void refresh () {
		new Downloader(this).execute(new String[] {XML_URI, "true"});
	}
	
	public void setState(int stateNew) throws IndexOutOfBoundsException {
		state = stateNew;
		if (state < STATE_NONE || state > STATE_LAST) {
			throw new IndexOutOfBoundsException("State is out of bounds");
		}
		switch (state) {
		case STATE_SECTION:
			currentObj = new Section();
			break;
		default:
			break;
		}
	}
	
	/* Return URI if it is passed as value, otherwise null */
	public String setPair(String key, String value) throws IndexOutOfBoundsException {
		switch (state) {
		case STATE_NONE: 
			break;
		case STATE_ADV:
			advUri = value;
			return value;
		case STATE_UPDATE:
			lastUpdate = value;
			break;
		case STATE_COMMAND:
			break;
		case STATE_SECTION:
			if (key.equals("id")) {
				currentObj.setId(value);
			} else if (key.equals("parent")) {
				currentObj.setCategoryId(value);
			} else if (key.equals("category")) {
				currentObj.setName(value);
			}
			break;
		case STATE_DISH:
			break;
		default: 
			throw new IndexOutOfBoundsException("State is out of bounds");
		}
		return null;
	}
	
	public void commit() {
		switch (state) {
		case STATE_SECTION:
			if (currentObj.isRootElement()) {
				synchronized (sections) {
					sections.add(currentObj);
				}
			} else {
				synchronized (subs) {
					try {
					Section root = getRootSectionbyId(currentObj.getCategoryId());
					if (!subs.containsKey(root.getName())) {
						subs.put(root.getName(), new Vector<Section>());
					}
					/* TODO: check duplicates */
					subs.get(root.getName()).add(currentObj);
					} catch (NullPointerException e) {
						Log.w(LOG_TAG, "Can't add category: " + currentObj.getId(), e);
					}
				}
			}
		case STATE_ADV:
			state = STATE_NONE;
			break;
		default:
			break;
		}
	}
	
	private Section getRootSectionbyId(String id) {
		Iterator<Section> itr = sections.iterator();
		while (itr.hasNext()) {
			Section elem = itr.next();
			if (elem.getId().equals(id)) {
				return elem;
			}
		}
		return null;
		
	}
	
	public static void fillAdv(Handler hl, ImageView iv) {
		synchronized (delayed) {		
			try {
				/*TODO: probably this advUri is not thread-safe */
				FileCache.fillImageFromCache(advUri, iv);
			} catch (FileNotFoundException e) {
				delayed.put("adv", new ResourseRequest(hl,iv));
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
						if (parser.getAttributeName(i).toLowerCase().equals("url")) {
							newUri = setPair(currentTag, parser.getAttributeValue(i));
							if (newUri != null) {
									new Downloader(this).execute(new String[] {newUri});
								}
						}
					}
				} else if (currentTag.toLowerCase().equals("dategeneration")) {
					setState(STATE_UPDATE);
				} else if (currentTag.toLowerCase().equals("category")) {
					setState(STATE_SECTION);
					for (int i = 0; i < parser.getAttributeCount(); i++) {
						setPair(parser.getAttributeName(i).toLowerCase(), parser.getAttributeValue(i));
					}
				} else if (currentTag.toLowerCase().equals("dish")) {
					
				}
				break;
			case XmlPullParser.TEXT:
				newUri = setPair(currentTag, parser.getText());
				if (newUri != null) {
					new Downloader(this).execute(new String[] {newUri});
				}
				commit();
				break;
			default:
				break;
			}
			parser.next();
		}	
	}
	
	private static void checkDelayed(FileCache cache) {
		synchronized (delayed) {
			String uri = cache.getUri().equals(advUri)?"adv":cache.getUri();
			if (delayed.containsKey(uri)) {
				ResourseRequest rr = delayed.get(uri);
				rr.getHl().post(new PicLoader(cache, rr.getIv()));
				delayed.remove(cache.getUri());
			}
		}
	}
	
	static public class PicLoader implements Runnable{
		private FileCache cache;
		private ImageView iv;
		public PicLoader(FileCache cache, ImageView iv) {
			this.cache = cache;
			this.iv = iv;
		}
		
		@Override
		public void run() {
			try {
			cache.fillImageFromCache(iv);
			} catch (NullPointerException e) {
				Log.w(LOG_TAG, "ImageView already deleted by GC", e);
			}
		}
		
	}

	@Override
	public void onFileReceived(FileCache cache) {
		if (cache.isXml()) {
			try {
				parseXML(cache.getInputStream());
			} catch (FileNotFoundException e) {
				Log.w(LOG_TAG, "File not found at: ", e);
			} catch (XmlPullParserException e) {
				Log.w(LOG_TAG, "XML can't be parsed: ", e);
			} catch (IOException e) {
				Log.w(LOG_TAG, "IO Error", e);
			}
		} else {
			checkDelayed(cache);
		}
		
	}
}

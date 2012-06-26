package com.remirran.digitalmenu.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.remirran.digitalmenu.CafeMenuActivity;
import com.remirran.digitalmenu.R;

import android.content.Context;
import android.os.AsyncTask;
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
	public static float DENSITY;
	private static Context context;
	
	private static final String LOG_TAG = "ExtData";
	
	/* Members */
	/*TODO check this flag before XML re-parsing*/
	private static String lastUpdate;
	private static String advUri;
	private static final Vector<Section> sections = new Vector<Section>();
	private static final HashMap<Section, Vector<Section>> subs = new HashMap<Section, Vector<Section>>();
	private static final HashMap<Section, Vector<Dish>> dishes = new HashMap<Section, Vector<Dish>>();
	private static final Vector<Section> flatSubs = new Vector<Section>();
	
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
	
	private static class Parser extends AsyncTask<FileCache, Void, Void> {
		private DlCallbacks listener;
		public Parser(DlCallbacks listener) {
			this.listener = listener;
		}
		
		@Override
		protected Void doInBackground(FileCache... params) {
			listener.cleanOldValues();
			listener.doParseXML(params[0]);
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			listener.onXMLParsed();
		}
	}
	
	/* Methods */
	public ExtData(Context context) {
		XML_URI = context.getString(R.string.xml_uri);
		CACHE_DIR = context.getCacheDir();
		DENSITY = context.getResources().getDisplayMetrics().density;
		ExtData.context = context;
		state = STATE_NONE;
		/* Spawn a new task to update the structures */
		/*TODO: remove "true" to disable cache*/
		new Downloader(this).execute(new String[] {XML_URI, "true"});
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
		case STATE_DISH:
			currentObj = new Dish();
			break;
		default:
			break;
		}
	}
	
	public void setPair(String key, String value) throws IndexOutOfBoundsException {
		if (value.isEmpty() || value.trim().isEmpty()) return;
		switch (state) {
		case STATE_NONE: 
			break;
		case STATE_ADV:
			if (value.startsWith("http://")) {
				advUri = value;
				new Downloader(this).execute(new String[] {advUri});
			}
			break;
		case STATE_UPDATE:
			try {
				int newTime = Integer.parseInt(value);
				int oldTime = Integer.parseInt(lastUpdate);
				if ( oldTime >= newTime) {
					/*TODO: stop parsing here*/
				}
				lastUpdate = value;
			} catch (NumberFormatException e) {
				Log.d(LOG_TAG, "Date reading error, but this is ok ;-)");
				lastUpdate = value;
			}
			break;
		case STATE_COMMAND:
			break;
		case STATE_SECTION:
			if (key.equals("id")) {
				currentObj.setId(value);
			} else if (key.equals("parent")) {
				currentObj.setCategoryId(value);
			} else if (key.equals("img") && value.trim().startsWith("http://")) {
				currentObj.setImgUri(value.trim());
				new Downloader(this).execute(new String[] {currentObj.getImgUri()});
			} else if (key.equals("category")) {
				currentObj.setName(value);
			}
			break;
		case STATE_DISH:
			if (key.equals("id")) {
				currentObj.setId(value);
			} else if (key.equals("available")) {
				((Dish)currentObj).setAvailFlag(value.toLowerCase().equals("true"));
			} else if (key.equals("categoryId")) {
				currentObj.setCategoryId(value);
			} else if (key.equals("price")) {
				((Dish)currentObj).setPrice(Integer.parseInt(value));
			} else if (key.equals("name")) {
				currentObj.setName(value);
			} else if (key.equals("picture") && value.trim().startsWith("http://") ) {
				currentObj.setImgUri(value.trim());
				new Downloader(this).execute(new String[] {currentObj.getImgUri()});
			}
			break;
		default: 
			throw new IndexOutOfBoundsException("State is out of bounds");
		}
	}
	
	public void commit() {
		switch (state) {
		case STATE_SECTION:
			if (currentObj.isRootElement()) {
				synchronized (sections) {
					sections.add(currentObj);
					flatSubs.add(currentObj);
				}
			} else {
				synchronized (subs) {
					try {
						Section root = getSectionbyId(currentObj.getCategoryId());
						if (!subs.containsKey(root)) {
							subs.put(root, new Vector<Section>());
						}
						/* TODO: check duplicates */
						subs.get(root).add(currentObj);
						flatSubs.add(currentObj);
					} catch (NullPointerException e) {
						Log.w(LOG_TAG, "Can't add category: " + currentObj.getId(), e);
					}
				}
			}
			state = STATE_NONE;
			break;
		case STATE_ADV:
			state = STATE_NONE;
			break;
		case STATE_DISH:
			state = STATE_NONE;
			synchronized (dishes) {
				try {
					Section parent = getSectionbyId(currentObj.getCategoryId());
					if (!dishes.containsKey(parent)) {
						dishes.put(parent, new Vector<Dish>());
					}
					dishes.get(parent).add((Dish)currentObj);
				} catch (NullPointerException e) {
					Log.w(LOG_TAG, "Can't add dish: " + currentObj.getId(), e);
				}
			}
			break;
		default:
			state = STATE_NONE;
			break;
		}
	}
	
	private Section getSectionbyId(String id) {
		Iterator<Section> itr = flatSubs.iterator();
		while (itr.hasNext()) {
			Section elem = itr.next();
			if (elem.getId().equals(id)) {
				return elem;
			}
		}
		return null;
		
	}
	/* TODO: combine fillAdv && fillImg */
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
	
	public static void fillImg(String uri, Handler hl, ImageView iv) {
		synchronized (delayed) {		
			try {
				/*TODO: probably this advUri is not thread-safe */
				FileCache.fillImageFromCache(uri, iv);
			} catch (FileNotFoundException e) {
				delayed.put(uri, new ResourseRequest(hl,iv));
			} catch (NullPointerException e) {
				delayed.put(uri, new ResourseRequest(hl,iv));
			}
		}
	}
	
	@Override
	public void doParseXML(FileCache cache) {
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			
			String currentTag = "";
			
			parser.setInput(cache.getInputStream(), null);
			while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				switch(parser.getEventType()) {
				case XmlPullParser.START_TAG:
					currentTag = parser.getName();
					if (currentTag.toLowerCase().equals("adv")) {
						setState(STATE_ADV);
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							if (parser.getAttributeName(i).toLowerCase().equals("url")) {
								setPair(currentTag, parser.getAttributeValue(i));
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
						setState(STATE_DISH);
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							setPair(parser.getAttributeName(i).toLowerCase(), parser.getAttributeValue(i));
						}
					}
					break;
				case XmlPullParser.END_TAG:
					if (state == STATE_DISH && !parser.getName().equals("dish")) break;
					currentTag = "";
					commit();
					break;
				case XmlPullParser.TEXT:
					setPair(currentTag, parser.getText());
					break;
				default:
					break;
				}
				parser.next();
		}
		} catch (XmlPullParserException e){
			Log.w(LOG_TAG, "XML parsing problem", e);			
		} catch (FileNotFoundException e) { 
			Log.w(LOG_TAG, "FileNotFound", e);
		} catch (IOException e) {
			Log.w(LOG_TAG, "IO problem", e);
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
			new Parser(this).execute(new FileCache[] {cache});
			} else {
			checkDelayed(cache);
		}
		
	}

	@Override
	public void onXMLParsed() {
		((CafeMenuActivity) context).applyDownloadedInfo();
	}
	
	public Vector<Section> getSections() {
		return sections;
	}
	
	public Section getSectionByName(String name) throws NoSuchElementException {
		Iterator<Section> itr = flatSubs.iterator();
		synchronized (flatSubs) {
			while (itr.hasNext()) {
				Section tmp = itr.next();
				if (tmp.getName() == name) return tmp;
			}
		}
		throw new NoSuchElementException("Can't find section: " + name);
	}
	
	public Vector<Dish> getDishesBySectionName(String name) {
		return dishes.get(getSectionByName(name));
	}
	
	public Vector<Section> getSubs(String name) {
		return subs.get(getSectionByName(name));
	}

	@Override
	public void cleanOldValues() {
		synchronized (dishes) {
			dishes.clear();
		}
		synchronized (subs) {
			subs.clear();
		}
		synchronized (sections) {
			sections.clear();
		}
		synchronized (flatSubs) {
			flatSubs.clear();
		}
	}
}

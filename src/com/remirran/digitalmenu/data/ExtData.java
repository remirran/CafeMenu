package com.remirran.digitalmenu.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.remirran.digitalmenu.CafeMenuActivity;
import com.remirran.digitalmenu.R;
import com.remirran.digitalmenu.data.FileCache.CacheEntry;

import android.content.Context;
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
	
	private static final String LOG_TAG = "ExtData";
	private static final String ADV_TAG = "ADV";
	
	public static String XML_URI;
	public static File CACHE_DIR;
	public static float DENSITY;
	private static Context context;
	
	/* Members */
	/*TODO check this flag before XML re-parsing*/
	private static String lastUpdate;
	private static String advUri;
	private static final Vector<Section> sections = new Vector<Section>();
	private static final HashMap<Section, Vector<Section>> subs = new HashMap<Section, Vector<Section>>();
	private static final HashMap<Section, Vector<Dish>> dishes = new HashMap<Section, Vector<Dish>>();
	private static final Vector<Section> flatSubs = new Vector<Section>();
	
	private Section currentObj;
	
	private int state;
	
	private static ExtData mInstance = null;
	
	public static ExtData getInstance() {
		if (mInstance == null) {
			mInstance = new ExtData();
		}
		return mInstance;
	}
	
	/* Methods */
	public ExtData() {
		/* TODO: replace restaurant with a configurable option */
		XML_URI = context.getString(R.string.xml_uri) + context.getString(R.string.restaurant) + ".xml";
		CACHE_DIR = context.getCacheDir();
		DENSITY = context.getResources().getDisplayMetrics().density;
		state = STATE_NONE;
		/* Spawn a new task to update the structures */
		CacheEntry request = FileCache.request(XML_URI, true);
		new Downloader(this).Setup(request);
	}
	/*TODO: remove this later*/
	public static Context getContext() {
		return context;
	}
	public static void setContext(Context context) {
		ExtData.context = context;
	}
	
	public void refresh () {
		CacheEntry request = FileCache.request(XML_URI, true);
		new Downloader(this).Setup(request);
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
			if (value.startsWith("/")) {
				advUri = context.getString(R.string.xml_uri) + value;
				CacheEntry request = FileCache.request(ADV_TAG);
				request.setUri(advUri);
				new Downloader(this).Setup(request);
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
			} else if (key.equals("img") && value.trim().startsWith("/")) {
				currentObj.setImgUri( context.getString(R.string.xml_uri) + value.trim() );
				CacheEntry request = FileCache.request(currentObj.getImgUri(), false);
				new Downloader(this).Setup(request);
			} else if (key.equals("category")) {
				currentObj.setName(value);
			}
			break;
		case STATE_DISH:
			if (key.equals("id")) {
				currentObj.setId(value);
			} else if (key.equals("available")) {
				((Dish)currentObj).setAvailFlag(value.toLowerCase().equals("true"));
			} else if (key.equals("category_id")) {
				currentObj.setCategoryId(value);
			} else if (key.equals("price")) {
				((Dish)currentObj).setPrice(Float.parseFloat(value));
			} else if (key.equals("name")) {
				currentObj.setName(value);
			} else if (key.equals("img") && value.trim().startsWith("/") ) {
				currentObj.setImgUri( context.getString(R.string.xml_uri) + value.trim() );
				CacheEntry request = FileCache.request(currentObj.getImgUri(), false);
				request.setResizeable(true);
				new Downloader(this).Setup(request);
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
	
	public void fillAdv(ImageView iv) {
		fillImg(ADV_TAG, iv);
	}
	
	public void fillImg(String uri, ImageView iv) {
		CacheEntry request;
		if (uri.equals(ADV_TAG)) {
			request = FileCache.request(uri);
		} else {
			request = FileCache.request(uri, false);
		}
		request.setImageView(iv);
		new Downloader(this).Setup(request);
	}
	
	@Override
	public void doParseXML(CacheEntry cache) {
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
					} else if (currentTag.toLowerCase().equals("cache")) {
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
		cache.setParsed(true);	
		
		} catch (XmlPullParserException e){
			Log.w(LOG_TAG, "XML parsing problem", e);			
		} catch (FileNotFoundException e) { 
			Log.w(LOG_TAG, "FileNotFound", e);
		} catch (IOException e) {
			Log.w(LOG_TAG, "IO problem", e);
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
				if (tmp.getName().equals(name)) return tmp;
			}
		}
		throw new NoSuchElementException("Can't find section: " + name);
	}
	
	public Vector<Dish> getDishesBySection(Section sect) {
		return dishes.get(sect);
	}
	
	public Vector<Section> getSubs(Section sect) {
		return subs.get(sect);
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

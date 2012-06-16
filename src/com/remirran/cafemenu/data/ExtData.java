package com.remirran.cafemenu.data;

import java.util.HashMap;
import java.util.Vector;

public class ExtData {
	public static final int STATE_NONE		= 0;
	public static final int STATE_UPDATE	= 1;
	public static final int STATE_ADV		= 2;
	public static final int STATE_SECTION	= 3;
	public static final int STATE_DISH		= 4;
	public static final int STATE_COMMAND	= 5;
	public static final int STATE_LAST		= 5;
	
	private static String lastUpdate;
	private static String advUri;
	private static final Vector<Section> sections = new Vector<Section>();
	private static final HashMap<String, Vector<Section>> subs = new HashMap<String, Vector<Section>>();
	private static final HashMap<String, Vector<Dish>> dishes = new HashMap<String, Vector<Dish>>();
	
	static class TheLock extends Object {
	}
	public static TheLock lockObject = new TheLock();
	
	private int state;
	
	public ExtData() {
		state = STATE_NONE;
	}
	
	public void setState(int stateNew) {
		state = stateNew;
		if (state < STATE_NONE || state > STATE_LAST) {
			throw new IndexOutOfBoundsException("State is out of bounds");
		}
	}
	
	/* Return URI if it is passed as value, otherwise null */
	public String setPair(String key, String value) {
		switch (state) {
		case STATE_NONE: 
			break;
		case STATE_ADV:
			advUri = value;
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
	
	public static String getAdv() {
		synchronized (ExtData.lockObject) {
			return advUri;
		}
	}
}

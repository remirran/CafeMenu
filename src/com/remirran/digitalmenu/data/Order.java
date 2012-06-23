package com.remirran.digitalmenu.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Order {
	private static HashMap<Dish, Integer> list = new HashMap<Dish, Integer>();
	private static ArrayList<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
	private static final String ATTR_IMG = "img";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_COUNT = "count";
	//private static final String[] from = {ATTR_IMG, ATTR_NAME, ATTR_COUNT};
	private static final String[] from = {ATTR_NAME, ATTR_COUNT};
	private int sum;
	
	public void set(Dish d, int count) {
		synchronized (list) {
			list.put(d, count);
		}
		calc();
	}
	public void inc(Dish d) {
		synchronized (list) {
			if (list.containsKey(d)) {
				int old = list.get(d);
				list.put(d, ++old);
			} else {
				list.put(d, 1);
			}
		}
		calc();
	}
	public void remove(Dish d) {
		synchronized (list) {
			list.remove(d);
		}
		calc();
	}
	public int getSum() {
		return sum;
	}
	private void calc() {
		synchronized (list) {
			data.clear();
			sum = 0;
			for (Dish key : list.keySet()) {
				sum =+ key.getPrice() * list.get(key);
				Map<String, Object> m = new HashMap<String, Object>();
				//m.put(ATTR_IMG, key);
				m.put(ATTR_NAME, key.getName());
				m.put(ATTR_COUNT, list.get(key));
				data.add(m);
			}
		}
	}
	public static int getCount() {
		return list.keySet().size();
	}
	
	public static String[] getFrom() {
		return from;
	}
	public static ArrayList<Map<String, Object>> getData() {
		return data;
	}
}

package com.remirran.digitalmenu.data;

import java.util.HashMap;

public class Order {
	private HashMap<Dish, Integer> list = new HashMap<Dish, Integer>();
	private int sum;
	
	public void set(Dish d, int count) {
		synchronized (list) {
			list.put(d, count);
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
			for (Dish key : list.keySet()) {
				sum =+ Integer.parseInt(key.getPrice()) * list.get(key);
			}
		}
	}
}

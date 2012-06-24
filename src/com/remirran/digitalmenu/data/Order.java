package com.remirran.digitalmenu.data;

import java.util.ArrayList;
import java.util.HashMap;

public class Order {
	private class Pair {
		private Dish dish;
		private int count;
		public Pair(Dish dish, int count) {
			this.dish = dish;
			this.count = count;
		}
		public Dish getDish() {
			return dish;
		}
		public int inc() {
			return ++count;
		}
		@Override
		public boolean equals(Object o) {
			return dish.equals(o);
		}
	}
	private static ArrayList<Pair> list = new ArrayList<Order.Pair>();
	private static int sum;
	
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
			sum = 0;
			for (Dish key : list.keySet()) {
				sum =+ key.getPrice() * list.get(key);
			}
		}
	}
	public static int getCount() {
		return list.size();
	}
}

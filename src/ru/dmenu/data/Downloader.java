package ru.dmenu.data;

import java.util.Vector;

import ru.dmenu.data.FileCache.CacheEntry;


import android.util.Log;

public class Downloader extends Thread{
	private static final String LOG_TAG="Downloader";
	private DlCallbacks listener;
	private static Vector<CacheEntry> queue = new Vector<CacheEntry>();
	CacheEntry task;

	@Override
	public void run() {
		try {
			while (true) {
				synchronized (queue) {
					if (queue.isEmpty()) {
						queue.wait();
					}
					task = queue.remove(0);
				}
				task.process();
				
				if (task.isParceable() ) {
					listener = ExtData.getInstance();
					listener.cleanOldValues();
					listener.doParseXML(task);
					if (task.isParsed()) {
						Runnable r = new Runnable() {
								
							@Override
							public void run() {
								listener.onXMLParsed();
								
							}
						};
						task.getHandler().post(r);
					}
				} else {
					task.applyImage();
				}
			}
		} catch(InterruptedException e) {
			Log.w(LOG_TAG, "Run exception: " + queue.size(), e);
		}
	}

	public void enqueue(CacheEntry task) {
		synchronized (queue) {
			queue.add(task);
			queue.notifyAll();
		}
	}
}

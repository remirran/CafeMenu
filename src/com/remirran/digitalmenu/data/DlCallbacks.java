package com.remirran.digitalmenu.data;

import com.remirran.digitalmenu.data.FileCache.CacheEntry;

public interface DlCallbacks {
	void doParseXML(CacheEntry cache);
	void onXMLParsed();
	void cleanOldValues();
}

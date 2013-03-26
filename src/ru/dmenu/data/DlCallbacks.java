package ru.dmenu.data;

import ru.dmenu.data.FileCache.CacheEntry;

public interface DlCallbacks {
	void doParseXML(CacheEntry cache);
	void onXMLParsed();
	void cleanOldValues();
}

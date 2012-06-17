package com.remirran.digitalmenu.data;

public interface DlCallbacks {
	void onFileReceived(FileCache cache);
	void doParseXML(FileCache cache);
	void onXMLParsed();
}

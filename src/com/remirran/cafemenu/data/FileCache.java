package com.remirran.cafemenu.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class FileCache {
	public static final long CACHE_TIME = 31536000; /* One year */
	private static Map<String, File> cacheIndex = Collections.synchronizedMap( new HashMap<String, File>() );
	private File cacheFile;
	private boolean isXml;
	
	public FileCache(File dir, String uri, boolean isMasterXml) throws FileNotFoundException, IOException {
		cacheFile = getCacheFile(dir, uri);
		isXml = isMasterXml;
		if (!isCached() || isXml) {
			fileSave(new URL(uri).openStream(), new FileOutputStream(cacheFile));
		}
		cacheIndex.put(uri, cacheFile);
	}
	/* TODO: Handle it somewhere */
	public FileCache(String uri, ImageView iv) throws FileNotFoundException {
		File cache = cacheIndex.get(uri);
		Bitmap bm = BitmapFactory.decodeStream(new FileInputStream(cache));
		iv.setImageBitmap(bm);
	}
	
	public boolean isCached() throws IOException {
		if (!cacheFile.exists()) {
			cacheFile.createNewFile();
			return false;
		} else {
			long time = new Date().getTime() / 1000;
			long timeLastModified = cacheFile.lastModified() / 1000;
			if ( timeLastModified + CACHE_TIME < time ) {
				cacheFile.delete();
				cacheFile.createNewFile();
				return false;
			}
		}
		return true;
	}
	
	public static File getCacheFile(File dir, String uri) {
		return new File(dir, md5(uri) + ".cache");
	}
	public boolean isXml() {
		return isXml;
	}
	
	public InputStream getInputStream() throws FileNotFoundException {
		return new FileInputStream(cacheFile);
	}
	
	public static String md5 (String s) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageHash[] = digest.digest();
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageHash.length; i++) {
				hexString.append(Integer.toHexString(0xFF & messageHash[i]));
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e){
			e.printStackTrace();
		}
		return "";
	}
	
	public static void fileSave (InputStream is, FileOutputStream os) {
		try {
			int i;
			while ((i = is.read()) != -1) {
				os.write(i);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean findObject(String object) {
		if (cacheIndex.get(object) != null) {
				return true;
		}
		return false;
	}
}

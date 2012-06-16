package com.remirran.cafemenu.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.util.Log;
import android.widget.ImageView;

public class FileCache {
	private static final String LOG_TAG = "FileCache"; 
	public static final long CACHE_TIME = 31536000; /* One year */
	private static HashMap<String, File> cacheIndex = new HashMap<String, File>();
	private static HashMap<String, ImageView> delayed = new HashMap<String, ImageView>();
	private File cacheFile;
	private boolean isXml;
	
	public FileCache(File dir, String uri, boolean update) throws FileNotFoundException, IOException {
		cacheFile = getCacheFile(dir, uri);
		if (!isCached() || update) {
			/* TODO: rewrite */
			fileSave(new URL(uri).openStream(), new FileOutputStream(cacheFile));
			final HttpClient client = AndroidHttpClient.newInstance("Android");
			final HttpUriRequest getRequest = new HttpGet(uri);
			try {
				HttpResponse response = client.execute(getRequest);
				final int status = response.getStatusLine().getStatusCode();
				if (status != HttpStatus.SC_OK) {
					/* TODO handle it somehow */
				}
				//isXml = response.getHeaders("Content-Type").toString().contains("text/xml");
				final HttpEntity entity = response.getEntity();
				try {
					entity.writeTo(new FileOutputStream(cacheFile));
				} finally {
					entity.consumeContent();
				}
			} catch (IOException e) {
				getRequest.abort();
				Log.w(LOG_TAG, "I/O error: url="+uri, e);
			} catch (IllegalStateException e) {
				getRequest.abort();
				Log.w(LOG_TAG, "Incorrect URI: "+uri, e);
			} catch (Exception e) {
				getRequest.abort();
				Log.w(LOG_TAG, "Exception during: " + uri, e);
			} finally {
				if ((client instanceof AndroidHttpClient)) {
					((AndroidHttpClient) client).close();
				}
			}
		}
		FileNameMap fnm = URLConnection.getFileNameMap();
		isXml = fnm.getContentTypeFor(cacheFile.getAbsolutePath()).contains("text/xml");
		
		synchronized (cacheIndex) {
			cacheIndex.put(uri, cacheFile);
			if (delayed.containsKey(uri)) {
				Bitmap bm = BitmapFactory.decodeStream(getInputStream());
				delayed.get(uri).setImageBitmap(bm);
				delayed.remove(uri);
			}
		}
	}
	/* TODO: Handle it somewhere */
	public static void fillImageFromCache(String uri, ImageView iv) throws FileNotFoundException {
		if (uri == null || uri.isEmpty()) {
			throw new FileNotFoundException("Empty URI string");
		}
		synchronized (cacheIndex) {
			File cache = cacheIndex.get(uri);
			if (cache == null) {
				/* TODO: change it to weak reference */
				delayed.put(uri, iv);
			} else {
				Bitmap bm = BitmapFactory.decodeStream(new FileInputStream(cache));
				iv.setImageBitmap(bm);
			}
		}
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
		synchronized (cacheIndex) {
			if (cacheIndex.get(object) != null) {
				return true;
			}
		}
		return false;
	}
}

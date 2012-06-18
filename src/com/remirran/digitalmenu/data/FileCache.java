package com.remirran.digitalmenu.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
	private String uri;
	private File cacheFile;
	private boolean isXml;
	
	public FileCache(String uri, boolean update) throws FileNotFoundException, IOException {
		this.uri = uri;
		isXml = uri.endsWith("xml") || uri.endsWith("html");
		cacheFile = getCacheFile(ExtData.CACHE_DIR, uri);
		if (!isCached() || update) {
			HttpClient client = AndroidHttpClient.newInstance("Android");
			HttpUriRequest getRequest = new HttpGet(uri);
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
		
		synchronized (cacheIndex) {
			cacheIndex.put(uri, cacheFile);
			Log.d(LOG_TAG, "SAVED:" + uri + " = " + cacheFile.getName());
		}
	}
	/* TODO: Handle it somewhere */
	public static void fillImageFromCache(String uri, ImageView iv) throws FileNotFoundException, NullPointerException {
		if (uri == null || uri.isEmpty()) {
			throw new FileNotFoundException("Empty URI string");
		}
		if (iv == null) {
			throw new FileNotFoundException("ImageView is null");
		}
		synchronized (cacheIndex) {
			Log.d(LOG_TAG, "REQ: "+uri + " = " + md5(uri));
			Bitmap bm = BitmapFactory.decodeStream(new FileInputStream(cacheIndex.get(uri)));
			iv.setImageBitmap(bm);
		}
	}
	
	public void fillImageFromCache(ImageView iv) {
		Bitmap bm;
		try {
			bm = BitmapFactory.decodeStream(getInputStream());
			iv.setImageBitmap(bm);
		} catch (FileNotFoundException e) {
			Log.w(LOG_TAG, "File not found: ", e);
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
	
	public String getUri() {
		return uri;
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

package com.remirran.digitalmenu.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.http.AndroidHttpClient;
import android.util.Log;
import android.widget.ImageView;

public class FileCache {
	private static final String LOG_TAG = "FileCache";
	private static final int ROUND_PX = 30;
	public static final long CACHE_TIME = 31536000; /* One year */
	private static HashMap<String, File> cacheIndex = new HashMap<String, File>();
	private String uri;
	private File cacheFile;
	private boolean isXml;
	
	/*TODO: Handle disconnects through CONNECTIVITY_CHANGE: http://stackoverflow.com/questions/1783117/network-listener-android */
	/*to create a request queue and check requests there in case of re-get anything*/
	public FileCache(String uri, boolean update) throws FileNotFoundException, IOException {
		this.uri = uri;
		isXml = uri.endsWith("xml") || uri.endsWith("html");
		cacheFile = getCacheFile(ExtData.CACHE_DIR, uri);
		if (!isCached() || update) {
			HttpClient client = AndroidHttpClient.newInstance("Android");
			HttpUriRequest getRequest = null;
			try {
				URL url = new URL(uri);
				URI safeURI = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
				getRequest = new HttpGet(safeURI);;
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
			} catch (IllegalStateException e) {
				getRequest.abort();
				Log.w(LOG_TAG, "Incorrect URI: "+uri, e);
			} catch (URISyntaxException e) {
				Log.w(LOG_TAG, "Can't create uri: " + uri, e);
			} catch (MalformedURLException e) {
				Log.w(LOG_TAG, "Can't parse uri: " + uri, e);
			} catch (IOException e) {
				getRequest.abort();
				Log.w(LOG_TAG, "I/O error: url="+uri, e);
			} catch (Exception e) {
				getRequest.abort();
				Log.w(LOG_TAG, "Exception during: " + uri, e);
			}finally {
				if ((client instanceof AndroidHttpClient)) {
					((AndroidHttpClient) client).close();
				}
			}
			synchronized (cacheIndex) {
				if(!getRequest.isAborted()) {
					cacheIndex.put(uri, cacheFile);
					Log.d(LOG_TAG, "SAVED:" + uri + " = " + cacheFile.getName());
				}
			}
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
		if (cacheIndex.get(uri) == null) {
			throw new FileNotFoundException("File is not in cache");
		}
		if (!cacheIndex.get(uri).exists()) {
			/* TODO: force reload*/
			throw new FileNotFoundException("File is not in cache");
		}
		synchronized (cacheIndex) {
			Log.d(LOG_TAG, "REQ: "+uri + " = " + md5(uri));
			Bitmap bm = BitmapFactory.decodeStream(new FileInputStream(cacheIndex.get(uri)));
//			int a = iv.getMeasuredHeight();
//			int b = iv.getMeasuredWidth();
//			Bitmap scale = Bitmap.createScaledBitmap(bm, Math.round(iv.getWidth() * ExtData.DENSITY), Math.round(iv.getHeight() * ExtData.DENSITY), true);
			iv.setImageBitmap(setRoundCorners(bm, ROUND_PX));
		}
	}
	
	public void fillImageFromCache(ImageView iv) {
		Bitmap bm;
		try {
			bm = BitmapFactory.decodeStream(getInputStream());
//			int a = iv.getHeight();
///			int b = iv.getWidth();
//			Bitmap scale = Bitmap.createScaledBitmap(bm, Math.round(iv.getWidth() * ExtData.DENSITY), Math.round(iv.getHeight() * ExtData.DENSITY), true);
			iv.setImageBitmap(setRoundCorners(bm, ROUND_PX));
		} catch (FileNotFoundException e) {
			Log.w(LOG_TAG, "File not found: ", e);
		}
	}
	
	private static Bitmap setRoundCorners(Bitmap in, int roundpx) {
		Bitmap output = Bitmap.createBitmap(in.getWidth(), in.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		
		/*TODO: add possibility to fill it from config*/
		final int color = 0xffcccccc;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, in.getWidth(), in.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPX = roundpx * ExtData.DENSITY;
		
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(in, rect, rect, paint);
		return output;
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
		if (cacheIndex.get(uri) == null) {
			cacheIndex.put(uri, cacheFile);
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

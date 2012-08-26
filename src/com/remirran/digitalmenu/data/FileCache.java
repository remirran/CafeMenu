package com.remirran.digitalmenu.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;

import com.remirran.digitalmenu.CafeMenuActivity;
import com.remirran.digitalmenu.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.net.http.AndroidHttpClient;
import android.util.Log;
import android.widget.ImageView;

public class FileCache {
	private static final String LOG_TAG = "FileCache";
	public static final long CACHE_TIME = 31536000; /* One year */
	
	public class CacheEntry {
		private static final int MAX_AVAILABLE = 1;
		private static final short MAX_DOWNLOADS = 3;
		private final Semaphore lock = new Semaphore(MAX_AVAILABLE);
		private String uri;
		private File mCachedFile = null;
		private File mImageResized = null;
		private WeakReference<ImageView> mImageView = null;
		private boolean mDownloaded = false;
		private boolean mParsed = false;
		private boolean mForceReload = false;
		private boolean mResizeable = false;
		
		/* Error protection */
		private short mDownloadsCounter = 0;
		
		public CacheEntry () {
		}
		
		public CacheEntry (String uri) {
			this.uri = uri;
			try {
				updateURIDeps();
			} catch (IOException e) {
				Log.e(LOG_TAG, "I/O error: url=" + uri, e);
			}
		}
		
		public void setForceReload(boolean flag) {
			if ( mDownloadsCounter < MAX_DOWNLOADS ) {
				this.mForceReload = flag;
			} else {
				Log.e(LOG_TAG, "Download limit reached for URI: " + uri);
				/* TODO: notify server */
			}
		}
		public void setResizeable(boolean flag) {
			this.mResizeable = flag;
			try {
				updateURIDeps();
			} catch (IOException e) {
				Log.e(LOG_TAG, "I/O error: url=" + uri, e);
			}
		}
		public void setParsed(boolean flag) {
			this.mParsed = flag;
		}
		public void setDownloaded(boolean flag) {
			this.mDownloaded = flag;
			if (flag) {
				mForceReload = !flag;
				mDownloadsCounter++;
			}
		}
		public void setUri(String uri) {
			try {
				lock.acquire();
				synchronized (mCacheIndex) {
					if (this.uri != null && !this.uri.isEmpty()) {
						mCacheIndex.remove(this.uri);
					}
					mCacheIndex.put(uri, this);
				}
				this.uri = uri;
			
				updateURIDeps();
			} catch (IOException e) {
				Log.e(LOG_TAG, "I/O error: url=" + uri, e);
			} catch (InterruptedException e) {
				Log.w(LOG_TAG, "Interrupted: " + uri, e);
			} finally {
				lock.release();
			}
		}
		public void setImageView(ImageView iv) {
			try {
				lock.acquire();
				this.mImageView = new WeakReference<ImageView>(iv);
			} catch (InterruptedException e) {
				Log.w(LOG_TAG, "Interrupted: " + uri, e);
			} finally {
				lock.release();
			}
		}
		
		public void clearImageView() {
			try {
				lock.acquire();
				this.mImageView = null;
			} catch (InterruptedException e) {
				Log.w(LOG_TAG, "Interrupted: " + uri, e);
			} finally {
				lock.release();
			}
		}
		
		private void updateURIDeps() throws IOException {
			String hash = md5(uri);
			mCachedFile =  new File(ExtData.CACHE_DIR, hash + ".cache");
			if (mResizeable) {
				mImageResized =  new File(ExtData.CACHE_DIR, hash + ".small.cache");
				if (!mImageResized.exists()) {
					mImageResized.createNewFile();
				}
			}
			
			if (!mCachedFile.exists()) {
				mCachedFile.createNewFile();
				mForceReload = true;
			} else {
				long time = new Date().getTime() / 1000;
				long timeLastModified = mCachedFile.lastModified() / 1000;
				if ( timeLastModified + CACHE_TIME < time ) {
					mCachedFile.delete();
					mCachedFile.createNewFile();
					if (mResizeable) {
						mImageResized.delete();
						mImageResized.createNewFile();
					}
					mForceReload = true;
				}
			}
		}
		
		public boolean isDownloaded() {
			return mDownloaded;
		}
		public boolean isResizeable() {
			return mResizeable;
		}
		
		public boolean isXml() {
			return uri.endsWith("xml") || uri.endsWith("html");
		}
		public boolean isParsed(){
			return mParsed;
		}
		
		public String getUri() {
			return uri;
		}
		
		public ImageView getImageView() {
			try {
				lock.acquire();
				return this.mImageView.get();
			} catch (InterruptedException e) {
				Log.w(LOG_TAG, "Interrupted: " + uri, e);
			} catch (NullPointerException e) {
				/*just return null*/
			} finally {
				lock.release();
			}
			return null;
		}
		
		public boolean exists() {
			return !mForceReload;
		}
		
		public InputStream getInputStream() throws FileNotFoundException {
			return new FileInputStream(mCachedFile);
		}
		
		public OutputStream getOutputStream() throws FileNotFoundException {
			return new FileOutputStream(mCachedFile);
		}
		
		public InputStream getResizedInputStream() throws FileNotFoundException {
			return new FileInputStream(mImageResized);
		}
		
		public OutputStream getResizedOutputStream() throws FileNotFoundException {
			return new FileOutputStream(mImageResized);
		}
		
		public Bitmap getBitmap() {
			Bitmap retval = null;
			try {
				retval = BitmapFactory.decodeStream(getInputStream());
				if ( retval == null ) {
					mForceReload = true;
					mDownloaded = false;
				}
			} catch (FileNotFoundException e) {
				Log.w(LOG_TAG, "FileNotFound: " + uri, e);
			}

			return retval;
		}
		
		public Bitmap getResizedBitmap() {
			Bitmap retval = null;
			try {
				retval = BitmapFactory.decodeStream(getResizedInputStream());
			
				if ( mDownloaded && retval == null ) {
					mForceReload = true;
					mDownloaded = false;
					mResizeable = true;
				}
			} catch (FileNotFoundException e) {
				Log.w(LOG_TAG, "FileNotFound: " + uri, e);
			}
			return retval;
		}
		
		public void process() {
			try {
				lock.acquire();
				
				if ( uri.length() > 0 && (mForceReload || mCachedFile.length() == 0) ) {
					downloadFile(this);
				}
				
				if ( !isXml() && mResizeable && mCachedFile.length() == 0 && (mDownloaded || mImageResized.length() == 0)  ) {
					prepareImageForTable(this);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				lock.release();
			}
		}
	}
	
	private static HashMap<String, CacheEntry> mCacheIndex = new HashMap<String, CacheEntry>();
	
	/*TODO: Handle disconnects through CONNECTIVITY_CHANGE: http://stackoverflow.com/questions/1783117/network-listener-android */
	/*to create a request queue and check requests there in case of re-get anything*/
	private static FileCache mInstance = null;
	private static FileCache getInstance() {
		if (mInstance == null) {
			mInstance = new FileCache();
		}
		return mInstance;
	}
	private FileCache() {
	}
	
	public static CacheEntry request(final String tag) {
		CacheEntry retval = null;
		synchronized (mCacheIndex) {
			retval = mCacheIndex.get(tag);
			if (retval == null) {
				retval = getInstance().new CacheEntry();
				mCacheIndex.put(tag, retval);
			}
		}
		return retval;
	}
	
	public static CacheEntry request(final String uri, boolean update) {
		CacheEntry retval = null;
		synchronized (mCacheIndex) {
			retval = mCacheIndex.get(uri);
			if (retval == null) {
				retval = getInstance().new CacheEntry(uri);
				mCacheIndex.put(uri, retval);
			}
			retval.setForceReload(update);
		}
		return retval;
	}
	
	public void downloadFile (CacheEntry cache) {
		HttpClient client = AndroidHttpClient.newInstance("Android");
		HttpClientParams.setRedirecting(client.getParams(), true);
		HttpUriRequest getRequest = null;
		String uri = cache.getUri();
		try {
			URL url = new URL(uri);
			URI safeURI = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
			getRequest = new HttpGet(safeURI);
			HttpResponse response = client.execute(getRequest);
			final int status = response.getStatusLine().getStatusCode();
			if (status != HttpStatus.SC_OK) {
				/* TODO handle it somehow */
			}
			final HttpEntity entity = response.getEntity();
			try {
				entity.writeTo(cache.getOutputStream());
			} finally {
				entity.consumeContent();
			}
		} catch (IllegalStateException e) {
			getRequest.abort();
			Log.w(LOG_TAG, "Incorrect URI: " + uri, e);
		} catch (URISyntaxException e) {
			Log.w(LOG_TAG, "Can't create uri: " + uri, e);
		} catch (MalformedURLException e) {
			Log.w(LOG_TAG, "Can't parse uri: " + uri, e);
		} catch (IOException e) {
			getRequest.abort();
			Log.w(LOG_TAG, "I/O error: url=" + uri, e);
		} catch (Exception e) {
			getRequest.abort();
			Log.w(LOG_TAG, "Exception during: " + uri, e);
		}finally {
			if ((client instanceof AndroidHttpClient)) {
				((AndroidHttpClient) client).close();
			}
		}
		if(!getRequest.isAborted()) {
			cache.setDownloaded(true);
		}
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
	
	
	private static final int RADIUS = 20;
	private static final int PNG_COMPRESSION = 90;
	
	private void prepareImageForTable(CacheEntry cache) {
		int scaledWidth = CafeMenuActivity.MAIN_TABLE_IMAGE_WIDTH;
		int scaledHeight = CafeMenuActivity.MAIN_TABLE_IMAGE_HEIGHT;
		
		Bitmap result = Bitmap.createBitmap(scaledWidth, scaledHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		Bitmap source = cache.getBitmap();
		if (source == null) {
			/*TODO: handle it in downloader*/
			return;
		}
		Bitmap mScaledBitmap;
		Bitmap mBack = Bitmap.createScaledBitmap(((BitmapDrawable)ExtData.getContext().getResources().getDrawable(R.drawable.background_dish)).getBitmap(), scaledWidth, scaledHeight, true);
		if (scaledWidth == source.getWidth() && scaledHeight == source.getHeight()) {
			mScaledBitmap = source;
		} else {
			mScaledBitmap = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true /* filter */);
		}
		Bitmap roundBitmap = setRoundCorners(mScaledBitmap, RADIUS, scaledWidth, scaledHeight);
		
		canvas.drawBitmap(roundBitmap, 0, 0, null);
		canvas.drawBitmap(mBack, 0, 0, null);
		
		try {
			OutputStream os = cache.getResizedOutputStream();
			result.compress(Bitmap.CompressFormat.PNG, PNG_COMPRESSION, os);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			Log.w(LOG_TAG, "FileNotFound: ", e);
		} catch (IOException e) {
			Log.w(LOG_TAG, "IO error: ", e);
		}
	}
	
	private Bitmap setRoundCorners(Bitmap source, int radius, int w, int h) {
		Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		
		final int color = 0xffcccccc;
		final Paint paint = new Paint();
		final Rect rect = new Rect(5, 5, w-5, h-5);
		final RectF rectF = new RectF(rect);
		final float roundPX = radius * ExtData.DENSITY;
		
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(source, rect, rect, paint);
		return output;
	}
}

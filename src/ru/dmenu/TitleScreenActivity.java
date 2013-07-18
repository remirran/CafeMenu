package ru.dmenu;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import ru.dmenu.data.ExtData;

import ru.dmenu.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class TitleScreenActivity extends Activity {
	private final String LOG_TAG = "TitleScreenActivity";
	public static Object pref_sync = new Object();
	public static boolean pref_flag = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.title);
		ImageView tAdv = (ImageView) findViewById(R.id.title_adv_img);
		ExtData.getInstance().fillAdv(tAdv);
		
		final TextView enterButton = (TextView) findViewById(R.id.title_enter_button);
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.title_enter_text_rotation);
		enterButton.startAnimation(anim);
		
		final OnClickListener defListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		};
		
		OnClickListener cfgListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
		        /*Show settings here*/
		        Intent tSettingsScreen = new Intent(TitleScreenActivity.this, PrefsActivity.class);
		        startActivity(tSettingsScreen);
				enterButton.setOnClickListener(defListener);
			}
		};
		enterButton.setOnClickListener(cfgListener);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		synchronized (pref_sync) {
			if (pref_flag) {
				new bindTable().execute(new Void[0]);
				pref_flag = false;
				pref_sync.notifyAll();
			}
		}
	}
	
	private class bindTable extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			HttpPost post = null;
			try {
				DefaultHttpClient http = new DefaultHttpClient();
				SharedPreferences shPrefs = PreferenceManager.getDefaultSharedPreferences(ExtData.getContext());
				
				JSONObject data = new JSONObject();
				data.put("restaurant_code", shPrefs.getString("pref_restaurant", "0"));
				data.put("table_code", shPrefs.getString("pref_table", "0"));
				data.put("busy", 1);
				data.put("waiter", 0);
				
				post = new HttpPost(getString(R.string.xml_uri) + getString(R.string.tables_ext)); //TODO: Replace later with REST
				
				ByteArrayEntity se = new ByteArrayEntity(data.toString().getBytes("UTF-8"));
				se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				post.setEntity(se);
				HttpResponse response = http.execute(post);
			} catch (UnsupportedEncodingException e) {
				Log.e(LOG_TAG, "Unsupported encoding", e);
	    		post.abort();
			} catch (ClientProtocolException e) {
				Log.e(LOG_TAG, "Can't send data", e);
				post.abort();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
}

package com.remirran.digitalmenu;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import com.remirran.digitalmenu.data.ExtData;
import com.remirran.digitalmenu.data.Section;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

public class CafeMenuActivity extends Activity {
	/* Constants */
	private final int REQ_CODE_TABLE_ID = 1;
	private final String LOG_TAG = "CafeMenuActivity";
	/* Tools */
	private ExtData eData;
	private static LayoutInflater ltInflatter;
	private static Handler hl;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        eData = new ExtData(this);
        hl = new Handler();
        /* Show adv window */
        /* TODO: move init screen there */
        Intent tAdvScreen = new Intent(this, TitleScreenActivity.class);
        startActivity(tAdvScreen);
        
        /* Init with the table number */
        /* TODO: skip this in locked state */
        //Intent tInitScreen = new Intent(this,InitScreenActivity.class);
        //startActivityForResult(tInitScreen, REQ_CODE_TABLE_ID);
        
        ltInflatter = getLayoutInflater();
    }
	
	public void applyDownloadedInfo() {
        /* Fill titles*/
        LinearLayout tTtlLayout = (LinearLayout) findViewById(R.id.main_title_layout);
        Vector<Section> sects = eData.getSections();
        synchronized (sects) {
			Collections.sort(sects);
			Iterator<Section> itr = sects.iterator();
			while (itr.hasNext()) {
				View tView = ltInflatter.inflate(R.layout.main_title_button, tTtlLayout, false);
				Button tButton = (Button) tView.findViewById(R.id.main_title_button);
				tButton.setText(itr.next().getName());
				tTtlLayout.addView(tView);
			}
		}
        
        /* Fill items for 1st section */
        ListView tLstLayout = (ListView) findViewById(R.id.main_list_layout);
        List<Section> subs = eData.getSubs(((Button)tTtlLayout.getChildAt(0)).getText().toString());
        List<String> subsTitles = (List) subs;
        ArrayAdapter<String> tLstAdapter = new ArrayAdapter<String>(this,
        		R.layout.main_section_item, subsTitles);
        tLstLayout.setAdapter(tLstAdapter);
        
        tLstLayout.setOnItemClickListener(new OnItemClickListener() {
        	@Override
            public void onItemClick(AdapterView<?> lv, View v, int position, long id) {
            	HorizontalScrollView tTableLayout = (HorizontalScrollView) findViewById(R.id.main_htable_layout);
            	unbindDrawables(tTableLayout);
            	System.gc();
            	
            	/* Start drawing */
                View tView = ltInflatter.inflate(R.layout.main_table, tTableLayout, true);
                
                /* Fill table */
                TableRow tTableRows[] = { (TableRow) tView.findViewById(R.id.main_table_row1), 
                		(TableRow) tView.findViewById(R.id.main_table_row2) };
               // for (int i = 0; i < dishes.length; i++ ) {
                //	tView = ltInflatter.inflate(R.layout.main_table_item, tTableRows[i%2], false);
               // 	ImageView tImg = (ImageView) tView.findViewById(R.id.main_table_img);
               // 	//ic.fetchImage(CafeMenuActivity.this, 3600, dishes[i][1], tImg);
               // 	tImg.setContentDescription(dishes[i][0]);
               // 	tTableRows[i%2].addView(tView);
                //}
                /* TODO: remove this */
                ((TextView) v).setBackgroundColor(Color.parseColor("#cccccc"));
            }
		});
        
        /* Init side menu with first section data */
        titleButtonOnClick(tTtlLayout.getChildAt(0));
            
	}
	
    public void titleButtonOnClick(View v) {
    	HorizontalScrollView tTableLayout = (HorizontalScrollView) findViewById(R.id.main_htable_layout);
    	unbindDrawables(tTableLayout);
    	//System.gc();
    	
    	ListView tLstLayout = (ListView) findViewById(R.id.main_list_layout);
        Vector<Section> subs = eData.getSubs(((Button)v).getText().toString());
        List<String> subsTitles = (List) subs;
        ArrayAdapter<String> tLstAdapter = new ArrayAdapter<String>(this,
        		R.layout.main_section_item, subsTitles);
        tLstLayout.setAdapter(tLstAdapter);
    	
        try {
	        View tView = ltInflatter.inflate(R.layout.main_table_splash, tTableLayout, false);
	        ImageView tImgView = (ImageView) tView.findViewById(R.id.main_table_splash);
	        Section sect = eData.getSectionByName((String) ((Button) v).getText());
	        ExtData.fillImg(sect.getImgUri(), hl, tImgView);
	        tTableLayout.addView(tView);
        } catch (NoSuchElementException e) {
        	Log.w(LOG_TAG, "Can't get section: ", e);
        }
    }
    
    private void unbindDrawables (View v) {
    	if (v.getBackground() != null){
    		if (v instanceof ImageView) {
    			BitmapDrawable content = (BitmapDrawable) ((ImageView) v).getDrawable();
    			if (content != null) {
    				Bitmap contentImg = content.getBitmap();
    				contentImg.recycle();
    				contentImg = null;
    			}
    		}
    		v.getBackground().setCallback(null);
    	}
    	if (v instanceof ViewGroup) {
    		for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++){
    			unbindDrawables(((ViewGroup) v).getChildAt(i));
    		}
    		((ViewGroup) v).removeAllViews();
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if (data == null) {
    		return;
    	}
    	if (resultCode == RESULT_OK) {
    		switch(requestCode) {
    		case REQ_CODE_TABLE_ID:
    			String tableId = data.getStringExtra("tableId");
    			Log.d("hz", tableId);
    			break;
    		default:
    			Log.d("hz", "requestCode: "+ requestCode);
    		}
    	}
    }

}
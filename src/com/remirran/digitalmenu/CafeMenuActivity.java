package com.remirran.digitalmenu;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import com.remirran.digitalmenu.data.Dish;
import com.remirran.digitalmenu.data.ExtData;
import com.remirran.digitalmenu.data.Order;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
	/*Vars*/
	private static final Order order = new Order();
	private static OrderAdapter orderAdapter;
	
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
        
        /*Setup order list*/
        //int orderTo[] = {R.id.order_pic, R.id.order_name, R.id.order_count};
       // int orderTo[] = {R.id.order_name, R.id.order_count};
       // orderAdapter = new OrderAdapter(this, Order.getData(), R.layout.main_order_item, Order.getFrom(), orderTo);
       // orderAdapter.setViewBinder(new orderViewBinder());
       // ListView tLstLayout = (ListView) findViewById(R.id.main_order_layout);
       // tLstLayout.setAdapter(orderAdapter);
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
        tLstLayout.setOnItemClickListener(new OnItemClickListener() {
        	@Override
            public void onItemClick(AdapterView<?> lv, View v, int position, long id) {
        		/*TODO: move to a function*/
            	mainDataClear();
            	
            	/* Start drawing */
            	LinearLayout tTableLayout = (LinearLayout) findViewById(R.id.main_data);
                View tView = ltInflatter.inflate(R.layout.main_table, tTableLayout, true);
                
                /* Fill table */
                TableRow tTableRows[] = { (TableRow) tView.findViewById(R.id.main_table_row1), 
                		(TableRow) tView.findViewById(R.id.main_table_row2) };
                Vector<Dish> dishes = eData.getDishesBySectionName(((TextView)v).getText().toString());
                try {
                	for (int i = 0; i < dishes.size(); i++ ) {
	                	tView = ltInflatter.inflate(R.layout.main_table_item, tTableRows[i%2], false);
	                	MenuImage tImg = (MenuImage) tView.findViewById(R.id.main_table_img);
	                	tImg.assign(dishes.elementAt(i));
	                	tTableRows[i%2].addView(tView);
	                }
                }catch (NullPointerException e) {
                	/*TODO: show something else, case of no dishes*/
                }
                
                /* TODO: remove this */
                //((TextView) v).setBackgroundColor(Color.parseColor("#cccccc"));
            }
		});
        
        /* Init side menu with first section data */
        titleButtonOnClick(tTtlLayout.getChildAt(0));
            
	}
	
	private void titleButtonSwitch(View v) {
		LinearLayout tTtlLayout = (LinearLayout) findViewById(R.id.main_title_layout);
		((Button)tTtlLayout.getChildAt(0)).setTextColor(Color.parseColor("#ffffff"));
        ((Button)tTtlLayout.getChildAt(0)).setBackgroundResource(R.drawable.menu_button_common);
        tTtlLayout.removeView(v);
        ((Button)v).setTextColor(Color.parseColor("#000000"));
        ((Button)v).setBackgroundResource(R.drawable.table_img_round_corners);
        ((Button)v).setBackgroundResource(R.drawable.menu_button_active);
        tTtlLayout.addView(v, 0);
        tTtlLayout.requestLayout();
	}
	
	private void updateSubsList(View v) {
		try {
			ListView tLstLayout = (ListView) findViewById(R.id.main_list_layout);
	        Vector<Section> subs = eData.getSubs(((Button)v).getText().toString());
	        List<String> subsTitles = (List) subs;
	        ArrayAdapter<String> tLstAdapter = new ArrayAdapter<String>(this,
	        		R.layout.main_section_item, subsTitles);
	        tLstLayout.setAdapter(tLstAdapter);
		} catch (NullPointerException e) {
        	/* TODO: case of no subs*/
        }
	}
	
	private void mainDataClear() {
		LinearLayout tTableLayout = (LinearLayout) findViewById(R.id.main_data);
    	unbindDrawables(tTableLayout);
    	System.gc();
	}
	
    public void titleButtonOnClick(View v) {
    	titleButtonSwitch(v);
        mainDataClear();
    	
    	updateSubsList(v);
        try {
        	LinearLayout tTableLayout = (LinearLayout) findViewById(R.id.main_data);
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
    	if (v == null) return;
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
    
    public void onTableImageClick (View v) {
    	order.inc(((MenuImage)v).getProduct());
    	//orderAdapter.notifyDataSetChanged();

    	
    	int orderTo[] = {R.id.order_name, R.id.order_count};
        orderAdapter = new OrderAdapter(this, Order.getData(), R.layout.main_order_item, Order.getFrom(), orderTo);
       // orderAdapter.setViewBinder(new orderViewBinder());
        ListView tLstLayout = (ListView) findViewById(R.id.main_order_layout);
        tLstLayout.setAdapter(orderAdapter);
    }
    
    class orderViewBinder implements SimpleAdapter.ViewBinder {

		@Override
		public boolean setViewValue(View view, Object data,
				String textRepresentation) {
			//switch(view.getId()) {
			//case R.id.order_pic:
			//	((MenuImage)view).assign((Dish)data);
			//	return true;
			//}
			return false;
		}
    	
    }
  

}
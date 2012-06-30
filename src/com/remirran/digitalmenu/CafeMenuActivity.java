package com.remirran.digitalmenu;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import com.remirran.digitalmenu.data.Dish;
import com.remirran.digitalmenu.data.ExtData;
import com.remirran.digitalmenu.data.Order;
import com.remirran.digitalmenu.data.Section;

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class CafeMenuActivity extends Activity {
	/* Constants */
	private final int REQ_CODE_TABLE_ID = 1;
	private final String LOG_TAG = "CafeMenuActivity";
	public static final int DIALOG_ORDER_REMOVE = 1;
	public static final int DIALOG_ORDER_ADD = 2;
	/* Tools */
	private ExtData eData;
	private static LayoutInflater ltInflatter;
	private static Handler hl;
	/* Order */
	private static final Order order = Order.getOrder();
	private static OrderAdapter orderAdapter;
	private static OrderDialog orderDialog;
	private Object dialogObject = null;
	/*Main window state save*/
	private static View save = null;
	
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
        
        orderAdapter = new OrderAdapter(this);
        ListView tLstLayout = (ListView) findViewById(R.id.main_order_layout);
        tLstLayout.setAdapter(orderAdapter);
        tLstLayout.setOnItemClickListener(orderListener);
    }
	
	public void applyDownloadedInfo() {
        /* Fill titles*/
        LinearLayout tTtlLayout = (LinearLayout) findViewById(R.id.main_title_layout);
        Vector<Section> sects = eData.getSections();
        synchronized (sects) {
			Collections.sort(sects);
			Iterator<Section> itr = sects.iterator();
			while (itr.hasNext()) {
				Section item = itr.next();
				View tView = ltInflatter.inflate(R.layout.main_title_button, tTtlLayout, false);
				Button tButton = (Button) tView.findViewById(R.id.main_title_button);
				tButton.setText(item.getName());
				tButton.setTag(item);
				tTtlLayout.addView(tView);
			}
		}
        
        
    	/* Fill items for 1st section */
        ListView tLstLayout = (ListView) findViewById(R.id.main_list_layout);
        tLstLayout.setOnItemClickListener(subsListener);
        
        /* Init side menu with first section data */
        titleButtonOnClick(tTtlLayout.getChildAt(0));
        /* TODO: remove this */
        //((TextView) v).setBackgroundColor(Color.parseColor("#cccccc"));
	}
	
	private OnItemClickListener subsListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			/*TODO: move to a function*/
        	mainDataClear();
        	
        	/* Start drawing */
        	LinearLayout tTableLayout = (LinearLayout) findViewById(R.id.main_data);
            View tView = ltInflatter.inflate(R.layout.main_table, tTableLayout, true);
            
            /* Fill table */
            LinearLayout tTableRows[] = { (LinearLayout) tView.findViewById(R.id.main_table_row1), 
            		(LinearLayout) tView.findViewById(R.id.main_table_row2) };
            Vector<Dish> dishes = eData.getDishesBySection((Section)parent.getAdapter().getItem(position));
            try {
            	for (int i = 0; i < dishes.size(); i++ ) {
                	tView = ltInflatter.inflate(R.layout.main_table_item, tTableRows[i%2], false);
                	MenuImage tImg = (MenuImage) tView.findViewById(R.id.main_table_img);
                	tImg.setMaxLtWidth(tTableLayout.getMeasuredWidth() / 2);
                	tImg.assign(dishes.elementAt(i));
                	tTableRows[i%2].addView(tView);
                }
            }catch (NullPointerException e) {
            	/*TODO: show something else, case of no dishes*/
            }
		}
	};
	
	private OnItemClickListener orderListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			dialogObject = orderAdapter.getItem(position);
			showDialog(DIALOG_ORDER_ADD);
			
		}
	};

	private void titleButtonSwitch(View v) {
		try {
			LinearLayout tTtlLayout = (LinearLayout) findViewById(R.id.main_title_layout);
			((Button)tTtlLayout.getChildAt(0)).setTextColor(Color.parseColor("#ffffff"));
		    ((Button)tTtlLayout.getChildAt(0)).setBackgroundResource(R.drawable.menu_button_common);
		    tTtlLayout.removeView(v);
		    ((Button)v).setTextColor(Color.parseColor("#000000"));
		    ((Button)v).setBackgroundResource(R.drawable.table_img_round_corners);
		    ((Button)v).setBackgroundResource(R.drawable.menu_button_active);
		    tTtlLayout.addView(v, 0);
		    tTtlLayout.requestLayout();
		} catch (NullPointerException e) {
			Log.w(LOG_TAG, "No menu buttons", e);
		}
	}
	
	private void updateSubsList(View v) {
		try {
			ListView tLstLayout = (ListView) findViewById(R.id.main_list_layout);
	        SubAdapter tLstAdapter = new SubAdapter(this, eData.getSubs((Section) ((Button)v).getTag()));
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
    	/*Restore main view if req*/
    	if (save != null) {
    		LinearLayout tll = (LinearLayout) findViewById(R.id.main);
    		tll.removeViewAt(1);
    		tll.addView(save);
    		save = null;
    		
    		/*Assign adapter*/
    		ListView tLstLayout = (ListView) tll.findViewById(R.id.main_order_layout);
            tLstLayout.setAdapter(orderAdapter);
            
            /*Reset order if req*/
        	if (Order.getSize() == 0) {
        		tll = (LinearLayout) findViewById(R.id.main_text_confirm);
        		if (tll != null) {
        			tll.removeAllViews();
        			ltInflatter.inflate(R.layout.main_order_empty, tll);
        		}
        	}
    	}
    	titleButtonSwitch(v);
        mainDataClear();
    	
    	updateSubsList(v);
        try {
        	LinearLayout tTableLayout = (LinearLayout) findViewById(R.id.main_data);
        	View tView = ltInflatter.inflate(R.layout.main_table_splash, tTableLayout, false);
	        ImageView tImgView = (ImageView) tView.findViewById(R.id.main_table_splash);
	        Section sect = (Section) ((Button) v).getTag();
	        ExtData.fillImg(sect.getImgUri(), hl, tImgView);
	        tTableLayout.addView(tView);
        } catch (NoSuchElementException e) {
        	Log.w(LOG_TAG, "Can't get section: ", e);
        } catch (NullPointerException e) {
        	Log.w(LOG_TAG, "Title bar is empty", e);
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
    
    public void onTableImageClick(View v) {
    	dialogObject = ((MenuImage)v).getProduct();
    	showDialog(DIALOG_ORDER_ADD);
    }
    
    public void onTableImageButtonClick (View v) {
    	order.inc(((MenuImage)v).getProduct());
    	updateOrderDetails();
    }
  
    public void onOrderCleanButtonClick (View v) {
    	order.clear();
    	updateOrderDetails();
    }
    
    public void updateOrderDetails () {
    	orderAdapter.notifyDataSetChanged();

    	TextView tv = (TextView) findViewById(R.id.order_total_sum);
    	if (Order.getSize() == 0) {
    		LinearLayout tll = (LinearLayout) findViewById(R.id.main_text_confirm);
    		if (tll != null) {
    			tll.removeAllViews();
    			ltInflatter.inflate(R.layout.main_order_empty, tll);
    		}
    	} else if (Order.getSize() > 0 && tv == null) {
    		LinearLayout tll = (LinearLayout) findViewById(R.id.main_text_confirm);
    		tll.removeAllViews();
    		View w = ltInflatter.inflate(R.layout.main_order_complete, tll, false);
    		tv = (TextView) w.findViewById(R.id.order_total_sum);
    		tll.addView(w);
    	}
    	
    	tv = (TextView) findViewById(R.id.order_total_sum);
    	if (tv != null) tv.setText(Order.getSum());
    	
    	
    }
    
    public void onOrderCompleteClick(View v) {
    	/*Replace main screen*/
    	LinearLayout tll = (LinearLayout) findViewById(R.id.main);
    	save = tll.getChildAt(1);
    	tll.removeViewAt(1);
    	ltInflatter.inflate(R.layout.order_second_screen, tll);
    	
    	/*Remove title button highlight*/
		try {
			LinearLayout tTtlLayout = (LinearLayout) findViewById(R.id.main_title_layout);
			((Button)tTtlLayout.getChildAt(0)).setTextColor(Color.parseColor("#ffffff"));
		    ((Button)tTtlLayout.getChildAt(0)).setBackgroundResource(R.drawable.menu_button_common);
		    tTtlLayout.requestLayout();
		} catch (NullPointerException e) {
			Log.w(LOG_TAG, "No menu buttons", e);
		}
		
		/*Set sum*/
		TextView tv = (TextView) findViewById(R.id.order_total_sum);
		tv.setText(Order.getSum());
		
		/*Assign adapter*/
		ListView tLstLayout = (ListView) tll.findViewById(R.id.main_order_layout);
        tLstLayout.setAdapter(orderAdapter);
    	
    }
    
    public void onOrderSendClick(View v) {
    	/*TODO: Send it on server*/
    	
    	LinearLayout tll = (LinearLayout) findViewById(R.id.order_second_screen);
    	tll.removeAllViews();
    	ltInflatter.inflate(R.layout.order_thanks, tll);
    }
    
    @Override
    protected Dialog onCreateDialog(int id, Bundle params) {
    	switch(id) {
    	case DIALOG_ORDER_REMOVE:
    		return null;
    	case DIALOG_ORDER_ADD:
    		orderDialog = new OrderDialog(this, (Dish)dialogObject);
    		return orderDialog.draw();
    	default:
    		return null;
    	}
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    	switch(id) {
    	case DIALOG_ORDER_ADD:
    		orderDialog.setDish((Dish)dialogObject);
    		LinearLayout view = (LinearLayout) dialog.getWindow().findViewById(R.id.dialog_root);
    		orderDialog.updateView(view);
    	default:
    		break;
    	}
    }

}
package com.remirran.cafemenu;

import java.util.Arrays;

import android.app.Activity;
import android.app.DownloadManager.Request;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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
	/* Tools */
	private final ImgCache ic = new ImgCache();
	private LayoutInflater ltInflatter;
	/*Stubs*/
	private final String title_buttons[] = {"Европейская кухня", "Кавказская кухня", "Японская кухня", "Бакалея"}; 
	private final String section_items[] = {"Холодные закуски", "Горячие закуски", "Салаты рыбные", "Салаты мясные", "Салаты овощные", "Первые блюда", "Вторые блюда", "Гарниры", "Закуски к пиву"};
	private final String dishes[][] = {
			{ "Филе сельди с картошкой", "http://edem.argroup52.ru/assets/images/europa/seld%20s%20kartoph.jpg", "90" },
			{ "Язык отварной", "http://edem.argroup52.ru/assets/images/europa/2.jpg", "160" },
			{ "Русский разносол", "http://edem.argroup52.ru/assets/images/europa/raznosol.jpg", "160" },
			{ "Корейская морковь", "http://edem.argroup52.ru/assets/images/europa/4.jpg", "40" },
			{ "Квашеная капуста","http://edem.argroup52.ru/assets/images/europa/5.jpg", "40" },
			{ "Рыбное ассорти", "http://edem.argroup52.ru/assets/images/europa/ribnoe%20assorti.jpg", "160" },
			{ "Мясная тарека", "http://edem.argroup52.ru/assets/images/europa/myasnaya%20tarelka.jpg", "220" },
			{ "Сырная тарелка", "http://edem.argroup52.ru/assets/images/europa/8.jpg", "180" },
			{ "Маслины/оливки", "http://edem.argroup52.ru/assets/images/europa/9.jpg", "75" },
			{ "Лимонная нарезка с сахаром", "http://edem.argroup52.ru/assets/images/europa/10.jpg", "50" }
	};
	private final String splashes[] = {
			"http://edem.argroup52.ru/assets/images/9/(1).JPG",
			"http://edem.argroup52.ru/assets/images/9/imagesK4.JPG",
			"http://edem.argroup52.ru/assets/images/9/imagesK3.JPG",
			"http://edem.argroup52.ru/assets/images/9/imageK2.JPG"
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /* Show adv window */
        /* TODO: move init screen there */
        Intent tAdvScreen = new Intent(this, TitleScreenActivity.class);
        startActivity(tAdvScreen);
        
        /* Init with the table number */
        /* TODO: skip this in locked state */
        Intent tInitScreen = new Intent(this,InitScreenActivity.class);
        startActivityForResult(tInitScreen, REQ_CODE_TABLE_ID);
        
        ltInflatter = getLayoutInflater();
        
        /* Fill titles*/
        LinearLayout tTtlLayout = (LinearLayout) findViewById(R.id.main_title_layout);
        for (int i = 0; i < title_buttons.length; i++ ) {
        	View tView = ltInflatter.inflate(R.layout.main_title_button, tTtlLayout, false);
        	Button tButton = (Button) tView.findViewById(R.id.main_title_button);
        	tButton.setText(title_buttons[i]);
        	tTtlLayout.addView(tView);
        }
        
        /* Fill items for 1st section */
        ListView tLstLayout = (ListView) findViewById(R.id.main_list_layout);
        ArrayAdapter<String> tLstAdapter = new ArrayAdapter<String>(this,
        		R.layout.main_section_item, section_items);
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
                for (int i = 0; i < dishes.length; i++ ) {
                	tView = ltInflatter.inflate(R.layout.main_table_item, tTableRows[i%2], false);
                	ImageView tImg = (ImageView) tView.findViewById(R.id.main_table_img);
                	ic.fetchImage(CafeMenuActivity.this, 3600, dishes[i][1], tImg);
                	tImg.setContentDescription(dishes[i][0]);
                	tTableRows[i%2].addView(tView);
                }
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
    	System.gc();
    	
        View tView = ltInflatter.inflate(R.layout.main_table_splash, tTableLayout, false);
        ImageView tImgView = (ImageView) tView.findViewById(R.id.main_table_splash);
        int index = Arrays.asList(title_buttons).indexOf(((Button) v).getText());
        ic.fetchImage(this, 3600, splashes[index], tImgView);
        tTableLayout.addView(tView);
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
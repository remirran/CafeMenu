package com.remirran.digitalmenu;

import com.remirran.cafemenu.R;
import com.remirran.digitalmenu.data.ExtData;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class TitleScreenActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.title);
		ImageView tAdv = (ImageView) findViewById(R.id.title_adv_img);
		Handler hl = new Handler();
		ExtData.fillAdv(hl, tAdv);
		
		
		
		TextView enterButton = (TextView) findViewById(R.id.title_enter_button);
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.title_enter_text_rotation);
		enterButton.startAnimation(anim);
		
		enterButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/* TODO: check that ImageView is destroyed properly */
				finish();
				
			}
		});
	}

}

package ru.dmenu;

import ru.dmenu.data.ExtData;

import ru.dmenu.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
	
}

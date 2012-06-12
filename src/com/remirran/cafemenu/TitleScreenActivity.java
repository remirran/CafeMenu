package com.remirran.cafemenu;

import android.app.Activity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class TitleScreenActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.title);
		
		TextView enterButton = (TextView) findViewById(R.id.title_enter_button);
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.title_enter_text_rotation);
		enterButton.startAnimation(anim);
	}

}

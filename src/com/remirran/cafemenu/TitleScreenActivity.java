package com.remirran.cafemenu;

import java.io.FileNotFoundException;

import com.remirran.cafemenu.data.ExtData;
import com.remirran.cafemenu.data.FileCache;

import android.app.Activity;
import android.os.Bundle;
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
	///	try {
			//FileCache fc = new FileCache(ExtData.getAdv(), tAdv);
	//	} catch (FileNotFoundException e) {
	//		e.printStackTrace();
	//	}
		ExtData.fillAdv(tAdv);
		
		
		
		TextView enterButton = (TextView) findViewById(R.id.title_enter_button);
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.title_enter_text_rotation);
		enterButton.startAnimation(anim);
	}

}

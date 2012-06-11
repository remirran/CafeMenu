package com.remirran.cafemenu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class InitScreenActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_init);
	}
	
	public void buttonOnClick(View v) {
		TextView tv = (TextView) findViewById(R.id.init_result);
		switch (v.getId()){
		case R.id.init_button0:
		case R.id.init_button1:
		case R.id.init_button2:
		case R.id.init_button3:
		case R.id.init_button4:
		case R.id.init_button5:
		case R.id.init_button6:
		case R.id.init_button7:
		case R.id.init_button8:
		case R.id.init_button9:
			tv.setText(tv.getText() + (String) ((Button) v).getText());
			break;
		case R.id.init_clear:
			tv.setText("");
			break;
		case R.id.init_enter:
			Intent resultIntent = new Intent();
			resultIntent.putExtra("tableId", tv.getText());
			setResult(RESULT_OK, resultIntent);
			finish();
			break;
		default:
			break;
		}
	}
}

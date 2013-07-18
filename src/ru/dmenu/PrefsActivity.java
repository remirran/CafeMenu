package ru.dmenu;

import ru.dmenu.R;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class PrefsActivity extends Activity {
	
	public static class PrefsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.global_prefs);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		synchronized (TitleScreenActivity.pref_sync) {
			TitleScreenActivity.pref_flag = true;
			TitleScreenActivity.pref_sync.notifyAll();
		}
	}
	
	
}

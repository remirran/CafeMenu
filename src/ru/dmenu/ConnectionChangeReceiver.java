package ru.dmenu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class ConnectionChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if ( activeNetInfo != null ) {
			Toast.makeText( context, "Active Network Type : " + activeNetInfo.getTypeName(), Toast.LENGTH_SHORT ).show();
		}
	}

}

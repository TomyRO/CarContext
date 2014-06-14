package com.hacktory.carcontext;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener{
	private Button locationButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//startButton = (Button) findViewById(R.id.start_service_button);
		//Log.d("Activity", startButton.toString());
		setContentView(R.layout.main_activity);
		Intent intent = new Intent(this, CarContextService.class);
		startService(intent);
		locationButton = (Button) findViewById(R.id.car_button);
		locationButton.setOnClickListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onClick(View arg0) {
		SharedPreferences prefs = this.getSharedPreferences("CarContext", 0);
		String latitude = prefs.getString("latitude", "");
		String longitude = prefs.getString("longitude", "");
		
		String uri = "geo:" + latitude + "," +longitude + "?q=" +
		    latitude + "," +longitude + "(My Car)";
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplicationContext().startActivity(i);
		
	}

} 
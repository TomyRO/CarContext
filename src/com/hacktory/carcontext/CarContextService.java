package com.hacktory.carcontext;

import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

public class CarContextService extends IntentService {

	public CarContextService() {
		super("CarContextService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Toast t = Toast.makeText(getApplicationContext(), "Service started !", Toast.LENGTH_SHORT);
		t.show();
	}

}

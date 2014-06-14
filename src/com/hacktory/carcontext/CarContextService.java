package com.hacktory.carcontext;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import static com.estimote.sdk.BeaconManager.MonitoringListener;

public class CarContextService extends Service {
  private static final String TAG = CarContextService.class.getSimpleName();
  private static final int NOTIFICATION_ID = 123;

  private BeaconManager beaconManager;
  private NotificationManager notificationManager;
  private LocationManager locationManager;
  private Region region;

  private String locationProvider;
  
  private Utils.Proximity lastProximity = Utils.Proximity.UNKNOWN;
  private Location lastLocation = null;
  
	public CarContextService() {
		//super("CarContextService");
		// TODO Auto-generated constructor stub
	}

  @Override
  public void onCreate() {
    notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
      // Display a notification about us starting.  We put an icon in the status bar.
    postNotification("Car context service started");
  }

  
  
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	  
    Log.i("LocalService", "Received start id " + startId + ": " + intent);
    region = new Region("myCarRegion", null, 1977, null);
    beaconManager = new BeaconManager(this);
    beaconManager.setRangingListener(new BeaconManager.RangingListener() {
      @Override
      public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
        // Note that results are not delivered on UI thread.
            // Note that beacons reported here are already sorted by estimated
            // distance between device and beacon.
        Utils.Proximity currentProximity = Utils.Proximity.UNKNOWN;
        if (beacons.size() > 0) {
          Beacon myBeacon = beacons.get(0);
          currentProximity = Utils.computeProximity(myBeacon);
        } 
        if (currentProximity != lastProximity) {
          if (currentProximity == Utils.Proximity.IMMEDIATE) {
            postNotification("My car is in immediate proximity");
          } else if (currentProximity == Utils.Proximity.NEAR) {
            postNotification("My car is nearby");
          } else {
            if (lastLocation != null) {
              SharedPreferences settings = getSharedPreferences("CarContext", 0);
              SharedPreferences.Editor editor = settings.edit();
                editor.putString("latitude", "" + lastLocation.getLatitude());
                editor.putString("longitude", "" + lastLocation.getLongitude());
                postNotification("location Saved");
                // Commit the edits!
                editor.commit();
            } else {
               postNotification("Dude, you lost your car, we had no location!");
            }
          }
        }
        lastProximity = currentProximity;
      }  // onBeaconsDiscovered
    });  // setRangingListener
    
    beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
      @Override public void onServiceReady() {
        try {
          beaconManager.startRanging(region);
        } catch (RemoteException e) {
          Log.e(TAG, "Cannot start ranging", e);
        }
      }
    });    

    locationProvider = locationManager.getBestProvider(new Criteria(), true);
    locationManager.requestLocationUpdates(locationProvider, 10000, 1,
        new LocationListener() {
          @Override
          public  void onLocationChanged(Location location) {
            lastLocation = location;
            postNotification("new location :" + location.toString());
          }
          @Override
          public void onProviderDisabled(String provider) {}
          @Override
          public void onProviderEnabled(String provider) {}
          @Override
          public void onStatusChanged(String provider, int status, Bundle extras) {}
        });
    
    
    postNotification("onStartCommand");
/*    beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);
    
    beaconManager.setMonitoringListener(new MonitoringListener() {
      @Override
      public void onEnteredRegion(Region region, List<Beacon> beacons) {
        postNotification("Entered region");
      }

      @Override
      public void onExitedRegion(Region region) {
        postNotification("Exited region");
      }
    });
*/
    
    // We want this service to continue running until it is explicitly
    // stopped, so return sticky.
    return START_STICKY;
}

	@Override
  public void onDestroy() {
      // Cancel the persistent notification.
      notificationManager.cancel(NOTIFICATION_ID);
      beaconManager.disconnect();
      // Tell the user we stopped.
      Toast.makeText(this, "car service destroyed", Toast.LENGTH_SHORT).show();
  }

	private void postNotification(String msg) {
    Intent notifyIntent = new Intent(CarContextService.this, CarContextService.class);
    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pendingIntent = PendingIntent.getActivities(
        CarContextService.this,
        0,
        new Intent[]{notifyIntent},
        PendingIntent.FLAG_UPDATE_CURRENT);
    Notification notification = new Notification.Builder(CarContextService.this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("Notify Car Context")
        .setContentText(msg)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build();
    notification.defaults |= Notification.DEFAULT_SOUND;
    notification.defaults |= Notification.DEFAULT_LIGHTS;
    notificationManager.notify(NOTIFICATION_ID, notification);

    Toast t = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
    t.show();  
  }

  @Override
  public IBinder onBind(Intent arg0) {
    // TODO Auto-generated method stub
    return null;
  }
}

package com.hacktory.carcontext;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
  private Region region;

  
	public CarContextService() {
		//super("CarContextService");
		// TODO Auto-generated constructor stub
	}

  @Override
  public void onCreate() {
    notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

      // Display a notification about us starting.  We put an icon in the status bar.
    postNotification("Car context service started");
  }

  
  
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i("LocalService", "Received start id " + startId + ": " + intent);
    region = new Region("myCarRegion", null, null, null);
    beaconManager = new BeaconManager(this);
    beaconManager.setRangingListener(new BeaconManager.RangingListener() {
      @Override
      public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
        // Note that results are not delivered on UI thread.
            // Note that beacons reported here are already sorted by estimated
            // distance between device and beacon.
            postNotification("Entered region with beacons" + beacons.size());
      }

    });    
    
    beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
      @Override public void onServiceReady() {
        try {
          beaconManager.startRanging(region);
        } catch (RemoteException e) {
          Log.e(TAG, "Cannot start ranging", e);
        }
      }
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

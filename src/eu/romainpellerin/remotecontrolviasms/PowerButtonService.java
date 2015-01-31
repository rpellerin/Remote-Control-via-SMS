package eu.romainpellerin.remotecontrolviasms;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * I made a service because I need the BroadcastReceiver to be registered even if the app is killed
 * Normally it should be a singleton
 * And it should never be killed
 */
public class PowerButtonService extends IntentService {

	private PowerButtonReceiver receiver; // singleton

	public PowerButtonService() {
		super("PowerButtonService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (receiver == null) {
			receiver = new PowerButtonReceiver();
		}
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
		// http://stackoverflow.com/a/15292719/2105309
		this.registerReceiver(receiver, filter);
		//Log.e("service","create");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onDestroy() {
		//Log.e("service","destroy");
		super.onDestroy();
		this.unregisterReceiver(receiver);
		// TODO relaunch the service
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		//Log.e("ok","onhandleintent "+intent.toString());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Log.e("ok","onstartcommand "+(intent!=null?intent.toString():"(null)"));
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

}

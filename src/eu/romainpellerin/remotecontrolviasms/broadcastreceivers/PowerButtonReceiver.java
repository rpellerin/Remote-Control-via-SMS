package eu.romainpellerin.remotecontrolviasms.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import eu.romainpellerin.remotecontrolviasms.R;

public class PowerButtonReceiver extends BroadcastReceiver {
	
	private static int counter = 0;
	private static long lastPress = 0;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        
		Log.e("POWER BUTTON", "Power button is pressed.");
		
		if ((System.currentTimeMillis() - 1000) <= lastPress) {
			lastPress = System.currentTimeMillis();
			counter++;
			if (counter >= 4) {
				SmsManager manager = SmsManager.getDefault();
				final String msg = prefs.getString("emergency_sms", "EMERGENCY!");
				final String recipient = prefs.getString("emergency_recipient","");
				if (!recipient.isEmpty()) {
					for(String num : recipient.split(",")) {
						manager.sendTextMessage(num.trim(), null, msg, null, null);
					}
					Toast.makeText(context, R.string.sms_sent, Toast.LENGTH_LONG).show();
					 Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
					 v.vibrate(500);
					 Log.e("EMERGENCY SMS", "SMS SENT");
				}
				counter = 0;
			}
		}
		else {
			lastPress = System.currentTimeMillis();
			counter = 1;
		}
	}
}

package eu.romainpellerin.remotecontrolviasms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class PowerButtonReceiver extends BroadcastReceiver {
	
	private static int counter = 0;
	private static long lastPress = 0;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        
		Log.e("POWER BUTTON", "Power button is pressed.");
		
		if (counter == 0) {
			lastPress = System.currentTimeMillis();
			counter++;
		}
		else if ((System.currentTimeMillis() - 1500) <= lastPress) {
			lastPress = System.currentTimeMillis();
			counter++;
			if (counter >= 3) {
				SmsManager manager = SmsManager.getDefault();
				String msg = prefs.getString("emergency_sms", "EMERGENCY!");
				String recipient = prefs.getString("emergency_recipient","");
				if (!recipient.isEmpty()) {
					manager.sendTextMessage(prefs.getString("emergency_recipient", ""), null, msg, null, null);
					Toast.makeText(context, R.string.sms_sent, Toast.LENGTH_LONG).show();
					Log.e("EMERGENCY", "SMS sent");
				}
				counter = 0;
			}
		}
		else {
			lastPress = System.currentTimeMillis();
			counter = 0;
		}
		
	}
}

package eu.romainpellerin.remotecontrolviasms.broadcastreceivers;

import eu.romainpellerin.remotecontrolviasms.PowerButtonService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Called at boot time
 */
public class BootCompletedReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			context.startService(new Intent(context, PowerButtonService.class));
		}
	}
}

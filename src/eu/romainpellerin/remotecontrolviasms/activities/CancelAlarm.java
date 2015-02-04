package eu.romainpellerin.remotecontrolviasms.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import eu.romainpellerin.remotecontrolviasms.R;
import eu.romainpellerin.remotecontrolviasms.broadcastreceivers.SmsReceiver;

public class CancelAlarm extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		setContentView(R.layout.cancelalarm);
		Button btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				try {
				SmsReceiver.stopMP();
				}
				catch (NullPointerException e) {}
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.cancel_alarm, menu);
		return false;
	}
	
	@Override
	public void onBackPressed() {} // disable back button
}

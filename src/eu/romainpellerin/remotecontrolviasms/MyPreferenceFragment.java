package eu.romainpellerin.remotecontrolviasms;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MyPreferenceFragment extends PreferenceFragment {
    public static final String ARG_PAGE = "page_number";
    private SharedPreferences.OnSharedPreferenceChangeListener listener_pref;
    public static final int PERMISSIONS_REQUEST_CODE = 4587515;                 // For identifying permission requests

    public MyPreferenceFragment() {
        // Empty constructor required for fragment subclasses
    }
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int page = getArguments().getInt(ARG_PAGE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        switch(page) {
        case 1: /* wifi */
        	addPreferencesFromResource(R.xml.prefs_wifi);
        	PreferenceManager.setDefaultValues(getActivity(), R.xml.prefs_wifi, true);
        	if (!prefs.getBoolean("wifi_enable", true)) {
        		getPreferenceScreen().findPreference("wifi_sms").setEnabled(false);
        	}
        	break;
        case 2: /* data */
            boolean useRootForData = prefs.getBoolean("enable_root_data", false);
        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !useRootForData) {
        		prefs.edit().putBoolean("data_enable", false).apply();
            }
        	addPreferencesFromResource(R.xml.prefs_data);
        	PreferenceManager.setDefaultValues(getActivity(), R.xml.prefs_data, true);
        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !useRootForData) {
        		getPreferenceScreen().findPreference("data_enable").setEnabled(false);
        		Toast.makeText(getActivity(),
                        RootUtils.isRooted() ? R.string.try_root_data : R.string.lolipop_nodata,
                        Toast.LENGTH_LONG).show();
        	}
        	if (!prefs.getBoolean("data_enable", true)) {
        		getPreferenceScreen().findPreference("data_sms").setEnabled(false);
        	}
        	break;
        case 3: /* beep */
        	addPreferencesFromResource(R.xml.prefs_beep);
        	PreferenceManager.setDefaultValues(getActivity(), R.xml.prefs_beep, true);
        	if (!prefs.getBoolean("beep_enable", true)) {
        		getPreferenceScreen().findPreference("beep_sms").setEnabled(false);
				getPreferenceScreen().findPreference("beep_play_again").setEnabled(false);
                getPreferenceScreen().findPreference("volume").setEnabled(false);
        	}
        	break;
        case 4: /* gps */
        	addPreferencesFromResource(R.xml.prefs_gps);
        	PreferenceManager.setDefaultValues(getActivity(), R.xml.prefs_gps, true);
        	if (!prefs.getBoolean("gps_enable", true)) {
        		getPreferenceScreen().findPreference("gps_sms").setEnabled(false);
        	}
        	int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
    		if (result != ConnectionResult.SUCCESS) { // NO GOOGLE PLAY SERVICES
    			if (prefs.edit().putBoolean("gps_enable", false).commit()) {
    				getPreferenceScreen().removeAll();
    				addPreferencesFromResource(R.xml.prefs_gps);
    			}
    			getPreferenceScreen().findPreference("gps_enable").setEnabled(false);
        		getPreferenceScreen().findPreference("gps_sms").setEnabled(false);
    			if (GooglePlayServicesUtil.isUserRecoverableError(result)) {
    	            GooglePlayServicesUtil.getErrorDialog(result, getActivity(), 9000).show();
    	        } else {
    				Toast.makeText(getActivity(),R.string.noplayservices, Toast.LENGTH_LONG).show();
    	        }
    		}
        	break;
        case 5: // emergency
        	addPreferencesFromResource(R.xml.prefs_emergency);
        	PreferenceManager.setDefaultValues(getActivity(), R.xml.prefs_emergency, true);
        	if (!prefs.getBoolean("emergency_enable", true)) {
        		getPreferenceScreen().findPreference("emergency_sms").setEnabled(false);
        		getPreferenceScreen().findPreference("emergency_recipient").setEnabled(false);
        	}
        	getPreferenceScreen().findPreference("emergency_sms").setSummary(prefs.getString("emergency_sms", "EMERGENCY!"));
        	getPreferenceScreen().findPreference("emergency_recipient").setSummary(prefs.getString("emergency_recipient", "123"));
        	break;
		case 6: /* rooted */
			addPreferencesFromResource(R.xml.prefs_rooted);
			PreferenceManager.setDefaultValues(getActivity(), R.xml.prefs_rooted, true);
			if (!prefs.getBoolean("enable_root", true)) {
                getPreferenceScreen().findPreference("test_root").setEnabled(false);
				getPreferenceScreen().findPreference("enable_root_gps").setEnabled(false);
				getPreferenceScreen().findPreference("enable_root_data").setEnabled(false);
			}
        default:
        	break;
        }
        listener_pref = new OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key.equals("wifi_enable")) {
					boolean bool = sharedPreferences.getBoolean(key, true);
					getPreferenceScreen().findPreference("wifi_sms").setEnabled(bool);
				}
				else if(key.equals("data_enable")) {
					boolean bool = sharedPreferences.getBoolean(key, true);
					getPreferenceScreen().findPreference("data_sms").setEnabled(bool);
				}
				else if(key.equals("beep_enable")) {
					boolean bool = sharedPreferences.getBoolean(key, true);
					getPreferenceScreen().findPreference("beep_sms").setEnabled(bool);
					getPreferenceScreen().findPreference("beep_play_again").setEnabled(bool);
					getPreferenceScreen().findPreference("volume").setEnabled(bool);
				}
				else if(key.equals("gps_enable")) {
					boolean bool = sharedPreferences.getBoolean(key, false);
					getPreferenceScreen().findPreference("gps_sms").setEnabled(bool);
				}
				else if(key.equals("emergency_enable")) {
					boolean bool = sharedPreferences.getBoolean(key, false);
					getPreferenceScreen().findPreference("emergency_sms").setEnabled(bool);
					getPreferenceScreen().findPreference("emergency_recipient").setEnabled(bool);
				}
				else if(key.equals("emergency_sms") || key.equals("emergency_recipient")) {
					getPreferenceScreen().findPreference("emergency_sms").setSummary(sharedPreferences.getString("emergency_sms", "EMERGENCY!"));
		        	getPreferenceScreen().findPreference("emergency_recipient").setSummary(sharedPreferences.getString("emergency_recipient", "123"));
				}
				else if(key.equals("enable_root")) {
					boolean bool = sharedPreferences.getBoolean(key, false);
                    getPreferenceScreen().findPreference("test_root").setEnabled(bool);
					getPreferenceScreen().findPreference("enable_root_gps").setEnabled(bool);
					getPreferenceScreen().findPreference("enable_root_data").setEnabled(bool);

					// Request root privileges
					if (bool) {
					    RootUtils.requestRootPrivileges();
                    }
				} else if(key.equals("enable_root_data")) {                 // Remind user to enable mobile data in Data preference page
				    if (sharedPreferences.getBoolean(key, false) &&
                            !sharedPreferences.getBoolean("data_enable", false)) {
                        Toast.makeText(getActivity(), R.string.enable_data, Toast.LENGTH_LONG).show();
                    }
                }
			}
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // If permissions were granted
        if (requestCode == PERMISSIONS_REQUEST_CODE && (grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            getPreferenceScreen().findPreference("gps_sms_get").setEnabled(true);
        } else {
            getPreferenceScreen().findPreference("gps_sms_get").setEnabled(false);
        }
    }
	
	@Override
	public void onResume() {
	    super.onResume();
	    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener_pref);
	}

	@Override
	public void onPause() {
	    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener_pref);
	    super.onPause();
	}
}

package eu.romainpellerin.remotecontrolviasms.broadcastreceivers;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import eu.romainpellerin.remotecontrolviasms.R;
import eu.romainpellerin.remotecontrolviasms.RootUtils;
import eu.romainpellerin.remotecontrolviasms.activities.CancelAlarm;

/* LOCATION */
/* Google Analytics */

public class SmsReceiver extends BroadcastReceiver implements ConnectionCallbacks, OnConnectionFailedListener {

    private Context cont;
    private String phoneNumber;
    private static MediaPlayer mp;
    private static AudioManager audioManager;
    private static int val_ini; // volume
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onReceive(final Context context, Intent intent) {
        cont = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        PreferenceManager.setDefaultValues(context, R.xml.prefs_wifi, true); // Met les settings par défaut si l'utilsateur n'a rien personnalisé
        PreferenceManager.setDefaultValues(context, R.xml.prefs_data, true);
        PreferenceManager.setDefaultValues(context, R.xml.prefs_beep, true);
        PreferenceManager.setDefaultValues(context, R.xml.prefs_gps, true);
        Bundle extras = intent.getExtras();

        if (extras != null) {
            GoogleAnalytics mGaInstance = GoogleAnalytics.getInstance(context);
            Tracker mGaTracker = mGaInstance.getTracker("UA-39228475-2");
            Object[] smsExtra = (Object[]) extras.get("pdus");
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[0]);
            phoneNumber = sms.getOriginatingAddress();
            String body = sms.getMessageBody();

            if (body.equalsIgnoreCase(prefs.getString("wifi_sms", "wifi")) && prefs.getBoolean("wifi_enable", true)) { // WIFI
                mGaTracker.send(MapBuilder.createEvent("received_sms", "received_sms", "WIFI", null).build());

                WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifi != null) {
                    wifi.setWifiEnabled(true);
                } else {
                    Toast.makeText(context, R.string.unable_to_enable_wifi, Toast.LENGTH_LONG).show();
                }
            }
            if (body.equalsIgnoreCase(prefs.getString("data_sms", "data")) && prefs.getBoolean("data_enable", true)) { // DATA
                mGaTracker.send(MapBuilder.createEvent("received_sms", "received_sms", "DATA", null).build());

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    try {
                        final Class<?> conmanClass = Class.forName(conman.getClass().getName());
                        final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
                        iConnectivityManagerField.setAccessible(true);
                        final Object iConnectivityManager = iConnectivityManagerField.get(conman);
                        final Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
                        final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
                        setMobileDataEnabledMethod.setAccessible(true);
                        setMobileDataEnabledMethod.invoke(iConnectivityManager, true);
                    } catch (Exception e) { // many
                        e.printStackTrace();
                    }
                } else if (prefs.getBoolean("enable_root_data", false)) {
                    RootUtils.enableMobileData();
                }
            }
            if (body.equalsIgnoreCase(prefs.getString("beep_sms", "beep")) && prefs.getBoolean("beep_enable", true)) { // BEEP
                mGaTracker.send(MapBuilder.createEvent("received_sms", "received_sms", "BEEP", null).build());

                if (mp == null) {
                    // Volume
                    audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    val_ini = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    int volumeToSet = ((prefs.getInt("volume", 100) * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) / 100);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeToSet, AudioManager.FLAG_VIBRATE);

                    final boolean loopmode = prefs.getBoolean("beep_play_again", false);

                    mp = MediaPlayer.create(context, R.raw.alarm);
                    mp.setOnCompletionListener(new OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            SmsReceiver.mp.reset();
                            SmsReceiver.mp.release();
                            SmsReceiver.mp = null;
                            // Remet le volume tel qu'il était avant
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, val_ini, AudioManager.FLAG_VIBRATE);
                        }
                    });

                    mp.setLooping(loopmode);
                    mp.start();
                    Intent inte = new Intent(context, CancelAlarm.class);
                    inte.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (loopmode) context.startActivity(inte);
                }
            }
            if (body.equalsIgnoreCase(prefs.getString("gps_sms", "gps")) && prefs.getBoolean("gps_enable", false) && GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) { // GPS
                mGaTracker.send(MapBuilder.createEvent("received_sms", "received_sms", "GPS", null).build());

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    Intent gps_intent = new Intent("android.location.GPS_ENABLED_CHANGE");
                    gps_intent.putExtra("enabled", true);
                    context.sendBroadcast(gps_intent);
                } else if (prefs.getBoolean("enable_root_gps", false)) {
                    RootUtils.enableGps();
                }

                mGoogleApiClient = new GoogleApiClient.Builder(context)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
                mGoogleApiClient.connect();
            }
        }
    }

    public static void stopMP() {
        mp.stop();
        mp.reset();
        mp.release();
        mp = null;
        // Remet le volume tel qu'il était avant
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, val_ini, AudioManager.FLAG_VIBRATE);
    }

    private StringBuffer addressToText(Address address) throws Exception {
        if (address == null) throw new Exception("No address provided");
        final StringBuffer addressText = new StringBuffer();
        for (int i = 0, max = address.getMaxAddressLineIndex(); i < max; ++i) {
            addressText.append(address.getAddressLine(i));
            if ((i + 1) < max) {
                addressText.append(", ");
            }
        }
        addressText.append(", ");
        addressText.append(address.getCountryName());
        return addressText;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Thread a = new Thread() {
            @Override
            public void run() {
                Location mLastLocation = null;
                int counter = 0;
                do {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {
                    }

                    if (ActivityCompat.checkSelfPermission(cont, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(cont, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    }

                    counter++;
                    if (counter > 120 && mLastLocation == null) {
                        return;
                    }
                } while (mLastLocation == null);

                String lat = String.valueOf(mLastLocation.getLatitude());
                String lon = String.valueOf(mLastLocation.getLongitude());
                String url = "http://maps.google.com/maps?q=" + lat + "," + lon;

                Geocoder geocoder = new Geocoder(cont);
                String address;
                try {
                    List<Address> result = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                    address = addressToText(result.get(0)).toString();
                } catch (Exception e) {
                    address = cont.getResources().getString(R.string.location_no_acuracy);
                }

                SmsManager manager = SmsManager.getDefault();
                String msg = address + " (precision=" + mLastLocation.getAccuracy() + "m)";
                manager.sendTextMessage(phoneNumber, null, msg, null, null);
                manager.sendTextMessage(phoneNumber, null, url, null, null);
                if (mGoogleApiClient.isConnected()) mGoogleApiClient.disconnect();
            }
        };
        a.start();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        SmsManager manager = SmsManager.getDefault();
        manager.sendTextMessage(phoneNumber, null, "Connection failed (suspended, code: " + String.valueOf(cause) + ")", null, null);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        SmsManager manager = SmsManager.getDefault();
        manager.sendTextMessage(phoneNumber, null, "Connection failed", null, null);
    }
}

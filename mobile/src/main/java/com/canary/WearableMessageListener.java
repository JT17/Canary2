package com.canary;

import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by JonathanTiao on 9/6/14.
 */
public class WearableMessageListener extends WearableListenerService {
    private static final String GET_HELP = "/text_for_help";
    private static final String STOP_HELP = "/stop_help";
    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences mSharedPreferences;
    @Override
    public void onCreate() {
        Log.d("OUTPUT", "Starting ResponseListenerService (PHONE)!");
        super.onCreate();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        Log.d("OUTPUT", "Message received on PHONE!!");
        if (messageEvent.getPath().equals(GET_HELP)){
            Log.d("Message Path", messageEvent.getPath());
            String response = new String(messageEvent.getData());
            Log.d("Message Contents", response);
          /*  runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    smssender.sendSMSOnTime(true, true, MainActivity.mCurrentLocation, context);
                }
            });*/

           //Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationManager locationManager = (LocationManager) this
                    .getSystemService(LOCATION_SERVICE);
            Location mCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Log.d("Location", mCurrentLocation.toString());

            SMSSender smssender =  new SMSSender();
            smssender.sendSMSOnTime(true, true, mCurrentLocation, this);



        }
        else if(messageEvent.getPath().equals(STOP_HELP)){
            Log.d("Message path", messageEvent.getPath());
            String response = new String(messageEvent.getData());
            Log.d("Message Contents", response);
        }
    }

}

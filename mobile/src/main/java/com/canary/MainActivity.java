package com.canary;

import android.app.Activity;
import android.app.Dialog;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    //Global constants for location updates
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 15;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    private static final String GET_HELP = "/text_for_help";
    private static final String STOP_HELP = "/stop_help";
    private SMSSender smss;
    //Global Variables to keep track of location
    private GoogleApiClient mGoogleApiClient;
    public static Location mCurrentLocation;
    // Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;

    boolean updatesRequested;
    boolean hasClicked;

    Handler handler = new Handler();
    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            sendSMSOnTime();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(ConnectionResult.SUCCESS == resultCode)
            Log.d("Location updates", "Google play services is available");
        else{
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            if (errorDialog != null) {
                Log.d("Location failure", errorDialog.toString());
            }
        }
        updatesRequested = true; //assume app opened so updates are requested
        hasClicked = false;
    }
    //Called when activity is opened (so when the app is opened)
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.

        mGoogleApiClient.connect();

        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);




    }

    @Override
    protected void onStop(){
        super.onStop();
        mGoogleApiClient.disconnect();
    }
    public void onClick(View v)
    {
        switch(v.getId()) {
            case R.id.main:
                Button mButton = (Button) v.findViewById(R.id.button_textoff);
                if(mButton.getVisibility() == View.GONE){
                    mButton.setVisibility(View.VISIBLE);
                }
                if(mCurrentLocation != null && updatesRequested) {
                    // String locationString;
                    hasClicked = true;
                    runnable.run();
                }
                //  locationString = "My latitude is " + mCurrentLocation.getLatitude() +
                //            " and my longitude is " + mCurrentLocation.getLongitude();
                // Log.v("Location", locationString);
                // sendSMS("7852182716", locationString);
                break;
            case R.id.button_textoff:
                updatesRequested = !updatesRequested;
                Button mButton1 = (Button) v.findViewById(R.id.button_textoff);
                String buttonText = "on";
                if(updatesRequested == true)
                    buttonText = "off";
                mButton1.setText("Turn " + buttonText + " texts");

                break;
        }
    }
    public void sendSMSOnTime(){
        smss.sendSMSOnTime(updatesRequested, hasClicked, mCurrentLocation, this);
        handler.postDelayed(runnable,10000);
    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v("Disconnected", "disconnected");
    }

    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        mCurrentLocation = location;
    }


    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        Log.v("Connected", "connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
*/
}

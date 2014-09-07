package com.canary;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.CountDownLatch;

public class WearableActivity extends Activity implements SensorEventListener, MessageApi.MessageListener,GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = WearableActivity.class.getName();
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private static final int SENSOR_TYPE_HEARTRATE = 65562;
    private TextView mTextView;
    private Context mContext;
    private ConfirmationActivity confirmationActivity;
    PendingIntent pendingIntent;
    private CountDownLatch latch;
    double firstMeasure;
    boolean sendMessage;
    boolean isConnected;
    private GoogleApiClient mGoogleApiClient;
    private static final String GET_HELP = "/text_for_help";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wearable);
        latch = new CountDownLatch(1);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                latch.countDown();
            }
        });

        mContext = this;
        pendingIntent =
                PendingIntent.getActivity(this, 0, new Intent(this, WearableActivity.class), 0);
        mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE_HEARTRATE);
        sendMessage = false;
        isConnected = false;

    }
    @Override
    protected void onStart() {
        super.onStart();
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        try {

            latch.await();
            if(sensorEvent.values[0] > 0)
                firstMeasure = sensorEvent.values[0];
          //  Log.d(TAG, "sensor event: " + sensorEvent.accuracy + " = " + sensorEvent.values[0]);
            TextView view = (TextView) findViewById(R.id.text);
            view.setText(Float.toString(sensorEvent.values[0]));
            if (sensorEvent.values[0] > 1.3 * firstMeasure) {
                Toast.makeText(this, "alerting authorities", Toast.LENGTH_SHORT);
                sendMessage = true;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "accuracy changed: " + i);
    }

    @Override
    protected void onStop() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
        mSensorManager.unregisterListener(this);
    }
    public void onClick(View v){
        //Log.v("Viewpoop", Integer.toString(v.getId()));
        switch(v.getId()){
            case (R.id.wearablemain):
                Log.v("Button", "Touched");
                if(isConnected){
                    new RequestDataAsyncTask().execute();
                }

        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v("Connection", "Devices Connected");
        isConnected = true;
        Wearable.MessageApi.addListener(mGoogleApiClient, this);

    }
    public class RequestDataAsyncTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... args) {
            NodeApi.GetConnectedNodesResult nodes =
                    Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

            if (nodes.getNodes().size() > 0) {
                Wearable.MessageApi.sendMessage(
                        mGoogleApiClient, nodes.getNodes().get(0).getId(), GET_HELP, "".getBytes()).setResultCallback(
                        new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                if (!sendMessageResult.getStatus().isSuccess()) {
                                    Log.d("WATCH OUTPUT", "Failed to send message with status code: "
                                            + sendMessageResult.getStatus().getStatusCode());
                                } else {
                                    Log.d("WATCH OUTPUT", "Successfully requested train times");
                                }
                            }
                        }
                );
            }

            return null; //return value doesn't matter...
        }
    }
    //Ignore this for now
    @Override
    public void onConnectionSuspended(int i) {

    }
    //Doesn't need to be implemented b/c phone shouldn't talk to watch as of now
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

    }
}

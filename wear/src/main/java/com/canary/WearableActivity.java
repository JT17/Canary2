package com.canary;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.CountDownLatch;

public class WearableActivity extends Activity implements SensorEventListener {
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
        Log.v("LKSFJ", "a;dskjfa;");
        pendingIntent =
                PendingIntent.getActivity(this, 0, new Intent(this, WearableActivity.class), 0);
        mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE_HEARTRATE);

    }
    @Override
    protected void onStart() {
        super.onStart();
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        try {

            latch.await();
            if(sensorEvent.values[0] > 0)
                firstMeasure = sensorEvent.values[0];
            Log.d(TAG, "sensor event: " + sensorEvent.accuracy + " = " + sensorEvent.values[0]);
            TextView view = (TextView) findViewById(R.id.text);
            view.setText(Float.toString(sensorEvent.values[0]));
            if (sensorEvent.values[0] > 1.3 * firstMeasure)
                Toast.makeText(this, "alerting authorities", Toast.LENGTH_SHORT);
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
        super.onStop();

        mSensorManager.unregisterListener(this);
    }
    public void onClick(View v){
        Log.v("Viewpoop", Integer.toString(v.getId()));
        switch(v.getId()){
            case (R.id.button):
                Log.v("Button", "Touched");
                Notification notif = new Notification.Builder(mContext)
                        .setContentTitle("My Notification")
                        .setContentText("This is a notification")
                        .setSmallIcon(R.drawable.ic_plusone_small_off_client)
                        .build();

                NotificationManager notificationManger =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManger.notify(0, notif);
        }
    }
}

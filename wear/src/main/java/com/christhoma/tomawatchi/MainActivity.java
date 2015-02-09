package com.christhoma.tomawatchi;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends Activity implements SensorEventListener {

    private GifDrawable gif;
    private GifImageView gifImageView;
    private SensorManager sensorManager;
    private Sensor sensor;
    private boolean stopped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        final WatchViewStub watchViewStub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        watchViewStub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub watchViewStub) {
                gifImageView = (GifImageView) watchViewStub.findViewById(R.id.gif);
                try {
                    gif = new GifDrawable(getResources(), R.drawable.bulba_walking);
                } catch (IOException e) {

                }
                gifImageView.setImageDrawable(gif);
                gifImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startOrStopGif();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    public void startOrStopGif() {
        if (gif != null) {
            if (stopped) {
                gif.start();
                stopped = false;
            } else {
                gif.stop();
                stopped = true;
            }
        }
    }

    public void startGif() {
        if (gif != null) {
            if (!gif.isPlaying()) {
                gif.start();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            startGif();
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            startGif();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

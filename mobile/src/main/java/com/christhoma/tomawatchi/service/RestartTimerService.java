package com.christhoma.tomawatchi.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.christhoma.tomawatchi.Const;

import java.util.Calendar;

/**
 * Created by christhoma on 3/13/15.
 */
public class RestartTimerService extends IntentService {

    public RestartTimerService() {
        super("RestartTimerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int updatesMissed = intent.getIntExtra(Const.UPDATES_MISSED, 0) - 1;

        SharedPreferences prefs = getSharedPreferences(Const.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int fitness = prefs.getInt(Const.FITNESS, -1);
        int hunger = prefs.getInt(Const.HUNGER, -1);
        int cleanliness = prefs.getInt(Const.CLEANLINESS, -1);

        if (updatesMissed > 1) {
            fitness -= updatesMissed;
            if (fitness < 0) {
                fitness = 0;
            }
            hunger -= updatesMissed;
            if (hunger < 0) {
                hunger = 0;
            }
            cleanliness -= updatesMissed;
            if (cleanliness < 0) {
                cleanliness = 0;
            }
        }

        editor.putInt(Const.FITNESS, fitness);
        editor.putInt(Const.HUNGER, hunger);
        editor.putInt(Const.CLEANLINESS, cleanliness);
        editor.apply();

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, PetPointsReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Const.ALARM_ID, alarmIntent, 0);
        Calendar calendar = Calendar.getInstance();
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 60 * 60 * 1000, pendingIntent);

    }
}

package com.christhoma.tomawatchi.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.christhoma.tomawatchi.Const;

import org.joda.time.Hours;
import org.joda.time.LocalDate;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by christhoma on 2/15/15.
 */
public class PhoneLifecycleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(Const.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED) {
            LocalDate now = new LocalDate();
            LocalDate then = new LocalDate(prefs.getLong(Const.SHUT_DOWN_TIME, -1));
            int hoursBetween = Hours.hoursBetween(now, then).getHours();
            if (hoursBetween > 0) {
                hoursBetween--;
            }
            int fitness = prefs.getInt(Const.FITNESS, -1) - hoursBetween;
            int cleanliness = prefs.getInt(Const.CLEANLINESS, -1) - hoursBetween;
            int hunger = prefs.getInt(Const.HUNGER, -1) - hoursBetween;
            editor.putInt(Const.FITNESS, fitness);
            editor.putInt(Const.HUNGER, hunger);
            editor.putInt(Const.CLEANLINESS, cleanliness);
            editor.apply();

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(context, PetPointsReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Const.ALARM_ID, alarmIntent, 0);
            Calendar calendar = Calendar.getInstance();
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 60 * 60 * 1000, pendingIntent);

        } else if (intent.getAction() == Intent.ACTION_SHUTDOWN || intent.getAction() == "android.intent.action.QUICKBOOT_POWEROFF") {
            Date now = new Date();
            editor.putLong(Const.SHUT_DOWN_TIME, now.getTime()).apply();
        }
    }
}

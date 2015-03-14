package com.christhoma.tomawatchi.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.christhoma.tomawatchi.Const;
import com.christhoma.tomawatchi.R;
import com.christhoma.tomawatchi.activity.MainActivity;

import java.util.Date;

/**
 * Created by christhoma on 2/16/15.
 */
public class UpdateStatsService extends IntentService {

    public UpdateStatsService() {
        super("UpdateStatsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SharedPreferences prefs = getSharedPreferences(Const.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int fitness = prefs.getInt(Const.FITNESS, -1) - 1;
        int hunger = prefs.getInt(Const.HUNGER, -1) - 1;
        int cleanliness = prefs.getInt(Const.CLEANLINESS, -1) - 1;
        if (fitness < 0) {
            fitness = 0;
        }
        if (hunger < 0) {
            hunger = 0;
        }
        if (cleanliness < 0) {
            cleanliness = 0;
        }
        String name = prefs.getString(Const.NAME, "Tomawatchi");
        editor.putInt(Const.FITNESS, fitness);
        editor.putInt(Const.HUNGER, hunger);
        editor.putInt(Const.CLEANLINESS, cleanliness);
        Date date = new Date();
        long now = date.getTime();
        editor.putLong(Const.LAST_UPDATE_TIME, now);
        editor.apply();

        sendBroadcast(new Intent(Const.UPDATE_PET));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(name)
                .setContentText("Take care of me!")
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);

        int numberOfActions = 0;
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();

        Intent petCareIntent = new Intent(this, PetCareService.class);
        Intent launchIntent = new Intent(this, MainActivity.class);

        if (hunger == 25 || hunger == 10) {
            numberOfActions++;
            NotificationCompat.Action feedAction = new NotificationCompat.Action(R.drawable.ic_feed, "Feed me!", PendingIntent.getService(this, Const.PET_CARE_ID+1, petCareIntent, PendingIntent.FLAG_CANCEL_CURRENT));
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.bg_food);

            wearableExtender.setBackground(icon);
            wearableExtender.addAction(feedAction);
            petCareIntent.putExtra(Const.PET_CARE_EXTRA_HUNGER, Const.HUNGER);

        } else if (cleanliness == 25 || cleanliness == 10) {
            numberOfActions++;
            NotificationCompat.Action cleanAction = new NotificationCompat.Action(R.drawable.ic_clean, "Bathe me!", PendingIntent.getService(this, Const.PET_CARE_ID+2, petCareIntent, PendingIntent.FLAG_CANCEL_CURRENT));
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.bg_clean);

            wearableExtender.setBackground(icon);
            wearableExtender.addAction(cleanAction);
            petCareIntent.putExtra(Const.PET_CARE_EXTRA_CLEANLINESS, Const.CLEANLINESS);
        }
        if (numberOfActions == 2) {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.bg_clouds);
            wearableExtender.setBackground(icon);
        }


        if (numberOfActions > 0) {
            PendingIntent.getActivity(this, Const.PET_CARE_ID+3, launchIntent, 0);
            notificationBuilder.extend(wearableExtender);
            notificationManager.notify(Const.PET_CARE_ID, notificationBuilder.build());
        }
    }
}

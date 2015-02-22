package com.christhoma.tomawatchi.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;

import com.christhoma.tomawatchi.Const;

/**
 * Created by christhoma on 2/16/15.
 */
public class PetCareService extends IntentService {


    public PetCareService() {
        super("PetCareService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        SharedPreferences prefs = getSharedPreferences(Const.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int fitness = prefs.getInt(Const.FITNESS, -1);
        int hunger = prefs.getInt(Const.HUNGER, -1);
        int cleanliness = prefs.getInt(Const.CLEANLINESS, -1);

        if (hunger + 25 > 100) {
            hunger = 100;
        } else {
            hunger += 25;
        }
        if (cleanliness + 25 > 100) {
            cleanliness = 100;
        } else {
            cleanliness += 25;
        }

        notificationManager.cancel(Const.PET_CARE_ID);

        editor.putInt(Const.FITNESS, fitness);
        editor.putInt(Const.HUNGER, hunger);
        editor.putInt(Const.CLEANLINESS, cleanliness);
        editor.apply();


        sendBroadcast(new Intent(Const.UPDATE_PET));
    }
}

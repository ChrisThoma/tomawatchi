package com.christhoma.tomawatchi.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.christhoma.tomawatchi.Const;

/**
 * Created by christhoma on 2/16/15.
 */
public class UpdateStatsService extends IntentService {

    public UpdateStatsService() {
        super("UpdateStatsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("tomawatchi", "onHandleIntent updatestatsservice");

        SharedPreferences prefs = getSharedPreferences(Const.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int fitness = prefs.getInt(Const.FITNESS, -1) - 1;
        int hunger = prefs.getInt(Const.HUNGER, -1) - 1;
        int cleanliness = prefs.getInt(Const.CLEANLINESS, -1) - 1;
        editor.putInt(Const.FITNESS, fitness);
        editor.putInt(Const.HUNGER, hunger);
        editor.putInt(Const.CLEANLINESS, cleanliness);
        editor.apply();

        sendBroadcast(new Intent(Const.UPDATE_PET));
    }
}

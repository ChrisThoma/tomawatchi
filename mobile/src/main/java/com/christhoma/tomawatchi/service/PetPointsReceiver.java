package com.christhoma.tomawatchi.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.christhoma.tomawatchi.Const;
import com.christhoma.tomawatchi.api.Tomawatchi;

import java.util.Date;

/**
 * Created by christhoma on 2/8/15.
 */
public class PetPointsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "ok hi", Toast.LENGTH_SHORT).show();
        SharedPreferences prefs = context.getSharedPreferences(Const.PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int fitness = prefs.getInt(Const.FITNESS, -1) - 1;
        int hunger = prefs.getInt(Const.HUNGER, -1) - 1;
        int cleanliness = prefs.getInt(Const.CLEANLINESS, -1) - 1;
        editor.putInt(Const.FITNESS, fitness);
        editor.putInt(Const.HUNGER, hunger);
        editor.putInt(Const.CLEANLINESS, cleanliness);
        editor.apply();

        context.sendBroadcast(new Intent(Const.UPDATE_PET));
    }
}

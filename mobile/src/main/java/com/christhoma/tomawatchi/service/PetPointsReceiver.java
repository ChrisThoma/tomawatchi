package com.christhoma.tomawatchi.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
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
        context.startService(new Intent(context, UpdateStatsService.class));
    }
}

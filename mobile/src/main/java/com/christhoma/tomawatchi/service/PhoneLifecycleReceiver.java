package com.christhoma.tomawatchi.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.christhoma.tomawatchi.Const;

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

        } else if (intent.getAction() == Intent.ACTION_SHUTDOWN || intent.getAction() == "android.intent.action.QUICKBOOT_POWEROFF") {
            Date now = new Date();
            editor.putLong(Const.SHUT_DOWN_TIME, now.getTime());
        }
    }
}

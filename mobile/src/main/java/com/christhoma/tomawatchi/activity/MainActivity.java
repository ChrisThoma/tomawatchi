package com.christhoma.tomawatchi.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.christhoma.tomawatchi.Const;
import com.christhoma.tomawatchi.R;
import com.christhoma.tomawatchi.api.Tomawatchi;
import com.christhoma.tomawatchi.fragment.PetCreationFragment;
import com.christhoma.tomawatchi.fragment.PetViewFragment;
import com.christhoma.tomawatchi.service.PetPointsReceiver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;


public class MainActivity extends ActionBarActivity {

    //Google fit variables
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    public GoogleApiClient client = null;
    private OnDataPointListener listener;
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";


    public Tomawatchi pet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        if (!getSharedPreferences(Const.PREFS, MODE_PRIVATE).getBoolean(Const.PET_CREATED, false)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_frame, PetCreationFragment.newInstance()).commit();
        } else {
            continueToPetView();
        }

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        buildFitClient();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pet = updatePetStats();

        Log.d("fit", "connecting");
        client.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePetStats();
    }

    public void savePetStats() {
        SharedPreferences prefs = getSharedPreferences(Const.PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Date date = new Date();
        editor.putLong(Const.TIME, date.getTime());
        editor.putString(Const.NAME, pet.name);
        editor.putInt(Const.TOTALSTEPS, pet.totalSteps);
        editor.putInt(Const.FITNESS, pet.fitness);
        editor.putInt(Const.HUNGER, pet.hunger);
        editor.putInt(Const.CLEANLINESS, pet.cleanliness);
        editor.apply();
    }

    public Tomawatchi updatePetStats() {
        if (pet == null) {
            pet = new Tomawatchi();
        }
        SharedPreferences prefs = getSharedPreferences(Const.PREFS, MODE_PRIVATE);
        Date date = new Date();
        Date startDate = new Date(prefs.getLong(Const.STARTDATE, -1));
        pet.name = prefs.getString(Const.NAME, "");
        pet.totalSteps = prefs.getInt(Const.TOTALSTEPS, -1);
        pet.fitness = prefs.getInt(Const.FITNESS, -1);
        pet.hunger = prefs.getInt(Const.HUNGER, -1);
        pet.cleanliness = prefs.getInt(Const.CLEANLINESS, -1);

        long startDateDifference = date.getTime() - startDate.getTime();
        int daysAlive = (int)((((startDateDifference / 1000) / 60) / 60) / 24);
        if (daysAlive < 1) {
            pet.age = Tomawatchi.Age.BABY;
        }
        else if (daysAlive < 5) {
            pet.age = Tomawatchi.Age.TEEN;
        }
        else if (daysAlive < 8) {
            pet.age = Tomawatchi.Age.ADULT;
        }
        else if (daysAlive < 12) {
            pet.age = Tomawatchi.Age.ELDERLY;
        }
        else if (daysAlive < 15) {
            pet.age = Tomawatchi.Age.DEDZO;
        } else {
            pet.age = Tomawatchi.Age.DEDZO;
        }
        return pet;
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (client.isConnected()) {
            client.disconnect();
        }
    }

    public void buildFitClient() {
        client = new GoogleApiClient.Builder(this)
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.d("fit", "connected");
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                Log.d("fit", "connection suspended");
                            }
                        }
                )
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d("fit", "failed connection");
                        Log.d("fit", connectionResult.toString());
                        if (!connectionResult.hasResolution()) {
                            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), MainActivity.this, 0).show();
                            return;
                        }
                        //if failure has a resolution, resolve it
                        if (!authInProgress) {
                            try {
                                Log.d("fit", "attempting to resolve issue");
                                authInProgress = false;
                                connectionResult.startResolutionForResult(MainActivity.this, REQUEST_OAUTH);
                            } catch (IntentSender.SendIntentException e) {
                                Log.d("fit", "exception while starting activity", e);
                            }
                        }
                    }
                }).build();
    }

    public void continueToPetView() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_frame, PetViewFragment.newInstance()).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                if (!client.isConnecting() && !client.isConnected()) {
                    client.connect();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

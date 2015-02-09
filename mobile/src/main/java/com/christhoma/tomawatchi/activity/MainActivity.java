package com.christhoma.tomawatchi.activity;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.christhoma.tomawatchi.R;
import com.christhoma.tomawatchi.api.Tomawatchi;
import com.christhoma.tomawatchi.fragment.PetViewFragment;
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


public class MainActivity extends ActionBarActivity  {

    //Google fit variables
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    public GoogleApiClient client = null;
    private OnDataPointListener listener;
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";

    public Tomawatchi pet;

    public static String TAG = "fit";
    public static String PREFS = "watchagatchi";
    public static String TIME =  "pettime";
    public static String HUNGER = "pethunger";
    public static String FITNESS = "petfitness";
    public static String TOTALSTEPS = "totalsteps";
    public static String NAME = "petname";
    public static String AGE = "petage";
    public static String OPENED = "opened";
    public static String STARTDATE = "startDate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        if (!getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(OPENED, false)) {
            pet = new Tomawatchi();
            pet.hunger = 90;
            pet.fitness = 100;
            pet.cleanliness = 85;
            pet.age = Tomawatchi.Age.BABY;
            pet.name = "ok";
            pet.overallHappiness = pet.getOverallHappiness();
            pet.startDate = new Date().getTime();
            savePetStats();
        }

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        buildFitClient();

        PetViewFragment petViewFragment = new PetViewFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_frame, petViewFragment).commit();
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(OPENED, true).apply();
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
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Date date = new Date();
        editor.putLong(TIME, date.getTime());
        editor.putString(NAME, pet.name);
        editor.putInt(TOTALSTEPS, pet.totalSteps);
        editor.putInt(FITNESS, pet.fitness);
        editor.putInt(HUNGER, pet.hunger);
        editor.apply();
    }

    public Tomawatchi updatePetStats() {
        if (pet == null) {
            pet = new Tomawatchi();
        }
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        Date date = new Date();
        Date oldDate = new Date(prefs.getLong(TIME, -1));
        Date startDate = new Date(prefs.getLong(STARTDATE, -1));
        pet.name = prefs.getString(NAME, "");
        pet.totalSteps = prefs.getInt(TOTALSTEPS, -1);
        pet.fitness = prefs.getInt(FITNESS, -1);
        pet.hunger = prefs.getInt(HUNGER, -1);

        long startDateDifference = date.getTime() - startDate.getTime();
        long difference = date.getTime() - oldDate.getTime();
        long hoursDifference = ((((difference / 1000) / 60)) / 60);
        int daysAlive = (int)((((startDateDifference / 1000) / 60) / 60) / 24);
        pet.fitness = (int)(pet.fitness - (hoursDifference * 1));
        pet.hunger = (int)(pet.hunger - (hoursDifference * 1));
        pet.overallHappiness = pet.getOverallHappiness();

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

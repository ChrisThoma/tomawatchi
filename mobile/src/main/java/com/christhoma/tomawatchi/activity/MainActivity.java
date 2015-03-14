package com.christhoma.tomawatchi.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.christhoma.tomawatchi.Const;
import com.christhoma.tomawatchi.R;
import com.christhoma.tomawatchi.api.AverageLoader;
import com.christhoma.tomawatchi.api.HistoryLoader;
import com.christhoma.tomawatchi.api.Tomawatchi;
import com.christhoma.tomawatchi.fragment.PetCreationFragment;
import com.christhoma.tomawatchi.fragment.PetViewFragment;
import com.christhoma.tomawatchi.service.PetPointsReceiver;
import com.christhoma.tomawatchi.service.RestartTimerService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.result.DataReadResult;

import org.joda.time.Hours;
import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;


public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<DataReadResult> {

    //Google fit variables
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    public GoogleApiClient client = null;
    private OnDataPointListener listener;
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    public LoaderManager.LoaderCallbacks<DataReadResult> stepsWhenCreatedLoader = new LoaderManager.LoaderCallbacks<DataReadResult>() {
        @Override
        public Loader<DataReadResult> onCreateLoader(int id, Bundle args) {
            return new HistoryLoader(MainActivity.this, client);
        }

        @Override
        public void onLoadFinished(Loader<DataReadResult> loader, DataReadResult data) {
            if (data != null) {
                printDataCreationSteps(data);
            } else {
                Toast.makeText(MainActivity.this, "failed to load data", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLoaderReset(Loader<DataReadResult> loader) {
            //nothing
        }
    };

    public LoaderManager.LoaderCallbacks<DataReadResult> avgStepsLoader = new LoaderManager.LoaderCallbacks<DataReadResult>() {
        @Override
        public Loader<DataReadResult> onCreateLoader(int id, Bundle args) {
            return new AverageLoader(MainActivity.this, client);
        }

        @Override
        public void onLoadFinished(Loader<DataReadResult> loader, DataReadResult data) {
            if (data != null) {
                printDataAverageSteps(data);
            } else {
                Toast.makeText(MainActivity.this, "failed to load data", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLoaderReset(Loader<DataReadResult> loader) {
            //nothing
        }
    };

    public Tomawatchi pet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        getSupportActionBar().hide();
        prefs = getSharedPreferences(Const.PREFS, Context.MODE_PRIVATE);
        editor = prefs.edit();
        if (!prefs.getBoolean(Const.PET_CREATED, false)) {
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

        Log.d("fit", "connecting");
        if (prefs.getBoolean(Const.PET_CREATED, false)) {
            LocalDate now = new LocalDate();
            LocalDate then = new LocalDate(prefs.getLong(Const.LAST_UPDATE_TIME, -1) * 10);
            int hoursBetween = Hours.hoursBetween(now, then).getHours();
            if (hoursBetween > 1) {
                startService(new Intent(this, RestartTimerService.class).putExtra(Const.UPDATES_MISSED, hoursBetween));
            }
            pet = updatePetStats();
            client.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (prefs.getBoolean(Const.PET_CREATED, false)) {
            savePetStats();
        }
    }

    public void savePetStats() {
        Date date = new Date();
        editor.putString(Const.NAME, pet.name);
        editor.putInt(Const.TOTALSTEPS, pet.totalSteps);
        editor.putInt(Const.FITNESS, pet.fitness);
        editor.putInt(Const.HUNGER, pet.hunger);
        editor.putInt(Const.CLEANLINESS, pet.cleanliness);
        editor.putInt(Const.TODAY_STEPS, pet.todaysSteps);
        editor.apply();
    }

    public Tomawatchi updatePetStats() {
        if (pet == null) {
            pet = new Tomawatchi();
        }
        Date date = new Date();
        Date startDate = new Date(prefs.getLong(Const.STARTDATE, -1));
        pet.name = prefs.getString(Const.NAME, "");
        pet.todaysSteps = prefs.getInt(Const.TODAY_STEPS, 0);
        pet.fitness = prefs.getInt(Const.FITNESS, -1);
        pet.hunger = prefs.getInt(Const.HUNGER, -1);
        pet.cleanliness = prefs.getInt(Const.CLEANLINESS, -1);
        pet.originalSteps = prefs.getInt(Const.TOTAL_STEPS_AT_CREATION, -1);

        long startDateDifference = date.getTime() - startDate.getTime();
        int daysAlive = (int) ((((startDateDifference / 1000) / 60) / 60) / 24);
        if (daysAlive <= 0) {
            int minutesAlive = (int) (((startDateDifference / 1000) / 60));
            if (minutesAlive <= 2) {
                pet.age = Tomawatchi.Age.EGG;
            } else {
                pet.age = Tomawatchi.Age.BABY;
            }
        } else if (daysAlive < 2) {
            pet.age = Tomawatchi.Age.BABY;
        } else if (daysAlive < 9) {
            pet.age = Tomawatchi.Age.TEEN;
        } else if (daysAlive < 16) {
            pet.age = Tomawatchi.Age.ADULT;
        } else if (daysAlive < 32) {
            pet.age = Tomawatchi.Age.ELDERLY;
        } else {
            pet.age = Tomawatchi.Age.ELDERLY;
        }
        getSupportLoaderManager().restartLoader(0, null, this);
        getSupportLoaderManager().restartLoader(43, null, avgStepsLoader);
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
                .replace(R.id.fragment_frame, PetViewFragment.newInstance(), PetViewFragment.PET_FRAGMENT_TAG).commit();
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

    @Override
    public Loader<DataReadResult> onCreateLoader(int id, Bundle args) {
        return new HistoryLoader(this, client);
    }

    public static PetViewFragment newInstance() {
        return new PetViewFragment();
    }

    @Override
    public void onLoadFinished(Loader<DataReadResult> loader, DataReadResult data) {
        if (data != null) {

        } else {
            Toast.makeText(this, "failed to load data", Toast.LENGTH_SHORT).show();
        }
        printData(data);
    }

    @Override
    public void onLoaderReset(Loader<DataReadResult> loader) {
        //nothing
    }

    private void printData(DataReadResult dataReadResult) {
        if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
    }

    private void dumpDataSet(DataSet dataSet) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        int previousTotalSteps = pet.totalSteps;
        int totalSteps = 0;
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                if (dp.getValue(field).asInt() > 0) {
                    totalSteps = dp.getValue(field).asInt();
                }
            }
        }
        pet.totalSteps = totalSteps;
        if (previousTotalSteps > 0) {
            int stepPoints = (totalSteps - previousTotalSteps) / 1000;
            if (stepPoints > 0) {
                stepPoints *= 5;
                if (pet.fitness + stepPoints > 100) {
                    pet.fitness = 100;
                } else {
                    pet.fitness += stepPoints;
                }
                pet.fitness += stepPoints;
                editor.putInt(Const.TOTALSTEPS, pet.totalSteps).apply();
            }
        } else {
            editor.putInt(Const.TOTALSTEPS, pet.totalSteps).apply();
        }
        PetViewFragment fragment = (PetViewFragment) getSupportFragmentManager().findFragmentByTag(PetViewFragment.PET_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.refreshContent(pet);
        }
    }

    private void printDataCreationSteps(DataReadResult dataReadResult) {
        if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSetCreationSteps(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSetCreationSteps(dataSet);
            }
        }
    }

    private void dumpDataSetCreationSteps(DataSet dataSet) {
        int totalSteps = 0;
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                if (dp.getValue(field).asInt() > 0) {
                    totalSteps = dp.getValue(field).asInt();
                }
            }
        }
        editor.putInt(Const.TOTAL_STEPS_AT_CREATION, totalSteps).apply();
    }

    private void printDataAverageSteps(DataReadResult dataReadResult) {
        int bucketCount = 0;
        int steps = 0;
        int avgSteps = 0;
        if (dataReadResult.getBuckets().size() > 0) {
            bucketCount = dataReadResult.getBuckets().size();
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    steps += dumpDataSetAverageSteps(dataSet);
                }
            }
            avgSteps = (steps / bucketCount);
        } else if (dataReadResult.getDataSets().size() > 0) {
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                avgSteps += dumpDataSetAverageSteps(dataSet);
            }
        }
        editor.putInt(Const.AVERAGE_STEPS, avgSteps).apply();
    }

    private int dumpDataSetAverageSteps(DataSet dataSet) {
        int steps = 0;
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                if (dp.getValue(field).asInt() > 0) {
                    steps += dp.getValue(field).asInt();
                }
            }
        }
        return steps;
    }
}

package com.christhoma.tomawatchi.fragment;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.christhoma.tomawatchi.Const;
import com.christhoma.tomawatchi.R;
import com.christhoma.tomawatchi.activity.MainActivity;
import com.christhoma.tomawatchi.activity.OtherActivity;
import com.christhoma.tomawatchi.api.HistoryLoader;
import com.christhoma.tomawatchi.api.Tomawatchi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DataReadResult;
import com.melnykov.fab.FloatingActionButton;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by christhoma on 1/31/15.
 */
public class PetViewFragment extends Fragment implements LoaderManager.LoaderCallbacks<DataReadResult> {

    //Views
    @InjectView(R.id.gif)
    GifImageView gifImageView;
    @InjectView(R.id.feed_button)
    FloatingActionButton feedButton;
    @InjectView(R.id.clean_button)
    FloatingActionButton cleanButton;
    @InjectView(R.id.arrow_button)
    FloatingActionButton arrowButton;
    @InjectView(R.id.notification_button)
    Button notificationButton;
    @InjectView(R.id.step_count)
    TextView stepCount;
    @InjectView(R.id.happiness_progress)
    ProgressBar happiness;
    @InjectView(R.id.fitness_progress)
    ProgressBar fitness;
    @InjectView(R.id.hunger_progress)
    ProgressBar hunger;
    @InjectView(R.id.loading_progres)
    ProgressBar loadingProgress;
    @InjectView(R.id.progress_layout)
    LinearLayout progressLayout;

    //FitData
    public static String TAG = "fit";
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";
    DataReadResult dataReadResult;
    public GoogleApiClient client = null;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshProgressBars();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        IntentFilter iF = new IntentFilter();
        iF.addAction(Const.UPDATE_PET);
        getActivity().registerReceiver(broadcastReceiver, iF);
        Tomawatchi tomawatchi = ((MainActivity)getActivity()).pet;
        hunger.setProgress(tomawatchi.hunger);
        fitness.setProgress(tomawatchi.fitness);
        happiness.setProgress(tomawatchi.overallHappiness);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pet_view, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Intent openMain = new Intent(getActivity(), OtherActivity.class);
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.drawable.bulby)
                .setContentTitle("TITLE")
                .setContentText("TEXT")
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_menu_send, "DO A THING", PendingIntent.getActivity(getActivity(), 1, openMain, PendingIntent.FLAG_CANCEL_CURRENT));

        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ONCLICK", "ONCLICK");
                notificationManager.notify(1, notificationBuilder.build());
            }
        });
        gifImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                refreshProgressBars();
                return true;
            }
        });
        feedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hunger = ((MainActivity)getActivity()).pet.hunger;
                if (hunger + 25 > 100) {
                    hunger = 100;
                } else {
                    ((MainActivity)getActivity()).pet.hunger += 25;
                }
                ((MainActivity)getActivity()).savePetStats();
                refreshProgressBars();
            }
        });
        stepCount.setText("0000");
        stepCount.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                refreshFitData();
                return true;
            }
        });
        if (getActivity() != null) {
            client = ((MainActivity)getActivity()).client;
        }

        refreshFitData();

        super.onViewCreated(view, savedInstanceState);
    }

    public void refreshFitData() {
        setContentVisible(false);
        getActivity().getSupportLoaderManager().restartLoader(1, null, this);
    }

    public void refreshProgressBars() {
        ((MainActivity)getActivity()).updatePetStats();
        Tomawatchi pet = ((MainActivity)getActivity()).pet;
        fitness.setProgress(pet.fitness);
        hunger.setProgress(pet.hunger);
        happiness.setProgress(pet.getOverallHappiness());
    }

    public void setContentVisible(boolean contentVisible) {
        if (contentVisible) {
            progressLayout.setVisibility(View.VISIBLE);
            gifImageView.setVisibility(View.VISIBLE);
            notificationButton.setVisibility(View.VISIBLE);
            stepCount.setVisibility(View.VISIBLE);
            feedButton.setVisibility(View.VISIBLE);
            cleanButton.setVisibility(View.VISIBLE);
            arrowButton.setVisibility(View.VISIBLE);
            loadingProgress.setVisibility(View.GONE);
        } else {
            progressLayout.setVisibility(View.GONE);
            gifImageView.setVisibility(View.GONE);
            notificationButton.setVisibility(View.GONE);
            stepCount.setVisibility(View.GONE);
            feedButton.setVisibility(View.GONE);
            cleanButton.setVisibility(View.GONE);
            arrowButton.setVisibility(View.GONE);
            loadingProgress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Loader<DataReadResult> onCreateLoader(int id, Bundle args) {
        return new HistoryLoader(getActivity(), client);
    }

    public static PetViewFragment newInstance() {
        return new PetViewFragment();
    }

    @Override
    public void onLoadFinished(Loader<DataReadResult> loader, DataReadResult data) {
        if (data != null) {
            setContentVisible(true);
        } else {
            setContentVisible(false);
            Toast.makeText(getActivity(), "failed to load data", Toast.LENGTH_SHORT).show();
        }
        this.dataReadResult = data;
        printData(dataReadResult);
    }

    private void printData(DataReadResult dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i("fit", "Number of returned buckets of DataSets is: "
                    + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i("fit", "Number of returned DataSets is: "
                    + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
        // [END parse_read_data_result]
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
                stepCount.setText("" + dp.getValue(field));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<DataReadResult> loader) {

    }
}

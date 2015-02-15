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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.christhoma.tomawatchi.Const;
import com.christhoma.tomawatchi.R;
import com.christhoma.tomawatchi.activity.MainActivity;
import com.christhoma.tomawatchi.activity.OtherActivity;
import com.christhoma.tomawatchi.activity.PetDetailActivity;
import com.christhoma.tomawatchi.api.SingleDayLoader;
import com.christhoma.tomawatchi.api.Tomawatchi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DataReadResult;
import com.melnykov.fab.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by christhoma on 1/31/15.
 */
public class PetViewFragment extends Fragment implements LoaderManager.LoaderCallbacks<DataReadResult> {

    public static String PET_FRAGMENT_TAG = "PETFRAGMENTTAG";
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
    @InjectView(R.id.pet_name_text)
    TextView petName;
    @InjectView(R.id.number_of_steps)
    TextView numberOfSteps;
    @InjectView(R.id.progress)
    ProgressBar loadingProgress;

    //FitData
    public static String TAG = "fit";
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";
    DataReadResult dataReadResult;
    public GoogleApiClient client = null;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshFitData();
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
        Tomawatchi tomawatchi = ((MainActivity) getActivity()).pet;
        petName.setText(tomawatchi.name);
        if (getActivity() != null) {
            client = ((MainActivity) getActivity()).client;
        }
        refreshFitData();
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
        feedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hunger = ((MainActivity) getActivity()).pet.hunger;
                if (hunger + 25 > 100) {
                    hunger = 100;
                } else {
                    ((MainActivity) getActivity()).pet.hunger += 25;
                }
                ((MainActivity) getActivity()).savePetStats();
                refreshFitData();
            }
        });
        cleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cleanliness = ((MainActivity) getActivity()).pet.cleanliness;
                if (cleanliness + 25 > 100) {
                    cleanliness = 100;
                } else {
                    ((MainActivity) getActivity()).pet.cleanliness += 25;
                }
                ((MainActivity) getActivity()).savePetStats();
                refreshFitData();
            }
        });
        arrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PetDetailActivity.class));
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    public void refreshFitData() {
        setContentVisible(false);
        updateStats();
        getActivity().getSupportLoaderManager().restartLoader(1, null, this);
    }

    public void updateStats() {
        ((MainActivity) getActivity()).updatePetStats();
        setContentVisible(false);
    }

    public void refreshContent(Tomawatchi pet) {
        setContentVisible(true);
        //update UI here
    }

    public void setContentVisible(boolean contentVisible) {
        if (contentVisible) {
            gifImageView.setVisibility(View.VISIBLE);
            notificationButton.setVisibility(View.VISIBLE);
            feedButton.setVisibility(View.VISIBLE);
            cleanButton.setVisibility(View.VISIBLE);
            arrowButton.setVisibility(View.VISIBLE);
            loadingProgress.setVisibility(View.GONE);
        } else {
//            gifImageView.setVisibility(View.GONE);
            notificationButton.setVisibility(View.GONE);
            feedButton.setVisibility(View.GONE);
            cleanButton.setVisibility(View.GONE);
            arrowButton.setVisibility(View.GONE);
            loadingProgress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Loader<DataReadResult> onCreateLoader(int id, Bundle args) {
        return new SingleDayLoader(getActivity(), client);
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

        int todaysSteps = 0;
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                numberOfSteps.setText("" + dp.getValue(field) + " steps");
                todaysSteps = dp.getValue(field).asInt();
            }
        }
        ((MainActivity) getActivity()).pet.todaysSteps = todaysSteps;
    }

    @Override
    public void onLoaderReset(Loader<DataReadResult> loader) {

    }
}

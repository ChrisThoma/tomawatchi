package com.christhoma.tomawatchi.fragment;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.christhoma.tomawatchi.service.PetCareService;
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
    @InjectView(R.id.pet_health_layout)
    LinearLayout healthLayout;
    @InjectView(R.id.pet_cleanliness_layout)
    LinearLayout cleanlinessLayout;
    @InjectView(R.id.pet_hunger_layout)
    LinearLayout hungerLayout;
    @InjectView(R.id.clean_animation_view)
    View cleanAnimationView;
    @InjectView(R.id.food_animation_view)
    View foodAnimationView;

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
        if (getActivity() != null) {
            client = ((MainActivity) getActivity()).client;
            refreshFitData();
        }
        IntentFilter iF = new IntentFilter();
        iF.addAction(Const.UPDATE_PET);
        getActivity().registerReceiver(broadcastReceiver, iF);
        Tomawatchi tomawatchi = ((MainActivity) getActivity()).pet;
        petName.setText(tomawatchi.name);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pet_view, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Intent openMain = new Intent(getActivity(), PetCareService.class);
        openMain.putExtra(Const.PET_CARE_EXTRA_HUNGER, Const.HUNGER);
        openMain.putExtra(Const.PET_CARE_EXTRA_CLEANLINESS, Const.CLEANLINESS);
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Feed")
                .setContentText("Feed your pet")
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.bg_food);
        NotificationCompat.Action feedAction = new NotificationCompat.Action(R.drawable.ic_feed, "Feed me!", PendingIntent.getService(getActivity(), Const.PET_CARE_ID+1, openMain, PendingIntent.FLAG_CANCEL_CURRENT));
        NotificationCompat.Action cleanAction = new NotificationCompat.Action(R.drawable.ic_clean, "Bathe me!", PendingIntent.getService(getActivity(), Const.PET_CARE_ID+2, openMain, PendingIntent.FLAG_CANCEL_CURRENT));
        notificationBuilder.extend(new NotificationCompat.WearableExtender()
                .setBackground(icon)
                .addAction(feedAction)
                .addAction(cleanAction)
        );

        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ONCLICK", "ONCLICK");
                notificationManager.notify(Const.PET_CARE_ID, notificationBuilder.build());
            }
        });

        final Animation foodAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.right_appear_animation);
        foodAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                foodAnimationView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        feedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foodAnimationView.setVisibility(View.VISIBLE);
                foodAnimationView.startAnimation(foodAnimation);
                int hunger = ((MainActivity) getActivity()).pet.hunger;
                if (hunger + 25 > 100) {
                    ((MainActivity) getActivity()).pet.hunger = 100;
                } else {
                    ((MainActivity) getActivity()).pet.hunger += 25;
                }
                ((MainActivity) getActivity()).savePetStats();
                refreshFitData();
            }
        });

        final Animation cleanAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.left_appear_animation);
        cleanAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cleanAnimationView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        cleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanAnimationView.setVisibility(View.VISIBLE);
                cleanAnimationView.startAnimation(cleanAnimation);
                int cleanliness = ((MainActivity) getActivity()).pet.cleanliness;
                if (cleanliness + 25 > 100) {
                    ((MainActivity) getActivity()).pet.cleanliness = 100;
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
//        setContentVisible(false);
        updateStats();
        getActivity().getSupportLoaderManager().restartLoader(1, null, this);
    }

    public void updateStats() {
        ((MainActivity) getActivity()).updatePetStats();
//        setContentVisible(false);
    }

    public void refreshContent(Tomawatchi pet) {
        healthLayout.removeAllViews();
        hungerLayout.removeAllViews();
        cleanlinessLayout.removeAllViews();

        //update UI here
        int fullImageCount = 0;
        if (pet.fitness >= 75) {
            fullImageCount = 4;
        } else if (pet.fitness >= 50) {
            fullImageCount = 3;
        } else if (pet.fitness >= 25) {
            fullImageCount = 2;
        } else if (pet.fitness > 0) {
            fullImageCount = 1;
        } else {
            fullImageCount = 0;
        }
        int emptyImageCount = 4 - fullImageCount;

        for (int i = 0; i < fullImageCount; i++) {
            ImageView fullHeart = new ImageView(getActivity());
            fullHeart.setVisibility(View.VISIBLE);
            fullHeart.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_full));
            fullHeart.setPadding(4, 0, 4, 0);
            healthLayout.addView(fullHeart);
        }
        for (int j = 0; j < emptyImageCount; j++) {
            ImageView emptyHeart = new ImageView(getActivity());
            emptyHeart.setVisibility(View.VISIBLE);
            emptyHeart.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_outline));
            emptyHeart.setPadding(4, 0, 4, 0);
            healthLayout.addView(emptyHeart);
        }

        if (pet.hunger >= 75) {
            fullImageCount = 4;
        } else if (pet.hunger >= 50) {
            fullImageCount = 3;
        } else if (pet.hunger >= 25) {
            fullImageCount = 2;
        } else if (pet.hunger > 0) {
            fullImageCount = 1;
        } else {
            fullImageCount = 0;
        }
        emptyImageCount = 4 - fullImageCount;
        for (int i = 0; i < fullImageCount; i++) {
            ImageView fullHeart = new ImageView(getActivity());
            fullHeart.setVisibility(View.VISIBLE);
            fullHeart.setImageDrawable(getResources().getDrawable(R.drawable.ic_food_full));
            fullHeart.setPadding(4, 0, 4, 0);
            hungerLayout.addView(fullHeart);
        }
        for (int j = 0; j < emptyImageCount; j++) {
            ImageView emptyHeart = new ImageView(getActivity());
            emptyHeart.setVisibility(View.VISIBLE);
            emptyHeart.setImageDrawable(getResources().getDrawable(R.drawable.ic_food_outline));
            emptyHeart.setPadding(4, 0, 4, 0);
            hungerLayout.addView(emptyHeart);
        }

        if (pet.cleanliness >= 75) {
            fullImageCount = 4;
        } else if (pet.cleanliness >= 50) {
            fullImageCount = 3;
        } else if (pet.cleanliness >= 25) {
            fullImageCount = 2;
        } else if (pet.cleanliness > 0) {
            fullImageCount = 1;
        } else {
            fullImageCount = 0;
        }
        emptyImageCount = 4 - fullImageCount;
        for (int i = 0; i < fullImageCount; i++) {
            ImageView fullHeart = new ImageView(getActivity());
            fullHeart.setVisibility(View.VISIBLE);
            fullHeart.setImageDrawable(getResources().getDrawable(R.drawable.ic_clean_full));
            fullHeart.setPadding(4, 0, 4, 0);
            cleanlinessLayout.addView(fullHeart);
        }
        for (int j = 0; j < emptyImageCount; j++) {
            ImageView emptyHeart = new ImageView(getActivity());
            emptyHeart.setVisibility(View.VISIBLE);
            emptyHeart.setImageDrawable(getResources().getDrawable(R.drawable.ic_clean_outline));
            emptyHeart.setPadding(4, 0, 4, 0);
            cleanlinessLayout.addView(emptyHeart);
        }


        if (pet.age == Tomawatchi.Age.BABY) {
            gifImageView.setImageDrawable(getResources().getDrawable(R.drawable.character_baby));
        } else if (pet.age == Tomawatchi.Age.TEEN) {
            gifImageView.setImageDrawable(getResources().getDrawable(R.drawable.character_child));
        } else if (pet.age == Tomawatchi.Age.ADULT) {
            gifImageView.setImageDrawable(getResources().getDrawable(R.drawable.character_teen));
        } else if (pet.age == Tomawatchi.Age.ELDERLY) {
            gifImageView.setImageDrawable(getResources().getDrawable(R.drawable.character_adult));
        }
        setContentVisible(true);
    }

    public void setContentVisible(boolean contentVisible) {
        if (contentVisible) {
            gifImageView.setVisibility(View.VISIBLE);
//            notificationButton.setVisibility(View.VISIBLE);
            healthLayout.setVisibility(View.VISIBLE);
            hungerLayout.setVisibility(View.VISIBLE);
            cleanlinessLayout.setVisibility(View.VISIBLE);
            feedButton.setVisibility(View.VISIBLE);
            cleanButton.setVisibility(View.VISIBLE);
            arrowButton.setVisibility(View.VISIBLE);
            loadingProgress.setVisibility(View.GONE);
            numberOfSteps.setVisibility(View.VISIBLE);
        } else {
//            gifImageView.setVisibility(View.GONE);
//            notificationButton.setVisibility(View.GONE);
            healthLayout.removeAllViews();
            hungerLayout.removeAllViews();
            cleanlinessLayout.removeAllViews();
            feedButton.setVisibility(View.GONE);
            cleanButton.setVisibility(View.GONE);
            arrowButton.setVisibility(View.GONE);
            numberOfSteps.setVisibility(View.INVISIBLE);
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
                todaysSteps = dp.getValue(field).asInt();
            }
        }
        numberOfSteps.setText(todaysSteps +  " steps");
        ((MainActivity) getActivity()).pet.todaysSteps = todaysSteps;
    }

    @Override
    public void onLoaderReset(Loader<DataReadResult> loader) {

    }
}

package com.christhoma.tomawatchi.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.christhoma.tomawatchi.Const;
import com.christhoma.tomawatchi.R;
import com.christhoma.tomawatchi.api.Tomawatchi;

import org.joda.time.LocalDate;
import org.joda.time.Months;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PetDetailActivity extends ActionBarActivity {

    @InjectView(R.id.detail_pet_name)
    TextView petName;
    @InjectView(R.id.detail_pet_birthday)
    TextView petBirhday;
    @InjectView(R.id.detail_average_steps)
    TextView averageSteps;
    @InjectView(R.id.detail_today_steps)
    TextView todaySteps;
    @InjectView(R.id.detail_total_steps)
    TextView totalSteps;
    @InjectView(R.id.detail_health_average_layout)
    LinearLayout averageHealthLayout;
    @InjectView(R.id.detail_health_today_layout)
    LinearLayout todayHealthLayout;

    Tomawatchi pet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_detail);

        ButterKnife.inject(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setIcon(R.drawable.text_title_bar_tamawatchi);

        if (pet == null) {
            pet = new Tomawatchi();
        }
        SharedPreferences prefs = getSharedPreferences(Const.PREFS, MODE_PRIVATE);
        Date date = new Date();
        Date startDate = new Date(prefs.getLong(Const.STARTDATE, -1));
        pet.name = prefs.getString(Const.NAME, "");
        pet.fitness = prefs.getInt(Const.FITNESS, -1);
        pet.hunger = prefs.getInt(Const.HUNGER, -1);
        pet.cleanliness = prefs.getInt(Const.CLEANLINESS, -1);
        pet.overallHappiness = pet.getOverallHappiness();
        pet.totalSteps = prefs.getInt(Const.TOTALSTEPS, -1);
        pet.todaysSteps = prefs.getInt(Const.TODAY_STEPS, -1);
        pet.startDate = startDate.getTime();
        pet.originalSteps = prefs.getInt(Const.TOTAL_STEPS_AT_CREATION, -1);
        int avgStepsCount = prefs.getInt(Const.AVERAGE_STEPS, -1);
        setHealthIcons(avgStepsCount);

        totalSteps.setText("" + (pet.totalSteps - pet.originalSteps) + " steps");
        petName.setText(pet.name);

        LocalDate birthday = new LocalDate(pet.startDate);
        LocalDate now = new LocalDate();
        Months monthsSince = Months.monthsBetween(birthday, now);
        petBirhday.setText("" + birthday.toString("MMMM dd, yyyy") + " / " + monthsSince.getMonths() + " Months");
        averageSteps.setText("" + avgStepsCount);
        todaySteps.setText("" + pet.todaysSteps);

    }

    public void setHealthIcons(int averageStepsCount) {
        int fullHeartsCount = 0;
        if (pet.fitness >= 75) {
            fullHeartsCount = 4;
        } else if (pet.fitness >= 50) {
            fullHeartsCount = 3;
        } else if (pet.fitness >= 25) {
            fullHeartsCount = 2;
        } else if (pet.fitness > 0) {
            fullHeartsCount = 1;
        } else {
            fullHeartsCount = 0;
        }
        int emptyHeartsCount = 4 - fullHeartsCount;
        for (int i = 0; i < fullHeartsCount; i++) {
            ImageView fullHeart = new ImageView(this);
            fullHeart.setVisibility(View.VISIBLE);
            fullHeart.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_full));
            fullHeart.setPadding(4, 0, 4, 0);
            todayHealthLayout.addView(fullHeart);
        }
        for (int j = 0; j < emptyHeartsCount; j++) {
            ImageView emptyHeart = new ImageView(this);
            emptyHeart.setVisibility(View.VISIBLE);
            emptyHeart.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_outline));
            emptyHeart.setPadding(4, 0, 4, 0);
            todayHealthLayout.addView(emptyHeart);
        }

        if (averageStepsCount >= 3750) {
            fullHeartsCount = 4;
        } else if (averageStepsCount >= 2500) {
            fullHeartsCount = 3;
        } else if (averageStepsCount >= 1250) {
            fullHeartsCount = 2;
        } else if (averageStepsCount > 0) {
            fullHeartsCount = 1;
        } else {
            fullHeartsCount = 0;
        }
        emptyHeartsCount = 4 - fullHeartsCount;
        for (int i = 0; i < fullHeartsCount; i++) {
            ImageView fullHeart = new ImageView(this);
            fullHeart.setVisibility(View.VISIBLE);
            fullHeart.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_full));
            fullHeart.setPadding(4, 0, 4, 0);
            averageHealthLayout.addView(fullHeart);
        }
        for (int j = 0; j < emptyHeartsCount; j++) {
            ImageView emptyHeart = new ImageView(this);
            emptyHeart.setVisibility(View.VISIBLE);
            emptyHeart.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_outline));
            emptyHeart.setPadding(4, 0, 4, 0);
            averageHealthLayout.addView(emptyHeart);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pet_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}

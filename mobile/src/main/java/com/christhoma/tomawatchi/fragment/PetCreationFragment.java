package com.christhoma.tomawatchi.fragment;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.christhoma.tomawatchi.Const;
import com.christhoma.tomawatchi.R;
import com.christhoma.tomawatchi.activity.MainActivity;
import com.christhoma.tomawatchi.api.Tomawatchi;
import com.christhoma.tomawatchi.service.PetPointsReceiver;

import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import oak.widget.CancelEditText;


public class PetCreationFragment extends Fragment {

    @InjectView(R.id.edit_name)
    CancelEditText editName;
    @InjectView(R.id.done_button)
    Button doneButton;

    public static PetCreationFragment newInstance() {
        PetCreationFragment fragment = new PetCreationFragment();
        return fragment;
    }

    public PetCreationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pet_creation, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tomawatchi pet = new Tomawatchi();
                pet.hunger = 100;
                pet.fitness = 100;
                pet.cleanliness = 100;
                pet.age = Tomawatchi.Age.BABY;
                pet.name = editName.getText().toString();
                pet.overallHappiness = pet.getOverallHappiness();
                pet.startDate = new Date().getTime();

                ((MainActivity)getActivity()).pet = pet;
                ((MainActivity)getActivity()).savePetStats();

                SharedPreferences prefs = getActivity().getSharedPreferences(Const.PREFS, v.getContext().MODE_PRIVATE);
                prefs.edit().putBoolean(Const.PET_CREATED, true).apply();
                prefs.edit().putLong(Const.STARTDATE, pet.startDate);

                ((MainActivity)getActivity()).continueToPetView();

                AlarmManager alarmManager = (AlarmManager) v.getContext().getSystemService(v.getContext().ALARM_SERVICE);
                Intent intent = new Intent(getActivity(), PetPointsReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);
                Calendar calendar = Calendar.getInstance();
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 60 * 60 * 1000, pendingIntent);
            }
        });
    }
}

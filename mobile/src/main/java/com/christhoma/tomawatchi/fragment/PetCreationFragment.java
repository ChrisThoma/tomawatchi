package com.christhoma.tomawatchi.fragment;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.christhoma.tomawatchi.Const;
import com.christhoma.tomawatchi.R;
import com.christhoma.tomawatchi.activity.MainActivity;
import com.christhoma.tomawatchi.api.HistoryLoader;
import com.christhoma.tomawatchi.api.Tomawatchi;
import com.christhoma.tomawatchi.service.PetPointsReceiver;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DataReadResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        editName.setFilters(new InputFilter[]{
                new InputFilter() {
                    public CharSequence filter(CharSequence src, int start,
                                               int end, Spanned dst, int dstart, int dend) {
                        if (src.equals("")) { // for backspace
                            return src;
                        }
                        if (src.toString().matches("[a-zA-Z]+")) {
                            return src;
                        }
                        return "";
                    }
                }
        });
        editName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    // position the text type in the left top corner
                    editName.setGravity(Gravity.LEFT | Gravity.TOP);
                } else {
                    // no text entered. Center the hint text.
                    editName.setGravity(Gravity.CENTER);
                }
            }
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editName.getText().length() > 0) {
                    Tomawatchi pet = new Tomawatchi();

                    pet.hunger = 76;
                    pet.fitness = 76;
                    pet.cleanliness = 101;
                    pet.age = Tomawatchi.Age.BABY;
                    String name = editName.getText().toString().replaceAll(" ", "");
                    pet.name = name;
                    pet.overallHappiness = pet.getOverallHappiness();
                    pet.startDate = new Date().getTime();

                    ((MainActivity) getActivity()).pet = pet;
                    ((MainActivity) getActivity()).savePetStats();

                    SharedPreferences prefs = getActivity().getSharedPreferences(Const.PREFS, v.getContext().MODE_PRIVATE);
                    prefs.edit().putBoolean(Const.PET_CREATED, true).apply();
                    prefs.edit().putLong(Const.STARTDATE, pet.startDate).apply();

                    AlarmManager alarmManager = (AlarmManager) v.getContext().getSystemService(v.getContext().ALARM_SERVICE);
                    Intent intent = new Intent(getActivity(), PetPointsReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);
                    Calendar calendar = Calendar.getInstance();
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 60 * 60 * 1000, pendingIntent);

                    ((MainActivity) getActivity()).client.connect();
                    getActivity().getSupportLoaderManager().restartLoader(9, null, ((MainActivity) getActivity()).stepsWhenCreatedLoader);
                    ((MainActivity) getActivity()).continueToPetView();
                }
            }
        });
    }
}

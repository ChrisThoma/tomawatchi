package com.christhoma.tomawatchi.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.christhoma.tomawatchi.Const;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import oak.util.OakAsyncLoader;

/**
 * Created by christhoma on 2/14/15.
 */
public class AverageLoader extends OakAsyncLoader<DataReadResult> {

    GoogleApiClient mClient = null;
    Context mContext;

    public AverageLoader(Context context, GoogleApiClient client) {
        super(context);
        mContext = context;
        mClient = client;
    }

    protected DataReadRequest queryFitnessData() {
        Calendar cal = Calendar.getInstance();

        SharedPreferences prefs = mContext.getSharedPreferences(Const.PREFS, Context.MODE_PRIVATE);
        long startDate = prefs.getLong(Const.STARTDATE, -1);
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startDate, endTime, TimeUnit.MILLISECONDS)
                .build();

        return readRequest;
    }

    @Override
    public DataReadResult loadInBackground() {
        DataReadRequest readRequest = queryFitnessData();
        DataReadResult dataReadResult =
                Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
        return dataReadResult;
    }
}

package com.ubercalendar.service;

import android.content.Context;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.TaskParams;

/**
 * Created by julian on 6/26/15.
 */
public class TaskService extends GcmTaskService {
    @Override
    public int onRunTask(TaskParams taskParams) {
        return 0;
    }
    public static void oneTimeTask(Context context) {
        long startSecs = 0L; // allow for execution of the task as soon as possible (0s from now)

        long endSecs = startSecs + 3600L; // task will be run within 1 min of start time
        // a unique task identifier
        String tag = "oneoff  | " + ": [" + startSecs + "," + endSecs + "]";

        OneoffTask oneoff = new OneoffTask.Builder()
                .setService(TaskService.class).setTag(tag)
                .setExecutionWindow(startSecs, endSecs)
                .setRequiredNetwork(com.google.android.gms.gcm.Task.NETWORK_STATE_CONNECTED)
                .setRequiresCharging(true)
                .build();
        GcmNetworkManager.getInstance(context).schedule(oneoff);
    }
    public static void periodicTask(Context context) {
        long periodSecs = 30L; // the task should be executed every 30 seconds

        long flexSecs = 15L; // the task can run as early as -15 seconds from the scheduled time

        String tag = "periodic  | " + ": " + periodSecs + "s, f:" + flexSecs;  // a unique task identifier

        PeriodicTask periodic = new PeriodicTask.Builder()
                .setService(TaskService.class)
        .setPeriod(periodSecs)
                .setFlex(flexSecs)
                .setTag(tag)
                .setPersisted(true)
                .setRequiredNetwork(com.google.android.gms.gcm.Task.NETWORK_STATE_ANY)
                .setRequiresCharging(false)
                .build();
        GcmNetworkManager.getInstance(context).schedule(periodic);
    }
}

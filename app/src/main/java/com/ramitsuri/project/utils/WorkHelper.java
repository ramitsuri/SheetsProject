package com.ramitsuri.project.utils;

import com.ramitsuri.expensemanager.Constants;
import com.ramitsuri.expensemanager.work.BackupWorker;
import com.ramitsuri.expensemanager.work.SyncWorker;
import com.ramitsuri.project.Constants;
import com.ramitsuri.project.MainApplication;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import timber.log.Timber;

public class WorkHelper {

    public static void enqueueOneTimeBackup() {
        Timber.i("Enqueue one-time backup invoked");
        String tag = getOneTimeWorkTag();

        Data input = new Data.Builder()
                .putString(Constants.Work.TYPE, Constants.LogType.ONE_TIME_BACKUP)
                .build();

        // Request
        OneTimeWorkRequest backupRequest = new OneTimeWorkRequest
                .Builder(BackupWorker.class)
                .setInputData(input)
                .addTag(tag)
                .build();

        // Enqueue
        getInstance()
                .enqueue(backupRequest);
    }

    /**
     * Will run every 12 hours
     */
    public static void enqueuePeriodicBackup() {
        Timber.i("Enqueue scheduled backup invoked");
        // Prepare
        String tag = getPeriodicWorkTag();
        Constraints constraints = getConstraints();
        Data input = new Data.Builder()
                .putString(Constants.Work.TYPE, Constants.LogType.PERIODIC_BACKUP)
                .build();

        // Request
        PeriodicWorkRequest request = new PeriodicWorkRequest
                .Builder(BackupWorker.class, 1, TimeUnit.DAYS)
                .setInputData(input)
                .addTag(tag)
                .setConstraints(constraints)
                .build();

        // Enqueue
        getInstance()
                .enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.REPLACE, request);
    }

    public static void enqueueOneTimeSync() {
        Timber.i("Enqueue one-time sync invoked");
        String tag = getOneTimeSyncTag();

        Data input = new Data.Builder()
                .putString(Constants.Work.TYPE, Constants.LogType.ONE_TIME_SYNC)
                .build();

        // Request
        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest
                .Builder(SyncWorker.class)
                .setInputData(input)
                .addTag(tag)
                .build();

        // Enqueue
        getInstance()
                .enqueue(syncRequest);
    }

    public static void cancelScheduledBackup() {
        Timber.i("Cancel scheduled backup invoked");
        String tag = getPeriodicWorkTag();
        getInstance().cancelAllWorkByTag(tag);
    }

    public static String getOneTimeWorkTag() {
        return Constants.Tag.ONE_TIME_BACKUP;
    }

    public static String getPeriodicWorkTag() {
        return Constants.Tag.SCHEDULED_BACKUP;
    }

    public static String getOneTimeSyncTag() {
        return Constants.Tag.ONE_TIME_SYNC;
    }

    public static LiveData<List<WorkInfo>> getWorkStatus(String tag) {
        return getInstance()
                .getWorkInfosByTagLiveData(tag);
    }

    private static Constraints getConstraints() {
        return new Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();
    }

    private static WorkManager getInstance() {
        return WorkManager.getInstance(MainApplication.getInstance());
    }
}

package com.ramitsuri.project.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

public class SyncWorker extends BaseWorker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        return Result.success();
    }
}

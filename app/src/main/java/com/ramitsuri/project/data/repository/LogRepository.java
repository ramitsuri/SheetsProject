package com.ramitsuri.project.data.repository;

import com.ramitsuri.project.AppExecutors;
import com.ramitsuri.project.data.Database;
import com.ramitsuri.project.entities.Log;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class LogRepository {

    private AppExecutors mExecutors;
    private Database mDatabase;

    public LogRepository(AppExecutors executors, Database database) {
        mExecutors = executors;
        mDatabase = database;
    }

    public void insertLog(final Log log) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.logDao().insert(log);
            }
        });
    }

    public LiveData<List<Log>> getUnacknowledgedLogs() {
        final MutableLiveData<List<Log>> logs = new MutableLiveData<>();
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Log> values = mDatabase.logDao().getUnacknowledged();
                logs.postValue(values);
            }
        });
        return logs;
    }

    public LiveData<List<Log>> getAllLogs() {
        final MutableLiveData<List<Log>> logs = new MutableLiveData<>();
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Log> values = mDatabase.logDao().getAll();
                logs.postValue(values);
            }
        });
        return logs;
    }

    public void deleteAcknowledged() {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.logDao().deleteAcknowledged();
            }
        });
    }

    public void deleteAll() {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.logDao().deleteAll();
            }
        });
    }
}

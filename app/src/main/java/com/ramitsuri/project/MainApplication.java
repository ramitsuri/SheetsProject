package com.ramitsuri.project;

import android.accounts.Account;
import android.app.Application;
import android.text.TextUtils;

import com.ramitsuri.project.data.Database;
import com.ramitsuri.project.data.repository.LogRepository;
import com.ramitsuri.project.data.repository.SheetRepository;
import com.ramitsuri.project.logging.ReleaseTree;
import com.ramitsuri.project.utils.AppHelper;

import java.util.Arrays;

import timber.log.Timber;

public class MainApplication extends Application {

    private LogRepository mLogRepo;

    private SheetRepository mSheetRepository;

    private static MainApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        initTimber();

        initDataRepos();

        initSheetRepo();
    }

    private void initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ReleaseTree());
        }
    }

    public static MainApplication getInstance() {
        return sInstance;
    }

    private void initDataRepos() {
        AppExecutors appExecutors = AppExecutors.getInstance();
        Database database = Database.getInstance();

        mLogRepo = new LogRepository(appExecutors, database);
    }

    private void initSheetRepo() {
        String spreadsheetId = AppHelper.getSpreadsheetId();
        String accountName = AppHelper.getAccountName();
        String accountType = AppHelper.getAccountType();

        initSheetRepo(spreadsheetId, accountName, accountType);
    }

    public void initSheetRepo(String spreadsheetId, String accountName, String accountType) {
        AppExecutors appExecutors = AppExecutors.getInstance();
        Database database = Database.getInstance();
        String appName = getString(R.string.app_name);

        if (TextUtils.isEmpty(spreadsheetId) ||
                TextUtils.isEmpty(accountName) || TextUtils.isEmpty(accountType)) {
            Timber.i("Spreadsheet Id - %s / Account Name - %s / Account Type - %s null or empty",
                    spreadsheetId, accountName, accountType);
            return;
        }

        Account account = new Account(accountName, accountType);

        mSheetRepository = new SheetRepository(this, appName, account, spreadsheetId,
                Arrays.asList(Constants.SCOPES), appExecutors, database);
    }

    public LogRepository getLogRepo() {
        return mLogRepo;
    }

    public SheetRepository getSheetRepository() {
        return mSheetRepository;
    }
}

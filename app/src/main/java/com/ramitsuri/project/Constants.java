package com.ramitsuri.project;

import com.google.api.services.sheets.v4.SheetsScopes;

public class Constants {

    public static final String[] SCOPES = {SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE};

    public static final int UNDEFINED = -1;

    public class RequestCode {
        public static final int GOOGLE_SIGN_IN = 100;
    }

    public class SystemTheme {
        public static final String LIGHT = "light";
        public static final String DARK = "dark";
        public static final String SYSTEM_DEFAULT = "system_default";
        public static final String BATTERY_SAVER = "battery_saver";
    }

    public class LogResult {
        public static final String SUCCESS = "success";
        public static final String FAILURE = "failure";
    }
}

package com.niko.whatsacc.db;

import android.provider.BaseColumns;

public final class AccountContract {

    public AccountContract() {}

    public static abstract class AccountEntry implements BaseColumns {
        public static final String TABLE_NAME = "accounts";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ACCOUNT = "account";
    }

}

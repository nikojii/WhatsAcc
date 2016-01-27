package com.niko.whatsacc.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AccountDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Accounts.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + AccountContract.AccountEntry.TABLE_NAME + " (" +
        AccountContract.AccountEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
        AccountContract.AccountEntry.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
        AccountContract.AccountEntry.COLUMN_ACCOUNT + TEXT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + AccountContract.AccountEntry.TABLE_NAME;

    public AccountDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP_TABLE_IF_EXISTS " + AccountContract.AccountEntry.TABLE_NAME);
        onCreate(db);
    }

    public void downUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, newVersion, oldVersion);
    }
}

package com.niko.whatsacc;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.niko.whatsacc.db.AccountContract;
import com.niko.whatsacc.db.AccountDBHelper;

public class AddAccountActivity extends AppCompatActivity {

    private String regex = "^[\\w ]+$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    //get the given name and account number, validate and save to db
    public void newAccount(View view) {
        EditText editTextName = (EditText)findViewById(R.id.name);
        EditText editTextNumber = (EditText)findViewById(R.id.number);

        String name = editTextName.getText().toString();
        String number = editTextNumber.getText().toString();

        if(isEmpty(name) || isEmpty(number)) {
            Toast.makeText(this, R.string.warn_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isSane(name) || !isSane(number)) {
            Toast.makeText(this, R.string.warn_invalid_input, Toast.LENGTH_SHORT).show();
            return;
        }

        AccountDBHelper aDBHelper = new AccountDBHelper(AddAccountActivity.this);
        SQLiteDatabase db = aDBHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AccountContract.AccountEntry.COLUMN_NAME, name);
        values.put(AccountContract.AccountEntry.COLUMN_ACCOUNT, number);

        long newRowId;
        newRowId = db.insert(AccountContract.AccountEntry.TABLE_NAME, null, values);

        if(newRowId == -1) {
            Toast.makeText(this, R.string.db_error, Toast.LENGTH_SHORT).show();
            return;
        }
        super.finish();
    }

    private boolean isSane(String str) {
        if(!str.matches(regex)) {
            return false;
        }
        return true;
    }

    private boolean isEmpty(String str) {
        return str.trim().length() == 0;
    }

    public void cancel(View view) {
        super.finish();
    }

}

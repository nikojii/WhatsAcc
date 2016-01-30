package com.niko.whatsacc;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.niko.whatsacc.db.AccountContract;
import com.niko.whatsacc.db.AccountDBHelper;

public class AddAccountActivity extends AppCompatActivity {

    private String regex = "^[\\w ]+$";
    private String oldName;
    private String oldNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Lets make this an EditAccountActivity
        Bundle b = getIntent().getExtras();
        if(b != null) {
            setTitle(R.string.title_edit_account);
            oldName = b.getString("name");
            oldNumber = b.getString("number");

            EditText editTextName = (EditText)findViewById(R.id.name);
            EditText editTextNumber = (EditText)findViewById(R.id.number);

            editTextName.setText(oldName);
            editTextNumber.setText(oldNumber);

            Button addButton = (Button)findViewById(R.id.button_add);
            addButton.setText(R.string.edit);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editAccount();
                }
            });
        }


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

        if(nameInUse(name)) {
            Toast.makeText(this, R.string.name_in_use, Toast.LENGTH_SHORT).show();
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

    //get the given information, validate and save over old information
    private void editAccount() {
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

        if(!name.equals(oldName) && nameInUse(name)) {
            Toast.makeText(this, R.string.name_in_use, Toast.LENGTH_SHORT).show();
            return;
        }

        AccountDBHelper aDBHelper = new AccountDBHelper(AddAccountActivity.this);
        SQLiteDatabase db = aDBHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(AccountContract.AccountEntry.COLUMN_NAME, name);
        values.put(AccountContract.AccountEntry.COLUMN_ACCOUNT, number);

        String selection = AccountContract.AccountEntry.COLUMN_NAME + " LIKE ?";
        String[] selectionArgs = { oldName };

        int count = db.update(AccountContract.AccountEntry.TABLE_NAME, values, selection, selectionArgs);

        if(count != 1) {
            Toast.makeText(this, R.string.db_error, Toast.LENGTH_SHORT).show();
            return;
        }
        super.finish();
    }

    private boolean nameInUse(String name) {
        AccountDBHelper aDBHelper = new AccountDBHelper(AddAccountActivity.this);
        SQLiteDatabase db = aDBHelper.getReadableDatabase();
        String[] projection = {
                AccountContract.AccountEntry.COLUMN_NAME,
        };
        String selection = AccountContract.AccountEntry.COLUMN_NAME + " LIKE ?";
        String[] selectionArgs = { name };

        Cursor c = db.query(
                AccountContract.AccountEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if(c.getCount() == 0) {
            return false;
        }
        return true;

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

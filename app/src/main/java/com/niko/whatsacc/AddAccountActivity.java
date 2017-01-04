package com.niko.whatsacc;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import com.niko.whatsacc.db.AccountContract;
import com.niko.whatsacc.db.AccountDBHelper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class AddAccountActivity extends AppCompatActivity {

    private String regex = "^[\\w ]+$";
    private String oldName;
    private String oldNumber;
    private ArrayList<String> names;
    static Map<String, Integer> countries = new HashMap<String, Integer>();
    static {
        countries.put("AD", 24);
        countries.put("AE", 23);
        countries.put("AL", 28);
        countries.put("AT", 20);
        countries.put("AZ", 28);
        countries.put("BA", 20);
        countries.put("BE", 16);
        countries.put("BG", 22);
        countries.put("BH", 22);
        countries.put("BR", 29);
        countries.put("CH", 21);
        countries.put("CR", 21);
        countries.put("CY", 28);
        countries.put("CZ", 24);
        countries.put("DE", 22);
        countries.put("DK", 18);
        countries.put("DO", 28);
        countries.put("EE", 20);
        countries.put("ES", 24);
        countries.put("FI", 18);
        countries.put("FO", 18);
        countries.put("FR", 27);
        countries.put("GB", 22);
        countries.put("GE", 22);
        countries.put("GI", 23);
        countries.put("GL", 18);
        countries.put("GR", 27);
        countries.put("GT", 28);
        countries.put("HR", 21);
        countries.put("HU", 28);
        countries.put("IE", 22);
        countries.put("IL", 23);
        countries.put("IS", 26);
        countries.put("IT", 27);
        countries.put("JO", 30);
        countries.put("KW", 30);
        countries.put("KZ", 20);
        countries.put("LB", 28);
        countries.put("LC", 32);
        countries.put("LI", 21);
        countries.put("LT", 20);
        countries.put("LU", 20);
        countries.put("LV", 21);
        countries.put("MC", 27);
        countries.put("MD", 24);
        countries.put("ME", 22);
        countries.put("MK", 19);
        countries.put("MR", 27);
        countries.put("MT", 31);
        countries.put("MU", 30);
        countries.put("NL", 18);
        countries.put("NO", 15);
        countries.put("PK", 24);
        countries.put("PL", 28);
        countries.put("PS", 29);
        countries.put("PT", 25);
        countries.put("QA", 29);
        countries.put("RO", 24);
        countries.put("RS", 22);
        countries.put("SA", 24);
        countries.put("SC", 31);
        countries.put("SE", 24);
        countries.put("SI", 19);
        countries.put("SK", 24);
        countries.put("SM", 27);
        countries.put("ST", 25);
        countries.put("TL", 23);
        countries.put("TN", 24);
        countries.put("TR", 26);
        countries.put("UA", 29);
        countries.put("VG", 24);
        countries.put("XK", 20);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Bundle b = getIntent().getExtras();
        if(b != null) {
            setTitle(R.string.title_edit_account);
            oldName = b.getString("name");
            oldNumber = b.getString("number");
            names = b.getStringArrayList("names");

            if(oldName != null) {
                //Lets make this an EditAccountActivity
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


    }

    //get the given name and account number, validate and save to db
    public void newAccount(View view) {
        EditText editTextName = (EditText)findViewById(R.id.name);
        EditText editTextNumber = (EditText)findViewById(R.id.number);

        String name = editTextName.getText().toString();
        String number = editTextNumber.getText().toString().replaceAll("\\s", "").toUpperCase();

        if(!validInput(name, number)) {
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
        setResult(RESULT_OK);
        super.finish();
    }

    //get the given information, validate and save over old information
    private void editAccount() {
        EditText editTextName = (EditText)findViewById(R.id.name);
        EditText editTextNumber = (EditText)findViewById(R.id.number);

        String name = editTextName.getText().toString();
        String number = editTextNumber.getText().toString().replaceAll("\\s", "").toUpperCase();

        if(!validInput(name, number)) {
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
        setResult(RESULT_OK);
        super.finish();
    }

    private boolean isSane(String str) {
        if(str.matches(regex)) {
            return true;
        }
        return false;
    }

    private boolean isEmpty(String str) {
        return str.trim().length() == 0;
    }

    public void cancel(View view) {
        setResult(RESULT_CANCELED);
        super.finish();
    }

    private boolean isIBAN(String str) {
        String country = str.substring(0,2);
        if(!countries.containsKey(country)) {
            return false;
        }
        if(str.length() != countries.get(country)) {
            return false;
        }

        str = str.substring(4) + str.substring(0, 4);
        for(int i = 0; i < str.length() - 2; ++i) {
            if(Character.isLetter(str.charAt(i))) {
                int newValue = (int)str.charAt(i) - 55;
                str =  str.substring(0,i) + newValue + str.substring(i + 1);
            }
        }
        if(!str.matches("\\d+")) {
            return false;
        }

        BigInteger number = new BigInteger(str);
        BigInteger divider = new BigInteger("97");
        if(number.mod(divider).equals(new BigInteger("1"))) {
            return true;
        }
        return false;
    }

    private boolean validInput(String name, String number) {
        if(isEmpty(name) || isEmpty(number)) {
            Toast.makeText(this, R.string.warn_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!isSane(name) || !isSane(number)) {
            Toast.makeText(this, R.string.warn_invalid_input, Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!isIBAN(number)) {
            Toast.makeText(this, R.string.not_iban, Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!name.equals(oldName) && names.contains(name)) {
            Toast.makeText(this, R.string.name_in_use, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}

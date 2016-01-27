package com.niko.whatsacc;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.niko.whatsacc.db.AccountContract;
import com.niko.whatsacc.db.AccountDBHelper;

public class MainActivity extends AppCompatActivity {

    public ListView listView;
    private AccountDBHelper aDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //a funny floating button for adding new account info
        FloatingActionButton add_button = (FloatingActionButton) findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_account(view);
            }
        });
        listView = (ListView)findViewById(R.id.account_list);
        updateUI();

    }

    //update new db entries to view, newest on bottom
    private void updateUI() {
        aDBHelper = new AccountDBHelper(MainActivity.this);
        SQLiteDatabase db = aDBHelper.getReadableDatabase();
        String[] projection = {
                AccountContract.AccountEntry._ID,
                AccountContract.AccountEntry.COLUMN_NAME,
                AccountContract.AccountEntry.COLUMN_ACCOUNT
        };

        String sortOrder = AccountContract.AccountEntry._ID + " ASC";
        Cursor c = db.query(
                AccountContract.AccountEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
        ListAdapter listAdapter = new SimpleCursorAdapter(
                this,
                R.layout.account_view,
                c,
                new String[] {
                        AccountContract.AccountEntry.COLUMN_NAME,
                        AccountContract.AccountEntry.COLUMN_ACCOUNT},
                new int[] { R.id.nameView, R.id.numberView },
                0
        );
        listView.setAdapter(listAdapter);
        registerForContextMenu(listView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //add new account info
        if (id == R.id.action_add_acc) {
            Intent intent = new Intent(this, AddAccountActivity.class);
            startActivity(intent);
            return true;
        }
        //remove all entries from db
        else if(id == R.id.action_delete_all) {
            aDBHelper = new AccountDBHelper(MainActivity.this);
            SQLiteDatabase db = aDBHelper.getWritableDatabase();
            db.delete(AccountContract.AccountEntry.TABLE_NAME, null, new String[]{});
            updateUI();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if(v.getId() == R.id.account_list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle("Tie jottai");
            String[] menuItems = {"Poista"};
            menu.add(menuItems[0]);
        }
    }

    public void add_account(View view)
    {
        Intent intent = new Intent(this, AddAccountActivity.class);
        startActivity(intent);
    }

    public void onRestart() {
        super.onRestart();
        updateUI();
    }
}

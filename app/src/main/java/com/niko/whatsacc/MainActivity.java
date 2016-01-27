package com.niko.whatsacc;


import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public ListView listView;
    private AccountDBHelper aDBHelper;
    private List<String> names;

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
                addAccount(view);
            }
        });
        listView = (ListView)findViewById(R.id.account_list);
        updateUI();

    }

    //update new db entries to view, alphabetical order
    private void updateUI() {
        aDBHelper = new AccountDBHelper(MainActivity.this);
        SQLiteDatabase db = aDBHelper.getReadableDatabase();
        String[] projection = {
                AccountContract.AccountEntry._ID,
                AccountContract.AccountEntry.COLUMN_NAME,
                AccountContract.AccountEntry.COLUMN_ACCOUNT
        };

        String sortOrder = AccountContract.AccountEntry.COLUMN_NAME + " ASC";
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
        //save the names for easier access on UI elements
        names = new ArrayList<String>();
        c.moveToFirst();
        while(!c.isAfterLast()) {
            String name = c.getString(c.getColumnIndex(AccountContract.AccountEntry.COLUMN_NAME));
            names.add(name);
            c.moveToNext();
        }
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
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.delete_all);
            builder.setMessage(R.string.val_delete_all);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    aDBHelper = new AccountDBHelper(MainActivity.this);
                    SQLiteDatabase db = aDBHelper.getWritableDatabase();
                    db.delete(AccountContract.AccountEntry.TABLE_NAME, null, new String[]{});
                    updateUI();
                    return;
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            builder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if(v.getId() == R.id.account_list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(names.get(info.position));
            String[] menuItems = {
                    getString(R.string.copy_acc),
                    getString(R.string.edit),
                    getString(R.string.delete)
            };
            for(int i = 0; i < menuItems.length; i++) {
                menu.add(menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuItem.getMenuInfo();
        int menuItemIndex = menuItem.getItemId();
        switch (menuItemIndex) {
            case 0: copyAccount(info.position);
                    break;
            case 1: editAccount(info.position);
                    break;
            case 2: deleteAccount(info.position);
                    break;
        }
        return true;
    }

    //Copy selected account number to clipboard
    private void copyAccount(int id) {
        aDBHelper = new AccountDBHelper(MainActivity.this);
        String name = names.get(id);
        SQLiteDatabase db = aDBHelper.getReadableDatabase();
        String[] projection = {
                AccountContract.AccountEntry._ID,
                AccountContract.AccountEntry.COLUMN_NAME,
                AccountContract.AccountEntry.COLUMN_ACCOUNT
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
        c.moveToFirst();
        String accountNumber = c.getString(c.getColumnIndex(AccountContract.AccountEntry.COLUMN_ACCOUNT));
        // Gets a handle to the clipboard service.
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.acc_number), accountNumber);
        clipboard.setPrimaryClip(clip);
    }

    private void editAccount(int id) {
        aDBHelper = new AccountDBHelper(MainActivity.this);
        SQLiteDatabase db = aDBHelper.getReadableDatabase();
        String[] projection = {
                AccountContract.AccountEntry._ID,
                AccountContract.AccountEntry.COLUMN_NAME,
                AccountContract.AccountEntry.COLUMN_ACCOUNT
        };

        String sortOrder = AccountContract.AccountEntry.COLUMN_NAME + " ASC";
        Cursor c = db.query(
                AccountContract.AccountEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
    }

    private void deleteAccount(int id) {
        String arg = "" + id;
        aDBHelper = new AccountDBHelper(MainActivity.this);
        SQLiteDatabase db = aDBHelper.getReadableDatabase();
        String[] projection = {
                AccountContract.AccountEntry._ID,
                AccountContract.AccountEntry.COLUMN_NAME,
                AccountContract.AccountEntry.COLUMN_ACCOUNT
        };

        String sortOrder = AccountContract.AccountEntry.COLUMN_NAME + " ASC";
        Cursor c = db.query(
                AccountContract.AccountEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        // Define 'where' part of query.
        String selection = AccountContract.AccountEntry._ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { arg };
        // Issue SQL statement.
        db.delete(AccountContract.AccountEntry.TABLE_NAME, selection, selectionArgs);
    }

    public void addAccount(View view) {
        Intent intent = new Intent(this, AddAccountActivity.class);
        startActivity(intent);
    }

    public void onRestart() {
        super.onRestart();
        updateUI();
    }
}

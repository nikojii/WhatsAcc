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
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.niko.whatsacc.db.AccountContract;
import com.niko.whatsacc.db.AccountDBHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public ListView listView;
    private AccountDBHelper aDBHelper;
    private ArrayList<String> names;

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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getBaseContext(), R.string.longPressForMenu, Toast.LENGTH_SHORT).show();
            }
        });
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
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);

    }

    //longpress context menu
    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuItem.getMenuInfo();
        switch (menuItem.getItemId()) {
            case R.id.action_copy_account:
                copyAccount(info.position);
                return true;
            case R.id.action_edit_account:
                editAccount(info.position);
                return true;
            case R.id.action_delete_account:
                deleteAccount(info.position);
                return true;
            default:
                return super.onContextItemSelected(menuItem);
        }
    }

    //Copy the selected account number to clipboard
    private void copyAccount(int id) {
        String accountNumber = getAccountNumber(id);
        // Gets a handle to the clipboard service.
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.acc_number), accountNumber);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show();
    }

    //Edit the selected account information
    private void editAccount(int id) {
        Intent intent = new Intent(this, AddAccountActivity.class);
        intent.putExtra("name", names.get(id));
        intent.putExtra("number", getAccountNumber(id));
        intent.putStringArrayListExtra("names", names);
        startActivityForResult(intent, 2);
    }

    //Delete the selected account information
    private void deleteAccount(int id) {
        aDBHelper = new AccountDBHelper(MainActivity.this);
        String name = names.get(id);
        SQLiteDatabase db = aDBHelper.getWritableDatabase();

        String selection = AccountContract.AccountEntry.COLUMN_NAME + " LIKE ?";
        String[] selectionArgs = { name};

        db.delete(AccountContract.AccountEntry.TABLE_NAME, selection, selectionArgs);
        updateUI();
        Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
    }

    public void addAccount(View view) {
        Intent intent = new Intent(this, AddAccountActivity.class);
        intent.putStringArrayListExtra("names", names);
        startActivityForResult(intent, 1);
    }

    private String getAccountNumber(int id) {
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
        return c.getString(c.getColumnIndex(AccountContract.AccountEntry.COLUMN_ACCOUNT));
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onRestart();
        if(resultCode == RESULT_OK) {
            updateUI();
        }
    }
 }


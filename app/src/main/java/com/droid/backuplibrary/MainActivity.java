package com.droid.backuplibrary;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/


    public void deleteAllBirthdays (View view) {
        // delete all the records and the table of the database provider
        String URL = "content://com.droid.backuplibrary.BirthdayProvider/friends";
        Uri friends = Uri.parse(URL);
        int count = getContentResolver().delete(
                friends, null, null);
        String countNum = "BackupLibrary: "+ count +" records are deleted.";
        Toast.makeText(getBaseContext(),
                countNum, Toast.LENGTH_LONG).show();

    }

    public void addBirthday(View view) {
        // Add a new birthday record
        ContentValues values = new ContentValues();

        values.put(BirthProvider.NAME,
                ((EditText)findViewById(R.id.name)).getText().toString());

        values.put(BirthProvider.BIRTHDAY,
                ((EditText)findViewById(R.id.birthday)).getText().toString());

        Uri uri = getContentResolver().insert(
                BirthProvider.CONTENT_URI, values);

        Toast.makeText(getBaseContext(),
                "BackupLibrary: " + uri.toString() + " inserted!", Toast.LENGTH_LONG).show();
    }


    public void showAllBirthdays(View view) throws UnsupportedEncodingException, JSONException {
        // Show all the birthdays sorted by friend's name
        String URL = "content://com.droid.backuplibrary.BirthdayProvider/friends";
        Uri friends = Uri.parse(URL);
        Cursor c = getContentResolver().query(friends, null, null, null, "name");
        String result = "BackupLibrary Results:";

        if (!c.moveToFirst()) {
            Toast.makeText(this, result+" no content yet!", Toast.LENGTH_LONG).show();
        }else{
            do{
                result = result + "\n" + c.getString(c.getColumnIndex(BirthProvider.NAME)) +
                        " with id " +  c.getString(c.getColumnIndex(BirthProvider.ID)) +
                        " has birthday: " + c.getString(c.getColumnIndex(BirthProvider.BIRTHDAY));
            } while (c.moveToNext());
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        }
        String s=GetJsonURI();

    }

    public String GetJsonURI() throws JSONException, UnsupportedEncodingException {
        String JsonURI = "";
        ArrayList<String> col = new ArrayList<>();
        String URL2 = "content://com.droid.backuplibrary.BirthdayProvider/friends";
        Uri friends2 = Uri.parse(URL2);
        Cursor c = getContentResolver().query(friends2, null, null, null, "name");
        String result = "no content";
        if (!c.moveToFirst()) {
            Toast.makeText(getApplicationContext(), "no content", Toast.LENGTH_LONG).show();
        } else {
            do {
                result = result + "\n" + c.getString(c.getColumnIndex(BirthProvider.NAME)) +
                        " with id " + c.getString(c.getColumnIndex(BirthProvider.ID)) +
                        " has birthday: " + c.getString(c.getColumnIndex(BirthProvider.BIRTHDAY));
            } while (c.moveToNext());
        }
        col.add(BirthProvider.NAME);
        col.add(BirthProvider.ID);
        col.add(BirthProvider.BIRTHDAY);
        JSONArray jarray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uri", URL2);
        JSONArray insideobj = new JSONArray();
        for (String item : col) {
            insideobj.put(item);
        }
        jsonObject.put("ColumnNames", insideobj);
        jarray.put(jsonObject);
        JsonURI=jarray.toString();
        return JsonURI;

    }

}

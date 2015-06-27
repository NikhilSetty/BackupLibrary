package com.droid.backuplibrary;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.droid.actuallibrary.CreateAndUploadBackup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {
    Button btn_Backup,btn_Restore;
    ProgressDialog progressDialog;
    public static String Json_to_send;
    public String value_from_sharedpref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String PREFS_NAME="MyApp_settings";
        setContentView(R.layout.activity_main);
        btn_Backup=(Button)findViewById(R.id.btn_Backup);
        btn_Restore=(Button)findViewById(R.id.btn_Restore);



        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Writing data to SharedPreferences
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Shared_pref", "This value would be in Shared Preferences");
        editor.commit();

        // Reading from SharedPreferences
         value_from_sharedpref = settings.getString("Shared_pref", "");
        Toast.makeText(getApplicationContext(),value_from_sharedpref,Toast.LENGTH_LONG).show();



        btn_Restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Restore data here
            }
        });

        btn_Backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText et_email,et_password;
                Button btn_login;
                LayoutInflater li = LayoutInflater.from(getApplicationContext());
                View promptsView = li.inflate(R.layout.login_dialog, null);
                et_email=(EditText)promptsView.findViewById(R.id.et_email);
                et_password=(EditText)promptsView.findViewById(R.id.et_password);
                //btn_login=(Button)promptsView.findViewById(R.id.btn_login);
                final String email=et_email.getText().toString();
                final String password=et_password.getText().toString();

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setView(promptsView);
                alertDialogBuilder.setMessage("Login!");

                alertDialogBuilder.setPositiveButton("Login",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                if(et_email.getText().toString().equals("") || et_password.getText() == null){
                                    Toast.makeText(getApplicationContext(), "Please enter appropriately!", Toast.LENGTH_LONG).show();
                                }
                                else{

                                    progressDialog = new ProgressDialog(MainActivity.this);
                                    progressDialog.setMessage("Attempting Login...");
                                    progressDialog.setCancelable(false);
                                    progressDialog.setIndeterminate(true);
                                    progressDialog.show();


                                    CreateAndUploadBackup backup=new CreateAndUploadBackup();
                                    backup.CreateBackup(getApplicationContext(),Json_to_send,email,password,progressDialog);

                                }
                            }
                        });
                alertDialogBuilder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });



                /*btn_login.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressDialog = new ProgressDialog(getApplicationContext());
                        progressDialog.setMessage("Attempting Login...");
                        progressDialog.setCancelable(false);
                        progressDialog.setIndeterminate(true);

                        CreateAndUploadBackup backup=new CreateAndUploadBackup();
                        backup.CreateBackup(getApplicationContext(),Json_to_send,email,password,progressDialog);
                    }
                });*/
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();


            }
        });


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
         Json_to_send=GetJsonURI();

        CreateAndUploadBackup uploadBackup = new CreateAndUploadBackup();

        uploadBackup.CreateBackup(getApplicationContext(), Json_to_send, "", "", null);

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
        JSONObject initial=new JSONObject();
        JSONArray jarray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uri", URL2);
        JSONArray insideobj = new JSONArray();
        for (String item : col) {
            insideobj.put(item);
        }
        jsonObject.put("ColumnNames", insideobj);
        jarray.put(jsonObject);
        initial.put("Shared_pref",value_from_sharedpref);
        initial.put("URI",jarray);
        JsonURI=initial.toString();
        return JsonURI;

    }

}

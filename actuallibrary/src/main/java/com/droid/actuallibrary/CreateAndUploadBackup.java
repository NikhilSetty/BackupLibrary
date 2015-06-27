package com.droid.actuallibrary;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;


/**
 * Created by NiRavishankar on 6/27/2015.
 */
public class CreateAndUploadBackup {

    public static boolean CreateBackup(Context context, String json){

        try {

            JSONArray array = (new JSONArray(json));

            for(int j = 0; j < array.length(); j++){
                String uri = array.getJSONObject(j).get("uri").toString();

                JSONArray columnNames = array.getJSONObject(j).getJSONArray("ColumnNames");
                String[] columns = new String[columnNames.length()];
                for(int k =0; k < columnNames.length(); k++){
                    columns[k] = columnNames.get(k).toString();
                }

                Cursor c = context.getContentResolver().query(Uri.parse(uri), null, null, null, null);
                String result = "no content";
                if (!c.moveToFirst()) {
                    Toast.makeText(context, "no content", Toast.LENGTH_LONG).show();
                } else {
                    do {
                        result = result + "\n" + c.getString(c.getColumnIndex(columns[0])) +
                                " with id " + c.getString(c.getColumnIndex(columns[1])) +
                                " has birthday: " + c.getString(c.getColumnIndex(columns[2]));
                    } while (c.moveToNext());
                }

            }

            return true;
        }catch(Exception ex){
            Log.e("Error", ex.getMessage());
            return false;
        }
    }
}

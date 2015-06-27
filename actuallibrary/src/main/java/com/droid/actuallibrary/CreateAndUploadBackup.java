package com.droid.actuallibrary;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Created by NiRavishankar on 6/27/2015.
 */
public class CreateAndUploadBackup {

    private static JSONArray finalObject;
    private static String encryptedString;

    private static String mUserName;
    private static String mPassword;

    public boolean CreateBackup(Context context, String json, String userName, String password, ProgressDialog progress){

        mUserName = userName;
        mPassword = password;
        finalObject = new JSONArray();

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
                JSONObject object = new JSONObject();
                object.put("uri", uri);
                object.put("columnNames",columnNames);
                JSONArray dataArray = new JSONArray();

                if (!c.moveToFirst()) {
                    Toast.makeText(context, "no content", Toast.LENGTH_LONG).show();
                } else {
                    do {
/*
                        result = result + "\n" + c.getString(c.getColumnIndex(columns[0])) +
                                " with id " + c.getString(c.getColumnIndex(columns[1])) +
                                " has birthday: " + c.getString(c.getColumnIndex(columns[2]));
*/

                        JSONArray data = new JSONArray();
                        for(int m = 0; m < columns.length; m++) {
                            data.put(c.getString(c.getColumnIndex(columns[m])));
                        }

                        dataArray.put(data);
                    } while (c.moveToNext());
                }

                object.put("data", dataArray);

                finalObject.put(object);
            }

            String finalJsonString = finalObject.toString();

            encryptedString = EncryptionHelper.encryptIt(finalJsonString);

            UploadPost post = new UploadPost();
            post.execute("");

            return true;
        }catch(Exception ex){
            Log.e("Error", ex.getMessage());
            return false;
        }
    }

    public static boolean RestoreBackup(Context context){


        return false;
    }

    public String POST(String url){
        String result = "";
        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            String json = "";
            JSONObject jsonObject = new JSONObject();
            JSONObject creds = new JSONObject();
            creds.put("Email" , mUserName);
            creds.put("Password" , mPassword);

            jsonObject.put("User", creds);
            jsonObject.put("ApplicationPackageID", "com.droid");

            JSONObject blob = new JSONObject();
            blob.put("Stream", encryptedString);
            blob.put("Size", encryptedString.length());
            blob.put("FileType", "text/plain");

            jsonObject.put("BlobEntity", blob);


            json = jsonObject.toString();

            StringEntity se = new StringEntity(json);

            httpPost.setEntity(se);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");


            HttpResponse httpResponse = httpclient.execute(httpPost);

            InputStream inputStream = httpResponse.getEntity().getContent();

            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

            return result;

        } catch (Exception e) {
            Log.v("Getter", e.getLocalizedMessage());
        }

        return result;
    }

    private class UploadPost extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return POST(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("OK")){
                return;
            }
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }


    private class RestorePost extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            return rPOST(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            if(result != null){
                return;
            }
        }
    }

    public String rPOST(String url){
        String result = "";
        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            String json = "";
            JSONObject jsonObject = new JSONObject();
            JSONObject creds = new JSONObject();
            creds.put("Email" , mUserName);
            creds.put("Password" , mPassword);

            jsonObject.put("User", creds);
            jsonObject.put("ApplicationPackageID", "com.droid");

            JSONObject blob = new JSONObject();
            blob.put("Stream", encryptedString);
            blob.put("Size", encryptedString.length());
            blob.put("FileType", "text/plain");

            jsonObject.put("BlobEntity", blob);


            json = jsonObject.toString();

            StringEntity se = new StringEntity(json);

            httpPost.setEntity(se);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");


            HttpResponse httpResponse = httpclient.execute(httpPost);

            InputStream inputStream = httpResponse.getEntity().getContent();

            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

            return result;

        } catch (Exception e) {
            Log.v("Getter", e.getLocalizedMessage());
        }

        return result;
    }
}

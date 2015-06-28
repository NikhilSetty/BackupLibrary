package com.droid.actuallibrary;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
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

    private static JSONObject finalObject;
    private static String encryptedString;

    private static String mUserName;
    private static String mPassword;

    private static String mRestoreString;
    private static Context mContext;

    private static ProgressDialog mProgressDialog;

    private static boolean responeObtained = false;

    public boolean CreateBackup(Context context, String json, String userName, String password, ProgressDialog progress){

        mUserName = userName;
        mPassword = password;
        mProgressDialog = progress;
        finalObject = new JSONObject();
        JSONArray finalContentObject = new JSONArray();

        try {
            JSONObject mainObject = new JSONObject(json);

            SharedPreferences prefs = context.getSharedPreferences("MyApp_settings", Context.MODE_PRIVATE);
            JSONArray spArray = mainObject.getJSONArray("Shared_pref");

            JSONArray finalSPArray = new JSONArray();
            for(int n = 0; n < spArray.length(); n++){
                JSONObject temp = new JSONObject();
                String value = prefs.getString(spArray.getString(n), null);
                temp.put("Key", spArray.getString(n));
                temp.put("Value", value);

                finalSPArray.put(temp);
            }

            finalObject.put("SharedPreferences", finalSPArray);

            JSONArray array = mainObject.getJSONArray("URI");

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

                finalContentObject.put(object);
            }

            finalObject.put("ContentArray", finalContentObject);

            String finalJsonString = finalObject.toString();

            encryptedString = EncryptionHelper.encryptIt(finalJsonString);

            UploadPost post = new UploadPost();
            //post.execute("http://backup-restore.cfapps.io/api/v1/file/upload");
            post.execute("http://192.168.43.73:8080/api/v1/file/upload");

            return true;
        }catch(Exception ex){
            Log.e("Error", ex.getMessage());
            return false;
        }
    }

    public synchronized boolean RestoreBackup(Context context, String userName, String password, ProgressDialog progress){

        try {
            mProgressDialog = progress;
            mUserName = userName;
            mPassword = password;
            mContext = context;

            RestorePost restore = new RestorePost();
  //          restore.execute("http://backup-restore.cfapps.io/api/v1/file/retrieve");
            restore.execute("http://192.168.43.73:8080/api/v1/file/retrieve");


            BackgroundTasks tasks = new BackgroundTasks();
            tasks.execute("");

            return true;
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
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
            jsonObject.put("ApplicationPackageID", "com");

            JSONObject blob = new JSONObject();
            blob.put("Stream", encryptedString);
            blob.put("Size", encryptedString.length());
            blob.put("FileType", "text/plain");

            jsonObject.put("BlobEntity", blob);


            json = jsonObject.toString();

            StringEntity se = new StringEntity(json);

            httpPost.setEntity(se);

            httpPost.setHeader("Content-type", "application/json");

            HttpResponse httpResponse = httpclient.execute(httpPost);

            InputStream inputStream = httpResponse.getEntity().getContent();

            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

            return result;

        } catch (Exception e) {
            Log.v("POST", e.getLocalizedMessage());
            mProgressDialog.dismiss();
        }

        return result;
    }

    private class UploadPost extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            Log.d("Debug", urls[0]);
            return POST(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("Debug", result);
            if(result.equals("OK")){
                mProgressDialog.dismiss();
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
                try {
                    JSONObject object = new JSONObject(result);
                    JSONObject temp = object.getJSONObject("BlobEntity");
                    mRestoreString = temp.getString("Stream");
                    responeObtained = true;
                }catch(Exception ex){
                    ex.printStackTrace();
                }
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
            jsonObject.put("ApplicationPackageID", "com");

            json = jsonObject.toString();

            StringEntity se = new StringEntity(json);

            httpPost.setEntity(se);

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

    private class BackgroundTasks extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            while(!responeObtained){

            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                String decryptedString = EncryptionHelper.decryptIt(mRestoreString);

                JSONObject nObject = new JSONObject(decryptedString);

                JSONArray array = nObject.getJSONArray("ContentArray");

                for (int i = 0; i < array.length(); i++) {

                    JSONObject object = array.getJSONObject(i);

                    String uri = object.getString("uri");
                    JSONArray columns = object.getJSONArray("columnNames");
                    JSONArray dataArray = object.getJSONArray("data");

                    ContentResolver cr = mContext.getContentResolver();
                    for (int j = 0; j < dataArray.length(); j++) {
                        ContentValues values = new ContentValues();
                        JSONArray dataRow = dataArray.getJSONArray(j);
                        for (int k = 0; k < dataRow.length(); k++) {
                            values.put(columns.getString(k), dataRow.getString(k));
                        }

                        cr.insert(Uri.parse(uri), values);
                    }
                }

                final String PREFS_NAME = "MyApp_settings";
                SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, mContext.MODE_PRIVATE);

                JSONArray spArray = nObject.getJSONArray("SharedPreferences");
                for (int q = 0; q < spArray.length(); q++) {
                    SharedPreferences.Editor editor = settings.edit();
                    JSONObject temp = spArray.getJSONObject(q);
                    editor.putString(temp.getString("Key"), temp.getString("Value"));
                    editor.commit();
                }
                mProgressDialog.dismiss();
            }catch(Exception e){
                e.printStackTrace();
                mProgressDialog.dismiss();
            }
        }
    }
}

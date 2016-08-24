package com.ihealth.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Uporabnik on 19/08/2016.
 */
public class BackgroundWorker extends AsyncTask<String, Void, String> {
    Context context;
    AlertDialog alertDialog;
    public AsyncResponse delegate = null;

    public BackgroundWorker (Context ctx, AsyncResponse delegate){
        context = ctx;
        this.delegate = delegate;
    }

    public interface AsyncResponse {
        void processFinish(String s, Context ctx);
    }


    @Override
    protected String doInBackground(String... strings){
        String[] a = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        String postReceiverUrl = strings[strings.length-1];
        URL url;
        String response = "";
        Log.i("asd", strings[0]+" "+strings[1]+" "+strings[2]);
        try {
            url = new URL(postReceiverUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();

            Uri.Builder builder = new Uri.Builder();
            for(int i = 0; i < strings.length-1; i++){
                Log.i("podatki", strings[i]);
                builder.appendQueryParameter(a[i], strings[i]);
            }

            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);

            writer.flush();
            writer.close();
            os.close();

            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }

            }
            else {
                response="Failed";
            }
            Log.i("dd", response);

        } catch (IOException e) {

        }
        return response;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
        Log.i("dd", result);
        delegate.processFinish(result, context);

    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}

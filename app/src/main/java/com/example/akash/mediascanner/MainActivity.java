package com.example.akash.mediascanner;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public class apiAccess extends AsyncTask<String , Void , Void> {
        public String st;
        @Override
        protected Void doInBackground(String... strings) {
            try {
                String code = strings[0];
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?zip="+code+"&appid=bc3a5fe021c3c556d184b37ad0f18aa2");

                URLConnection urlConnection = url.openConnection();

                InputStream in = urlConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);

                BufferedReader br = new BufferedReader(reader);
                st = br.readLine();


            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try{
                JSONObject mainObject = new JSONObject(st);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }}

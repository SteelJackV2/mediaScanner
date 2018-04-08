package com.example.akash.mediascanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

/**
 * Created by Akash on 4/7/2018.
 */

public class bookDetails extends AppCompatActivity {
    JSONObject book = MainActivity.object;
    String name;
    String author;
    String summary;
    String pages;
    String ISBN;
    String genre;
    String path;

    TextView title;
    TextView large;
    Button back;
    String [] ratingSources = new String[4];
    String [] ratings  = new String[4];

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookdetails);
        title = (TextView)findViewById(R.id.booktitle);
        large = (TextView)findViewById(R.id.booklargeText);
        back = (Button)findViewById(R.id.bookback);

        try {
            name = book.getJSONObject("query").getJSONArray("pages").getJSONObject(0).getString("title");
            summary = book.getJSONObject("query").getJSONArray("pages").getJSONObject(0).getString("extract");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        title.setText(name);
        large.setMovementMethod(new ScrollingMovementMethod());
        large.setText(largeFormat());
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }
    public String largeFormat(){
        String format;
        format = "\n"+summary;
        return format;
    }


}

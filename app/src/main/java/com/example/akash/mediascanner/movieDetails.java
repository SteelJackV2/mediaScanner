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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

/**
 * Created by Akash on 4/7/2018.
 */

public class movieDetails extends AppCompatActivity {
    JSONObject movie = MainActivity.object;
    String name;
    String plot;
    String year;
    String rated;
    String releasedate;
    String runtime;
    String genre;
    String director;
    String writers;
    String cast;
    String language;
    String country;
    String path;

    TextView title;
    TextView small;
    TextView large;
    ImageView image;
    Button back;
    String [] ratingSources = new String[4];
    String [] ratings  = new String[4];

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moviedetails);
        title = (TextView)findViewById(R.id.movietitle);
        small = (TextView)findViewById(R.id.smallText);
        large = (TextView)findViewById(R.id.largeText);
        image = (ImageView)findViewById(R.id.imageView);
        back = (Button)findViewById(R.id.bookback);
        try {
            name = movie.getString("Title");
            plot = movie.getString("Plot");
            rated = movie.getString("Rated");
            year = movie.getString("Year");
            releasedate = movie.getString("Released");
            runtime = movie.getString("Runtime");
            genre = movie.getString("Genre");
            director = movie.getString("Director");
            writers = movie.getString("Writer");
            cast = movie.getString("Actors");
            language = movie.getString("Language");
            country = movie.getString("Country");
            path = movie.getString("Poster");
            JSONArray ratinglist  = movie.getJSONArray("Ratings");
            for(int x = 0; x<4; x++){
                JSONObject rat = ratinglist.getJSONObject(x);
                ratingSources[x] = rat.getString("Source");
                ratings[x] = rat.getString("Value");
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
        title.setText(name+" ("+year+")");
        small.setText(smallFromat());
        large.setMovementMethod(new ScrollingMovementMethod());
        large.setText(largeFormat());
        new DownloadImageTask((ImageView) findViewById(R.id.imageView)).execute(path);
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

    public String smallFromat(){
        String format ="";
        format = genre+"\n"+rated+"\n"+runtime+"\n"+language;
        return format;
    }
    public String largeFormat(){
        String format ="";

        format += "Release Date: "+releasedate+"\n\nDirector by: "+director+"\n\n"+"Written by: "+writers+"\n\n"+"Cast: "+cast+"\n\n\t"+plot;
        format+="\n\nRatings:\n";
        for (int x = 0; x<4;x++){
            if(ratings[x]==null){
                format+=" ";
            }
            else {
                format += "" + ratingSources[x] + "\n\t" + ratings[x] + "\n\n";
            }
        }
        return format;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


}

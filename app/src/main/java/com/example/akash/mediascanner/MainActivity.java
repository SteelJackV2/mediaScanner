package com.example.akash.mediascanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.LoginFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.ColorInfo;
import com.google.api.services.vision.v1.model.DominantColorsAnnotation;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.ImageProperties;
import com.google.api.services.vision.v1.model.SafeSearchAnnotation;

import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    public static JSONObject object;
    public static String whichone="one";
    boolean state=true;

    private static final String TAG = "MainActivity";
    private static final int RECORD_REQUEST_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;

    private static final String CLOUD_VISION_API_KEY = "AIzaSyBKy2vSv-LXEiyqMGjbzPFtuM7t6scx5lk";

    @BindView(R.id.takePicture)
    Button takePicture;

    @BindView(R.id.imageProgress)
    ProgressBar imageUploadProgress;

    @BindView(R.id.imageView)
    ImageView imageView;

    @BindView(R.id.visionAPIData)
    TextView visionAPIData;
    private Feature featureOne;
    private Feature featureTwo;

    private Bitmap bitmap;
    private String[] visionAPI = new String[]{"LANDMARK_DETECTION", "LOGO_DETECTION", "SAFE_SEARCH_DETECTION", "IMAGE_PROPERTIES", "LABEL_DETECTION"};

    private String api = "LOGO_DETECTION";
    private String finaltext;
    private String mediaName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        featureOne = new Feature();
        featureOne.setType("LOGO_DETECTION");
        featureOne.setMaxResults(10);

        featureTwo = new Feature();
        featureTwo.setType("LABEL_DETECTION");
        featureTwo.setMaxResults(10);

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                whichone = "one";
                finaltext = "";
                takePictureFromCamera();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePicture.setVisibility(View.VISIBLE);
        } else {
            takePicture.setVisibility(View.INVISIBLE);
            makeRequest(Manifest.permission.CAMERA);
        }
    }

    private int checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission);
    }

    private void makeRequest(String permission) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, RECORD_REQUEST_CODE);
    }

    public void takePictureFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
            callCloudVision(bitmap, featureOne);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RECORD_REQUEST_CODE) {
            if (grantResults.length == 0 && grantResults[0] == PackageManager.PERMISSION_DENIED
                    && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                finish();
            } else {
                takePicture.setVisibility(View.VISIBLE);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void callCloudVision(final Bitmap bitmap, final Feature feature) {
        imageUploadProgress.setVisibility(View.VISIBLE);
        final List<Feature> featureList = new ArrayList<>();

        featureList.add(feature);
        final List<AnnotateImageRequest> annotateImageRequests = new ArrayList<>();

        AnnotateImageRequest annotateImageReq = new AnnotateImageRequest();
        annotateImageReq.setFeatures(featureList);
        annotateImageReq.setImage(getImageEncodeImage(bitmap));
        annotateImageRequests.add(annotateImageReq);

        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer = new VisionRequestInitializer(CLOUD_VISION_API_KEY);

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(annotateImageRequests);

                    Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
                    annotateRequest.setDisableGZipContent(true);
                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);
                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " + e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                imageUploadProgress.setVisibility(View.INVISIBLE);
                if (whichone.equals("one")) {
                    finaltext=result;
                    mediaName = result;
                    visionAPIData.setText(finaltext);
                    callCloudVision(bitmap,featureTwo);
                    whichone = "two";
                }
                else if(whichone.equals("two")){
                    //finaltext+=result;
                    visionAPIData.setText(finaltext);
                    mediaDecider(result);
                    Log.d("Sorted", finaltext);


                }
            }
        }.execute();

    }

    @NonNull
    private Image getImageEncodeImage(Bitmap bitmap) {
        Image base64EncodedImage = new Image();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        base64EncodedImage.encodeContent(imageBytes);
        return base64EncodedImage;
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        AnnotateImageResponse imageResponses = response.getResponses().get(0);
        List<EntityAnnotation> entityAnnotations;

        String message = "";
        if (whichone.equals("one")) {
            entityAnnotations = imageResponses.getLogoAnnotations();
            message = formatAnnotation(entityAnnotations);
        }
        else if(whichone.equals("two")){
            entityAnnotations = imageResponses.getLabelAnnotations();
            message = formatAnnotation(entityAnnotations);
        }

        return message;
    }

    private String formatAnnotation(List<EntityAnnotation> entityAnnotation) {
        String message = "";
        if (entityAnnotation != null) {
            if(whichone.equals("one")) {
                EntityAnnotation entity = entityAnnotation.get(0);
                message = entity.getDescription();
                message += "\n";
            }
            else if(whichone.equals("two")){
                message = "";
                for (EntityAnnotation entity : entityAnnotation) {
                    message = message + entity.getDescription();
                    message += " ";
                }
            }

        } else {
            message = "Nothing Found";
        }
        return message;
    }

    private void mediaDecider(String responses){
        if (responses.contains("film")){
            if(mediaName.contains("(")){
            mediaName = mediaName.substring(0,  mediaName.indexOf("(") );
                Log.d("TAG", mediaName);
            }
            movieAccess maccess = new movieAccess();
            maccess.execute(mediaName);
        }

        else{
            bookAccess baccess = new bookAccess();
            baccess.execute(mediaName);
        }
    }
    public class movieAccess extends AsyncTask<String, Void, Void> {
        public String st;
        @Override
        protected Void doInBackground(String ... voids) {
            try {
                URL url = new URL("http://www.omdbapi.com/?t="+voids[0]+"&apikey=4c7075e0");
                URLConnection urlConnection = url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(reader);
                JSONObject movie = new JSONObject(br.readLine());
                object = movie;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent i = new Intent(MainActivity.this,movieDetails.class);
            startActivityForResult(i,0);
        }
    }

    public class bookAccess extends AsyncTask<String, Void, Void> {
        String data="";
        private OkHttpClient okHttpClient;
        private Request request;
        @Override
        protected Void doInBackground(String... voids) {
            URL url = null;
            try {
                Log.d("TAGGED","A");
                String Wurl = "https://en.wikipedia.org/w/api.php?action=query&prop=extracts&explaintext&format=json&formatversion=2&titles="+voids[0];
//                https://en.wikipedia.org/w/api.php?action=query&prop=extracts&explaintext&format=json&formatversion=2&titles=The%20Maze%20Runner
                okHttpClient = new OkHttpClient();
                request = new Request.Builder().url(Wurl).build();
                Response response = okHttpClient.newCall(request).execute();
                data = response.body().string();
                object = new JSONObject(data);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent i = new Intent(MainActivity.this,bookDetails.class);
            startActivityForResult(i,0);
        }
    }


}

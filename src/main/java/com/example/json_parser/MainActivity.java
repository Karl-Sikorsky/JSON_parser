package com.example.json_parser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends ActionBarActivity {
    public static String LOG_TAG = "my_log";
    public static String API_KEY = "6ef688b6c4c41711";
    public static  String API_URL = "https://api.fullcontact.com/v2/person.json?";

    EditText editText;
    Button button;
    TextView textView;
    ImageView imageView;
    ProgressBar progressBar;
    String mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, "Парсер запущен ");

        editText = (EditText)findViewById(R.id.editText);
        button = (Button)findViewById(R.id.button);
        textView = (TextView)findViewById(R.id.textView);
        imageView = (ImageView)findViewById(R.id.imageView);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        progressBar.setVisibility(ProgressBar.INVISIBLE);

    }

    public void onClick(View view) {

        textView.setText("please wait...");
        mail = String.valueOf(editText.getText());
        progressBar.setVisibility(ProgressBar.VISIBLE);
        new ParseTask().execute();
        Bitmap myBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ask3);
        imageView.setImageBitmap(myBitmap);

    }

    private class ParseTask extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected String doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            try {
                URL url = new URL(API_URL + "email=" + mail + "&apiKey=" + API_KEY);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            // выводим целиком полученную json-строку
            Log.d(LOG_TAG, strJson);

            JSONObject dataJsonObj = null;
            String photoURL = "";
            String nameOrg = "";
            String title = "";
            String location = "";
            String fullname="";
            int statusInt = 0;
            try {
                dataJsonObj = new JSONObject(strJson);

                statusInt = dataJsonObj.getInt("status");
                try {
                    if (statusInt == 202) textView.setText("Queued for search \n");
                    }catch(Exception e){}

                try {
                    JSONArray photos = dataJsonObj.getJSONArray("photos");
                    JSONObject photo = photos.getJSONObject(0);
                    photoURL = photo.getString("url");
                }catch (Exception e){}

                try {
                    JSONObject contactinfo = dataJsonObj.getJSONObject("contactInfo");
                    fullname = contactinfo.getString("fullName");
                    Log.d(LOG_TAG, fullname);
                }catch (Exception e){fullname = "not find name";}

                try {
                    JSONObject demographics = dataJsonObj.getJSONObject("demographics");
                    JSONObject locationDeduced = demographics.getJSONObject("locationDeduced");
                    location = locationDeduced.getString("deducedLocation");
                }catch (Exception e){location="not find location";}

                try {


                    JSONArray organizations = dataJsonObj.getJSONArray("organizations");
                    JSONObject organization = organizations.getJSONObject(0);
                    nameOrg = organization.getString("name");
                    Log.d(LOG_TAG, nameOrg);
                    title = organization.getString("title");
                    Log.d(LOG_TAG, title);
                }catch (Exception e){title = "not find title";}




                textView.setText(fullname+"\n title: "+title+"\n location: "+location+"\n organization: "+nameOrg);



                setPhoto(photoURL);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setPhoto(String url) {
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        try {
            Picasso.with(this).load(url).into(imageView);
        }catch (Exception e){
        }

    }
}

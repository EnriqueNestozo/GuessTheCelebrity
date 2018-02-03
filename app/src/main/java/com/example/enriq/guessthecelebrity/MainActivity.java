package com.example.enriq.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    ImageView celebrityImageView;
    String html = "";
    int chosenCelebrity = 0;
    int[] namesChosen = new int[3];
    ArrayList<String> celebritiesNames = new ArrayList<>();
    ArrayList<String> celebritiesUrl = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        celebrityImageView = findViewById(R.id.celebrityImageView);
        downloadHTML();
        getContentFromHtml();
        deployCelebrities();
    }

    public void deployCelebrities(){
        Random rand = new Random();
        chosenCelebrity = rand.nextInt(celebritiesNames.size());
        String imageUrl = celebritiesUrl.get(chosenCelebrity);
        DownloadCelebritiesImages downloadCelebritiesImages = new DownloadCelebritiesImages();

        try {
            celebrityImageView.setImageBitmap(downloadCelebritiesImages.execute(imageUrl).get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        namesChosen = selectRandomCelebritiesNames();
        printNames(namesChosen);
    }

    public int[] selectRandomCelebritiesNames(){
        Random random = new Random();
        int[] randomNames = new int[4];
        int correctAnswerPlace = random.nextInt(4);
        System.out.println(correctAnswerPlace);

        for(int i=0; i<4;i++){
            int randomNumber = random.nextInt(celebritiesNames.size());
            if(i==correctAnswerPlace){
                randomNames[i] = chosenCelebrity;
            }else{
                while(randomNumber==chosenCelebrity){
                    randomNumber = random.nextInt(celebritiesNames.size());
                }
                randomNames[i] = randomNumber;
            }
        }
        return randomNames;
    }

    public void printNames(int[] arrayOfChoises){
        button1.setText(celebritiesNames.get(arrayOfChoises[0]));
        button2.setText(celebritiesNames.get(arrayOfChoises[1]));
        button3.setText(celebritiesNames.get(arrayOfChoises[2]));
        button4.setText(celebritiesNames.get(arrayOfChoises[3]));
    }


    public void downloadHTML(){
        Downloadhtml task = new Downloadhtml();
        try {
            html = task.execute("http://www.posh24.se/kandisar").get();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void getContentFromHtml(){
        String[] splitHtml = html.split("<div class=\"sidebarInnerContainer\">");
        Pattern pattern = Pattern.compile("img src=\"(.*?)\"");
        Matcher matcher = pattern.matcher(splitHtml[0]);
        while(matcher.find()){
            celebritiesUrl.add(matcher.group(1));
        }
        pattern = Pattern.compile("alt=\"(.*?)\"");
        matcher = pattern.matcher(html);
        while(matcher.find()){
            celebritiesNames.add(matcher.group(1));
            //Log.i("info",matcher.group(1));
        }
    }

    static class DownloadCelebritiesImages extends AsyncTask<String,Void,Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap image;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();
                image = BitmapFactory.decodeStream(input);
                return image;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    static class Downloadhtml extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String...urls) {
            try {
                URL url = new URL(urls[0]);
                String text = "";
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                InputStream input = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(input);
                int datos = reader.read();
                while(datos !=-1){
                    char current = (char)datos;
                    text += current;
                    datos = reader.read();
                }
                return text;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void checkAnswer(View view){
        Button buttonSelected = (Button)view;
        if(buttonSelected.getText().equals(celebritiesNames.get(chosenCelebrity))){
            Toast.makeText(getBaseContext(),"Correct!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getBaseContext(),"Incorrect! it was " + celebritiesNames.get(chosenCelebrity), Toast.LENGTH_SHORT).show();
        }
        deployCelebrities();
    }
}

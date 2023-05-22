package com.example.moviereviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.examples.textclassification.client.Result;
import org.tensorflow.lite.examples.textclassification.client.TextClassificationClient;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kotlin.text.UStringsKt;

public class MainActivity extends AppCompatActivity {
    private Recorder recorder = new Recorder();

    private int[] pictures = new int[]{R.drawable.m1, R.drawable.m2};
    private ImageSwitcher imageSwitcher;
    private Intent intent;
    private ImageButton imageButton;
    private int index;
    private String review;
    private  float touchDownX;
    private  float touchUpX;

//    private boolean hasRecordAudioPermission() {
//        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
//    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageSwitcher= findViewById(R.id.imageSwitcher);
        imageButton = findViewById(R.id.imageButton);
        imageSwitcher.setFactory(() -> {
            ImageView  imageView=new ImageView(MainActivity.this);
            imageView.setImageResource(pictures[index]);//设置显示图片（利用下标）
            return imageView;
        });
        imageSwitcher.setOnTouchListener((v, event) -> {
            if(event.getAction()== MotionEvent.ACTION_DOWN) {
                touchDownX=event.getX();
                return true;
            } else if(event.getAction()== MotionEvent.ACTION_UP) {
                touchUpX=event.getX();

                if(touchUpX-touchDownX>100){
                    index=index==0?pictures.length-1:index-1;
                    imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(MainActivity.this,android.R.anim.fade_in));
                    imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(MainActivity.this,android.R.anim.fade_out));
                    imageSwitcher.setImageResource(pictures[index]);
                }
                else if(touchDownX-touchUpX>100){
                    index=index==pictures.length-1?0:index+1;//注意这里下标是从0开始的，所以应该是长度减1
                    imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(MainActivity.this,android.R.anim.fade_in));
                    imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(MainActivity.this,android.R.anim.fade_out));
                    imageSwitcher.setImageResource(pictures[index]);

                }
                return true;
            }
            return false;
        });


        imageButton.setOnTouchListener((v,event)->{
            if(event.getAction()== MotionEvent.ACTION_DOWN) {
//                recorder.startRecording();
                promptSpeechInput();
            } else if(event.getAction()==MotionEvent.ACTION_UP){
//                recorder.stopRecording();
//                onActivityResult(100, 200, intent);
            }
            return false;
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case 100: {
                if (resultCode == RESULT_OK && data != null) {
                    TextView textView = (TextView) findViewById(R.id.reviewText);
                    ArrayList<String> results = data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS);
                    String paragraph = "";
                    for (String result : results) {
                        paragraph += result;
                    }
                    review = paragraph;
                    textView.setText(paragraph);

                    getScore(paragraph);
                }
            }
        }
    }

    private double getScore(String paragraph){
        TextClassificationClient client = new TextClassificationClient(this);
        client.load();
        List<Result> results = client.classify(paragraph);
        double result = results.stream()
                .mapToDouble(i->i.getConfidence()).sum()/results.size();

        client.unload();
        return result;
    }


    private void promptSpeechInput() {
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); //ACTION_RECOGNIZE_SPEECH
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something");

        try {
//            intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, getAudioData());
            startActivityForResult(intent, 100);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(),
                    "Speech not supported",
                    Toast.LENGTH_SHORT).show();
        }
//        onActivityResult(100, 200, intent);
    }

    private byte[] getAudioData() throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(getAudioFilePath());
        if (inputStream != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1000000];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            return byteArrayOutputStream.toByteArray();
        } else {
            return null;
        }

    }

    public Uri getAudioFilePath(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, "audio");
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/*");
        values.put(MediaStore.Audio.Media.DATA, recorder.getAudioFilePath());

        ContentResolver contentResolver = getContentResolver();
        Uri uri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);

        return uri;
    }

}
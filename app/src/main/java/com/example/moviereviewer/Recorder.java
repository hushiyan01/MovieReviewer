package com.example.moviereviewer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.IOException;

public class Recorder {
    //获取录音权限
//if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
//            != PackageManager.PERMISSION_GRANTED) {
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
//    }
//
    private MediaRecorder mediaRecorder;

    public String getAudioFilePath() {
        return audioFilePath;
    }

    private String audioFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record_audio.3gp";

    public Recorder() {
        //实例化MediaRecorder并设置参数
//        mediaRecorder = new MediaRecorder();
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        mediaRecorder.setOutputFile(audioFilePath);
    }


    //开始录音
    public void startRecording() {
        //开始录音
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //停止录音
    public void stopRecording() {
        //停止录音
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        //上传音频文件进行语音识别
        //...
    }
}

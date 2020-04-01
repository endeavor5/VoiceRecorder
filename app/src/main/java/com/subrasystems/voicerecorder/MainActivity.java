package com.subrasystems.voicerecorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button play, stop, record;
    private String mFilePath;

    private boolean isStarted;
    private MediaRecorder mRecorder;

    private String[] PERMISSIONS = {
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private PermissionUtils mPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //-----------------------------------------------| List of Permissions
        mPermissions = new PermissionUtils(this, PERMISSIONS);

        //-----------------------------------------------| findViewById
        play = (Button) findViewById(R.id.play);
        stop = (Button) findViewById(R.id.stop);
        record = (Button) findViewById(R.id.record);
        stop.setEnabled(false);
        play.setEnabled(false);

        //-----------------------------------------------| Check Permissions
        checkPermissions();

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BackTask().execute("start");
                record.setEnabled(false);
                stop.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BackTask().execute("stop");
                record.setEnabled(true);
                stop.setEnabled(false);
                play.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Audio Recorder successfully", Toast.LENGTH_LONG).show();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "" +mFilePath);
                MediaPlayer mPlayer = new MediaPlayer();
                try {
                    if (mFilePath != null) {
                        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mPlayer.setDataSource(getApplicationContext(), Uri.parse(mFilePath));
                        mPlayer.prepare();
                        mPlayer.start();
                        Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //===============================================| List of Permissions
    private void checkPermissions() {
        if(mPermissions.arePermissionsEnabled()){
            Log.d(TAG, "Permission granted");
        } else {
            mPermissions.requestMultiplePermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(mPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            Log.d(TAG, "Permission granted");
        }
    }

    //===============================================| Recording Task
    class BackTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            if (params[0].equals("start")) {
                try {
                    String path = getApplicationContext().getFilesDir().getPath();
                    File file = new File(path);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                    mRecorder = new MediaRecorder();
                    /*mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                    mRecorder.setAudioChannels(1);
                    mRecorder.setAudioSamplingRate(8000);
                    mRecorder.setAudioEncodingBitRate(44100);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);*/

                    mRecorder.reset();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                    if (!file.exists()){
                        file.mkdirs();
                    }
                    mFilePath = file+"/" + "REC_" + timeStamp + ".3gp";
                    mRecorder.setOutputFile(mFilePath);

                    mRecorder.prepare();
                    mRecorder.start();
                    isStarted = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (params[0].equals("stop")) {
                if (isStarted && mRecorder != null) {
                    mRecorder.stop();
                    mRecorder.reset(); // You can reuse the object by going back to setAudioSource() step
                    mRecorder.release();
                    mRecorder = null;
                    isStarted = false;
                }
            }
            return null;
        }
    }


}
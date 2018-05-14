package com.example.gerin.musicplayer;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.jgabrielfreitas.core.BlurImageView;

public class MainActivity extends AppCompatActivity {

    Button playButton;
    SeekBar progressBar;
    SeekBar volumeBar;
    TextView elapsedTimeLabel;
    TextView remainingTimeLabel;
    MediaPlayer mp;
    ImageView audioVolume;
    Switch loopSwitch;
    BlurImageView backgroundImage;
    int totalTime;
    boolean loopOn = false;
    boolean currPause = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find IDs
        playButton = (Button) findViewById(R.id.play_button);
        progressBar = (SeekBar) findViewById(R.id.progressBar);
        volumeBar = (SeekBar) findViewById(R.id.volumeBar);
        audioVolume = (ImageView) findViewById(R.id.volumeImage);
        elapsedTimeLabel = (TextView) findViewById(R.id.elapsedTime);
        remainingTimeLabel = (TextView) findViewById(R.id.remainingTime);
        loopSwitch = (Switch) findViewById(R.id.loopSwitch);
        backgroundImage = (BlurImageView) findViewById(R.id.backgroundImage);

        //blur the background with album art
        backgroundImage.setBlur(4);

        //create mediaplayer
        mp = MediaPlayer.create(this, R.raw.music_so_long);
        mp.setVolume(0.5f,0.5f);
        mp.seekTo(0);
        totalTime = mp.getDuration();

        //progress bar
        progressBar.setMax(totalTime);
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser && !mp.isPlaying() && !currPause) {
                    mp.seekTo(progress);
                    progressBar.setProgress(progress);
                    mp.start();
                    playButton.setBackgroundResource(R.drawable.pause_button);
                }
                else if(fromUser){
                    mp.seekTo(progress);
                    progressBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //volume bar
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float newVolume = progress / 100f;
                updateVolume(newVolume);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //loop switch
        loopSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loopOn = true;
                mp.setLooping(isChecked);
            }
        });

        //create second thread to update progress bar
       Thread second = new Thread(new Runnable() {
            @Override
            public void run() {
                while(mp != null){
                    try{
                        Message msg = new Message();
                        msg.what = mp.getCurrentPosition();
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    }catch (InterruptedException e){}
                }
            }
        });//.start();
        second.start();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.music_icon)
                .setContentTitle("Music Player")
                .setContentText("Playing \"So Long - Massari\" ")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, mBuilder.build());
    }

    public void playMusic(View v){
        if(!mp.isPlaying()) {
            mp.start();
            playButton.setBackgroundResource(R.drawable.pause_button);
            currPause = false;
        }
        else{
            pauseMusic(v);
        }
    }

    public void pauseMusic(View v){
        if(mp.isPlaying()) {
            mp.pause();
            playButton.setBackgroundResource(R.drawable.play_button);
            currPause = true;
        }
    }

    public void updateVolume(float newVolume){
        //set new volume
        mp.setVolume(newVolume, newVolume);

        //adjust sprite
        if(newVolume <= 0.3f)
            audioVolume.setImageResource(R.drawable.low_volume_button);
        else if(newVolume <= 0.7f)
            audioVolume.setImageResource(R.drawable.med_volume_button);
        else
            audioVolume.setImageResource(R.drawable.high_volume_button);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            int currentPosition = msg.what;

            //update play button
            if(!mp.isPlaying()) {
                playButton.setBackgroundResource(R.drawable.play_button);
            }

            //continuously update progress bar
            progressBar.setProgress(currentPosition);

            //continuously update time stamps
            String elapsedTime = createTimeStamp(currentPosition);
            elapsedTimeLabel.setText(elapsedTime);
            String remainingTime = createTimeStamp(totalTime - currentPosition);
            remainingTimeLabel.setText(remainingTime);

        }

    };

    public String createTimeStamp(int time){
        String label = "";
        int min = (time / 1000) / 60;
        int sec = (time / 1000) % 60;

        label = min + ":";
        if(sec < 10)
            label += "0";
        label += sec;

        return label;
    }

}

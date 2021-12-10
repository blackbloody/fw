package com.darker.fwvideoplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.darker.fwvideoplayer.custom_view.WrapperVideo;
import com.darker.fwvideoplayer.helper.EarPhoneStateReceiver;
import com.darker.fwvideoplayer.models.MdlVideo;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class VideoPlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
        View.OnClickListener, AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "VideoPlayerActivity";
    public static final String LOCAL_BROADCAST_ACTION = "com.darker.fwvideoplayer.VideoPlayerActivity.LOCAL_BROADCAST";
    public static final String LOCAL_BROADCAST_SOURCE = "LOCAL_BROADCAST_SOURCE";
    MdlVideo mdlVideo;
    List<MdlVideo> listVideo;
    boolean isBack = false;

    public LinearLayout root;
    private View decorView;
    private int uiImmersiveOptions;

    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    private SurfaceHolder surfaceHolder;
    WrapperVideo wrapperVideo;
    RelativeLayout lControl;

    SeekBar seekBar;
    public ImageButton iBtnPlay;
    ImageButton iBtnPause, iBtnRewind, iBtnForward;
    TextView tvTitle, tvCurrentVid, tvDurationVid;

    private Handler mainHandler;
    private Runnable updatePlayer;
    EarPhoneStateReceiver earPhoneStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        mainHandler = new Handler();

        tvTitle = findViewById(R.id.tv_title);
        root = findViewById(R.id.root);
        wrapperVideo = findViewById(R.id.respect_ratio);
        lControl = findViewById(R.id.layout_control);
        seekBar = findViewById(R.id.seekbar);
        tvCurrentVid = findViewById(R.id.txt_currentTime);
        tvDurationVid = findViewById(R.id.txt_totalDuration);
        iBtnRewind = findViewById(R.id.btn_rewind);
        iBtnForward = findViewById(R.id.btn_forward);
        iBtnPause = findViewById(R.id.btn_pause_play);
        iBtnPlay = findViewById(R.id.btn_play_pause);
        iBtnPlay.setOnClickListener(this);
        iBtnPause.setOnClickListener(this);
        iBtnRewind.setOnClickListener(this);
        iBtnForward.setOnClickListener(this);
        OnWrapperVideo();

        surfaceView = findViewById(R.id.vid_player);
        root.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void OnWrapperVideo(){
        wrapperVideo.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                Log.d(TAG, "OnWrapperVideo: ");
                if (root.getVisibility() == View.VISIBLE)
                    root.setVisibility(View.GONE);
                else
                    root.setVisibility(View.VISIBLE);
            }
            return false;
        });
        lControl.setOnClickListener(v -> root.setVisibility(View.VISIBLE));
    }

    private void OnSeekBar() {

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null){
                    Log.d(TAG, "onStopTrackingTouch: " + seekBar.getProgress());
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onResume() {
        super.onResume();

        uiImmersiveOptions = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LOW_PROFILE |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiImmersiveOptions);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        boolean isMain = Objects.requireNonNull(getIntent().getExtras()).getBoolean("isMain");
        if (isMain){
            mdlVideo = getIntent().getParcelableExtra("mdlVideo");
            listVideo = (List<MdlVideo>) Objects.requireNonNull(getIntent().getExtras()).getSerializable("list");
        }
        else {
            mdlVideo = new Gson().fromJson(prefs.getString("mdlVideo", "[]"), MdlVideo.class);
            editor.remove("mdlVideo");
            editor.apply();
        }

        tvTitle.setText(mdlVideo.getTitle());

        if (mdlVideo == null)
            return;
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        OnSeekBar();
    }

    @Override
    public void onBackPressed() {
        isBack = true;
        super.onBackPressed();
    }

    @Override
    protected void onPause() {

        if (earPhoneStateReceiver != null){
            unregisterReceiver(earPhoneStateReceiver);
            earPhoneStateReceiver = null;
        }
        super.onPause();
        if (!isBack && (mediaPlayer != null)){
            int currentPosition = mediaPlayer.getCurrentPosition();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("current_player", currentPosition);
            editor.putString("mdlVideo", new Gson().toJson(mdlVideo));
            editor.apply();
        }
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null){
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void UpdateControlPlayer(){

        if (mediaPlayer == null)
            return;

        @SuppressLint("DefaultLocale") String titlDur = String.format("%02d.%02d.%02d",
                TimeUnit.MILLISECONDS.toHours(mediaPlayer.getDuration()),
                TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getDuration()) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(mediaPlayer.getDuration())),
                TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getDuration())));

        @SuppressLint("DefaultLocale") String curDur = String.format("%02d.%02d.%02d",
                TimeUnit.MILLISECONDS.toHours(mediaPlayer.getCurrentPosition()),
                TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getCurrentPosition()) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(mediaPlayer.getCurrentPosition())),
                TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getCurrentPosition())));

        if (titlDur.contains("00")){
            String checkHourZero = titlDur.substring(0, 2);
            if (checkHourZero.contains("00")){
                titlDur =  titlDur.substring(3);
                curDur = curDur.substring(3);
            }
        }

        tvCurrentVid.setText(curDur);
        tvDurationVid.setText(titlDur);
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setProgress(mediaPlayer.getCurrentPosition());

        mainHandler.postDelayed(updatePlayer, 300);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDisplay(surfaceHolder);
        try{
            mediaPlayer.setDataSource(mdlVideo.getPath());
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            int widthVid = mediaPlayer.getVideoWidth();
            int heightVid = mediaPlayer.getVideoHeight();
            float videoProportion = (float) widthVid / (float)heightVid;

            int widthScreen = getWindowManager().getDefaultDisplay().getWidth();
            int heightScreen = getWindowManager().getDefaultDisplay().getHeight();
            float screenProportion = (float) widthScreen / (float) heightScreen;

            android.view.ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
            if (videoProportion > screenProportion){
                lp.width = widthScreen;
                lp.height = (int) ((float)widthScreen / videoProportion);
            }
            else {
                lp.width = (int) (videoProportion * (float) heightScreen);
                lp.height = heightScreen;
            }

            surfaceView.setLayoutParams(lp);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        iBtnPause.setVisibility(View.VISIBLE);
        iBtnPlay.setVisibility(View.GONE);
        mediaPlayer.start();
        root.setVisibility(View.GONE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        int player = prefs.getInt("current_player", -1);
        if (player > 0){
            mediaPlayer.seekTo(player);
            mediaPlayer.start();
            editor.remove("current_player");
            editor.apply();
        }

        if (earPhoneStateReceiver == null){
            earPhoneStateReceiver = new EarPhoneStateReceiver(this, mediaPlayer);
            IntentFilter intFil = new IntentFilter();
            for (String action : EarPhoneStateReceiver.EarphoneAction)
                intFil.addAction(action);
            registerReceiver(earPhoneStateReceiver, intFil);
        }

        if (updatePlayer == null){
            updatePlayer = this::UpdateControlPlayer;
        }
        mainHandler.postDelayed(updatePlayer, 300);
    }

    private static final int TIME_INTERVAL = 3000;
    private int forwardPress = 10000;
    private long timeForwardPress;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_pause_play:
            case R.id.btn_play_pause:
                if (mediaPlayer == null)
                    break;
                Log.d(TAG, "onClick: " + "Play > Pause");
                if (iBtnPlay.getVisibility() == View.VISIBLE){
                    mediaPlayer.start();
                    iBtnPause.setVisibility(View.VISIBLE);
                    iBtnPlay.setVisibility(View.GONE);
                }
                else if (iBtnPause.getVisibility() == View.VISIBLE) {
                    mediaPlayer.pause();
                    iBtnPause.setVisibility(View.GONE);
                    iBtnPlay.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_rewind:
                if (mediaPlayer != null){
                    final int currentPosition = mediaPlayer.getCurrentPosition() - 8000;
                    mediaPlayer.seekTo(currentPosition);
                    Log.d(TAG, "onClick: Rewind " + currentPosition);
                }
                break;
            case R.id.btn_forward:
                if (mediaPlayer != null) {

                    if (timeForwardPress + TIME_INTERVAL > System.currentTimeMillis()){
                        forwardPress += 3000;
                    }
                    timeForwardPress = System.currentTimeMillis();

                    final int currentPosition = mediaPlayer.getCurrentPosition() + forwardPress;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        mediaPlayer.seekTo(currentPosition, MediaPlayer.SEEK_CLOSEST);
                    else
                        mediaPlayer.seekTo(currentPosition);

                    Log.d(TAG, "onClick: Forward " + currentPosition);
                    Log.d(TAG, "onClick: Forward Press : " + forwardPress);

                    new Handler().postDelayed(()->{ forwardPress = 10000; timeForwardPress = 0; }, 3000);
                }
                break;
        }
    }

    public void OnResizeView(){
        if (mediaPlayer == null)
            return;

        int videoWidth= mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
    }

    int i = 0;
    @Override
    public void onAudioFocusChange(int focusChange) {
        ++i;
        Log.d(TAG, "onAudioFocusChange: " + i);
    }
}




















package com.darker.fwvideoplayer.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;

import com.darker.fwvideoplayer.VideoPlayerActivity;

public class EarPhoneStateReceiver extends BroadcastReceiver {

    private static final String TAG = "EarPhoneStateReceiver";
    public static final String[] EarphoneAction = new String[]{
            Intent.ACTION_HEADSET_PLUG,
            "android.bluetooth.headset.action.STATE_CHANGED",
            "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED",
            AudioManager.ACTION_AUDIO_BECOMING_NOISY
    };

    VideoPlayerActivity activity;
    MediaPlayer mediaPlayer;

    public EarPhoneStateReceiver(VideoPlayerActivity activity, MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        try{
            String action = intent.getAction();
            if (action != null && mediaPlayer != null && mediaPlayer.isPlaying()){
                if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)){
                    activity.iBtnPlay.performClick();
                    activity.root.setVisibility(View.VISIBLE);
                }
            }
        }
        catch (Exception e){
            Log.e(TAG, "onReceive: " + e);
        }
    }
}

package com.deakishin.yourturntimer.controllerlayer.sound;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by Dmitry Akishin on 31.01.2017.
 * <p>
 * Class for playing sounds.
 */

public class SoundPlayer {
    /* Player used for playing audio. */
    private MediaPlayer mMediaPlayer;

    /**
     * Empty constructor.
     */
    public SoundPlayer() {
    }

    /**
     * Plays sound.
     *
     * @param context    application context.
     * @param soundResId the raw resource identifier of the audio resource to play.
     */
    public void play(Context context, int soundResId) {
        mMediaPlayer = MediaPlayer.create(context, soundResId);

        if (mMediaPlayer != null){
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stop();
                }
            });
        }
    }

    /** Stops playing any sounds. */
    public void stop(){
        if (mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}

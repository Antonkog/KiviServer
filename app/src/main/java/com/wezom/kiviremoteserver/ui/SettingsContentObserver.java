package com.wezom.kiviremoteserver.ui;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

import timber.log.Timber;

public class SettingsContentObserver extends ContentObserver {
    private int previousVolume;
    private Context context;

    public SettingsContentObserver(Context c, Handler handler) {
        super(handler);
        context = c;

        AudioManager audio = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentMusicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

        int musicDelta = previousVolume - currentMusicVolume;

        if (musicDelta > 0) {
            previousVolume = currentMusicVolume;
        } else if (musicDelta < 0) {
            previousVolume = currentMusicVolume;
        }

        Timber.d("MUSIC VOLUME: " + currentMusicVolume);
    }
}
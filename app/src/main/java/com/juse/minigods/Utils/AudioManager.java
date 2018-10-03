package com.juse.minigods.Utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.SparseIntArray;

import com.juse.minigods.R;

public class AudioManager {
    public enum Music { MAIN_MUSIC }
    public enum Sound { HIT_SOUND }

    private SparseIntArray musicArray, soundArray;
    private boolean musicPlaying;
    private MediaPlayer player;
    private SoundPool soundPool;

    public AudioManager(Context context) {
        musicArray = new SparseIntArray();
        soundArray = new SparseIntArray();
        soundPool = new SoundPool.Builder().setMaxStreams(5).build();

        musicArray.put(Music.MAIN_MUSIC.ordinal(), R.raw.music_jumpshot);
        soundArray.put(Sound.HIT_SOUND.ordinal(),
                soundPool.load(context, R.raw.hit, 1));

    }

    public void delete() {
        player.release();
        soundPool.release();
    }

    public void playMusic(Context context, Music music, boolean loop) {
        if (musicPlaying) {
            player.release();
        }

        player = MediaPlayer.create(context, musicArray.get(music.ordinal()));
        player.setVolume(0.3f, 0.3f);
        player.setOnCompletionListener(mp -> {
            if (loop)
                mp.start();
            else
                mp.release();
        });

        player.start();
        musicPlaying = true;
    }

    public void playSound(Sound sound) {
        soundPool.play(soundArray.get(sound.ordinal()), 1, 1, 0, 0, 1);
    }
}
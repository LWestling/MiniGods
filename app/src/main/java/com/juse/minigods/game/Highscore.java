package com.juse.minigods.game;

import android.content.Context;
import android.content.SharedPreferences;

public class Highscore {
    private final static String NAME = "highscore", SCORE_KEY = "score";

    public int getHighscore(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sharedPreferences.getInt(SCORE_KEY, 0);
    }

    public void overwriteHighscore(Context context, int score) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(SCORE_KEY, score).apply();
    }
}

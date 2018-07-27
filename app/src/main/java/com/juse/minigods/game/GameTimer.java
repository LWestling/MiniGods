package com.juse.minigods.game;

/**
 * Created by LukasW on 2018-03-22.
 * Keeps track of deltaTime
 */

public class GameTimer {
    private long lastTime;

    public GameTimer() {
        lastTime = 0;
    }

    public void resetTimer() {
        lastTime = System.currentTimeMillis();
    }

    public float calcDeltaTime() {
        float dt = (System.currentTimeMillis() - lastTime) / 1000.f;
        resetTimer();
        return dt;
    }
}

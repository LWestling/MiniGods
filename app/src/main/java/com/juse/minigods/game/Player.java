package com.juse.minigods.game;

import org.joml.Vector3f;

/**
 * Created by LukasW on 2018-03-22.
 * Player class and its data
 */

public class Player extends Entity {
    private boolean dead, outOfBounds;
    private DeathObserver observer;

    public Player(Vector3f startPosition) {
        super(startPosition);
        dead = false;
    }

    public void setObserver(DeathObserver deathObserver) {
        observer = deathObserver;
    }

    public void kill() {
        dead = true;
        observer.onDeath();
    }

    public float getRadius() {
        return .65f;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public boolean isOutOfBounds() {
        return outOfBounds;
    }

    public void setOutOfBounds(boolean outOfBounds) {
        this.outOfBounds = outOfBounds;
    }

    public void reset() {
        dead = false;
        outOfBounds = false;
    }

    public interface DeathObserver {
        void onDeath();
    }
}

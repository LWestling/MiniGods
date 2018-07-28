package com.juse.minigods.game;

import org.joml.Vector3f;

/**
 * Created by LukasW on 2018-03-22.
 * Obstacle that player will lose on collision
 */

public class Obstacle extends Entity {
    private boolean isOffMap;

    public Obstacle(Vector3f startPosition) {
        super(startPosition);
        isOffMap = false;
    }

    public void update(float dt, float mapSpeed, float minPos) {
        setVelocity(new Vector3f(-mapSpeed, 0.f, 0.f));

        if (getPosition().x() < minPos)
            isOffMap = true;

        super.update(dt);
    }

    public boolean isOffMap() {
        return isOffMap;
    }
}

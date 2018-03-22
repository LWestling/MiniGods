package com.juse.minigods.game;

import org.joml.Vector3f;

/**
 * Created by LukasW on 2018-03-22.
 * Obstacle that player will lose on collision
 */

public class Obstacle extends Entity {
    public Obstacle(Vector3f startPosition) {
        super(startPosition);
    }

    public void update(float dt, float mapSpeed) {
        getVelocity().x = -mapSpeed;
        super.update(dt);
    }
}

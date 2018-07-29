package com.juse.minigods.game;

import org.joml.Vector3f;

/**
 * Created by LukasW on 2018-03-22.
 * Player class and its data
 */

public class Player extends Entity {
    public Player(Vector3f startPosition) {
        super(startPosition);
    }

    public float getRadius() {
        return 0.8f;
    }
}

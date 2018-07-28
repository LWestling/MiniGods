package com.juse.minigods.game;

import org.joml.Vector3f;

/**
 * Created by LukasW on 2018-03-22.
 * Entity class with basic components like position
 */

public class Entity {
    private Vector3f position;
    private Vector3f velocity;

    public Entity(Vector3f startPosition) {
        position = startPosition;
        velocity = new Vector3f();
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setPosition(Vector3f pos) {
        position = pos;
    }

    public void setVelocity(Vector3f vel) {
        velocity = vel;
    }

    public void update(float deltaTime) {
        position.add(new Vector3f(velocity).mul(deltaTime)); // don't know if this is the joml way of doing this
    }
}

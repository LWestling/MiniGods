package com.juse.minigods.game;

import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Created by LukasW on 2018-03-22.
 * Entity class with basic components like position
 */

public class Entity {
    private Vector3f position;
    private Vector2f tilePosition;
    private Vector3f velocity;

    public Entity(Vector3f startPosition) {
        position = startPosition;
        setTilePosition();
        velocity = new Vector3f();
    }

    public Vector3f getPosition() {
        return position;
    }

    /**
     * Without the y-axis
     * @return vec2(pos.x, pos.z)
     */
    public Vector2f getTilePosition() {
        return tilePosition;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    private void setTilePosition() {
        tilePosition = new Vector2f(position.x(), position.z());
    }

    public void setPosition(Vector3f pos) {
        position = pos;
        setTilePosition();
    }

    public void setVelocity(Vector3f vel) {
        velocity = vel;
    }

    public void update(float deltaTime) {
        position.add(new Vector3f(velocity).mul(deltaTime)); // don't know if this is the joml way of doing this
        setTilePosition();
    }
}

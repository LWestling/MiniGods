package com.juse.minigods.rendering.Sprite;

import android.util.SparseArray;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SpriteCache {
    private SparseArray<RenderSprite> spritesToRender;
    private Lock lock;
    private int counter;
    private boolean updated;

    /*
        Pretty inefficient, but only plan to render a few amount of sprites
     */

    public SpriteCache() {
        counter = 0;
        spritesToRender = new SparseArray<>();
        lock = new ReentrantLock();
    }

    public int addSpriteToRender(RenderSprite renderSprite) {
        lock.lock();

        try {
            spritesToRender.put(++counter, renderSprite);
            updated = true;
        } finally {
            lock.unlock();
        }

        return counter;
    }

    public void removeSpritesToRender(int... renderSpriteKeys) {
        lock.lock();

        try {
            for (int renderSpriteKey : renderSpriteKeys) {
                spritesToRender.remove(renderSpriteKey);
            }
            updated = true;
        } finally {
            lock.unlock();
        }
    }

    public void updatePosition(int key, float x, float y) {
        lock.lock();

        try {
            spritesToRender.get(key).setPosition(x, y);
            updated = true;
        } finally {
            lock.unlock();
        }
    }

    public void update(int key, Sprites.SpriteId spriteId, float x, float y) {
        lock.lock();

        try {
            RenderSprite renderSprite = spritesToRender.get(key);
            renderSprite.setPosition(x, y);
            renderSprite.setSpriteId(spriteId);
            updated = true;
        } finally {
            lock.unlock();
        }
    }

    public SparseArray<RenderSprite> getSpritesToRender() {
        return spritesToRender;
    }

    public Lock getLock() {
        return lock;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public static class RenderSprite {
        private float x, y;
        private Sprites.SpriteId spriteId;

        public RenderSprite(Sprites.SpriteId spriteId, float x, float y) {
            this.spriteId = spriteId;
            this.x = x;
            this.y = y;
        }

        void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }

        void setSpriteId(Sprites.SpriteId spriteId) {
            this.spriteId = spriteId;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public Sprites.SpriteId getSpriteId() {
            return spriteId;
        }
    }
}

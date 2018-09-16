package com.juse.minigods.rendering.Font;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TextCache {
    private SparseArray<Text> stringsToRender; // Could replace string with a class that holds font, size, etc.
    private ArrayList<Integer> stringsToUpdate;

    private int counter;
    private Lock lock;

    public TextCache() {
        stringsToRender = new SparseArray<>();
        stringsToUpdate = new ArrayList<>();

        lock = new ReentrantLock();
        counter = 0;
    }

    public int addStringToRender(Text text) {
        lock.lock();
        int key = counter++;

        try {
            stringsToRender.append(key, text);
            requestUpdate(key);
        } finally {
            lock.unlock();
        }

        return key;
    }

    public void removeStringToRender(int... keys) {
        lock.lock();

        try {
            for (int key : keys) {
                stringsToRender.remove(key);
                requestUpdate(key);
            }
        } finally {
            lock.unlock();
        }
    }

    public void updateString(int key, String newString) {
        lock.lock();

        try {
            Text text = stringsToRender.get(key);
            text.setContent(newString);
            requestUpdate(key);
        } finally {
            lock.unlock();
        }
    }

    private void requestUpdate(int key) {
        stringsToUpdate.add(key);
    }

    public SparseArray<Text> getStringsToRender() {
        return stringsToRender;
    }

    public ArrayList<Integer> getStringsToUpdate() {
        return stringsToUpdate;
    }

    public Lock getLock() {
        return lock;
    }

    public static class Text { // TODO Potential MT issue with reading content while being overwritten!
        private String content;
        private float x, y, xSpacing, ySpacing, scale;
        private boolean newlyAdded;

        public Text(String content, float x, float y) {
            this.content = content;
            this.x = x;
            this.y = y;

            newlyAdded = true;

            xSpacing = 1.05f;
            ySpacing = 1.15f;
            scale = 1.f;
        }

        public Text(String content, float x, float y, float scale) {
            this.content = content;
            this.x = x;
            this.y = y;

            newlyAdded = true;

            xSpacing = 1.05f;
            ySpacing = 1.15f;
            this.scale = scale;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setXSpacing(float spacing) {
            this.xSpacing = spacing;
        }

        public void setYSpacing(float spacing) {
            this.ySpacing = spacing;
        }

        public float getScale() {
            return scale;
        }

        public String getContent() {
            return content;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getXSpacing() {
            return xSpacing;
        }

        public float getYSpacing() {
            return ySpacing;
        }

        public boolean isNewlyAdded() {
            return newlyAdded;
        }

        public void setScale(float scale) {
            this.scale = scale;
        }
    }
}

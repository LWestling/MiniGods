package com.juse.minigods.map;

import org.joml.Vector3f;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by LukasW on 2018-03-11.
 * A terrain
 */

public class Terrain {
    public final static float WIDTH = 2.5f, HEIGHT = 2.5f;

    // replace with better later for some displacement
    public final static Vector3f SQUARE[] = {
            new Vector3f(0.f, 0.f, 0.f),
            new Vector3f(0.f, 1.f, 0.f),
            new Vector3f(1.f, 1.f, 0.f),

            new Vector3f(1.f, 1.f, 0.f),
            new Vector3f(1.f, 0.f, 0.f),
            new Vector3f(0.f, 1.f, 0.f),
    };

    private LinkedBlockingQueue<Vector3f> columns;
    private int rows;

    public Terrain(int rows, int columns) {
        this.columns = new LinkedBlockingQueue<>();
        this.rows = rows;

        createColumns(columns);
    }

    private void createColumns(int size) {
        Vector3f pos = new Vector3f(0.f, 0.f, 0.f);
        for (int i = 0; i < size; i++) {
            columns.add(pos);
            pos.x += WIDTH;
        }
    }

    private int getColumns() {
        return columns.size();
    }

    private int getRows() {
        return rows;
    }

    private LinkedBlockingQueue<Vector3f> getColumnPositions() {
        return columns;
    }
}

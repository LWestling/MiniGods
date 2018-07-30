package com.juse.minigods.map;

import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by LukasW on 2018-03-11.
 * A terrain, column by column since they will get deleted when the go past the screen.
 */

public class Terrain {
    enum TerrainType {
        GRASS, WATER, SLOPE
    }

    private LinkedBlockingQueue<ArrayList<TerrainType>> columns;
    private TerrainColumn renderColumns[];
    private Lock terrainUpdateLock;
    private Vector2f position;
    private Vector2i size;

    private float width;

    public Terrain(Vector2i size, Vector2f position) {
        this.columns = new LinkedBlockingQueue<>();
        this.position = position;
        this.size = size;

        this.width = getColumnWidth() * size.x();
        terrainUpdateLock = new ReentrantLock();

        createColumns();
    }

    private void createColumns() {
        renderColumns = new TerrainColumn[size.x()];
        TerrainColumn renderColumn = new TerrainColumn(size.y(), 0, position.x, position.x + width);

        for (int i = 0; i < size.x(); i++) {
            ArrayList<TerrainType> column = new ArrayList<>();
            for (int j = 0; j < size.y(); j++) {
                column.add(Math.random() < .5f ? TerrainType.GRASS : TerrainType.WATER);
            }
            this.columns.add(column);

            renderColumn.setOffset(size.x() + i * getColumnWidth());
            renderColumns[i] = new TerrainColumn(renderColumn);
        }
    }

    public void update(float dt, float terrainMovement) {
        terrainUpdateLock.lock();

        try {
            for (TerrainColumn renderColumn : renderColumns) {
                renderColumn.update(-dt * terrainMovement); // test
            }
        } finally {
            terrainUpdateLock.unlock();
        }
    }

    public float getWidth() {
        return width;
    }

    public int getColumnsSize() {
        return columns.size();
    }

    public float getColumnWidth() {
        return 2.f;
    }

    public float getColumnOffset(int index) {
        return getColumnsSize() * index;
    }

    public int getRows() {
        return columns.peek().size();
    }

    public LinkedBlockingQueue<ArrayList<TerrainType>> getColumns() {
        return columns;
    }

    public TerrainColumn[] getRenderColumns() {
        return renderColumns;
    }

    public Lock getTerrainUpdateLock() {
        return terrainUpdateLock;
    }

    public void reset() {
        for (int i = 0; i < renderColumns.length; i++) {
            renderColumns[i].setOffset(i * getColumnWidth() + position.x());
        }
    }
}

package com.juse.minigods.map;

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

    public Terrain(int rows, int columns, float startOffset) {
        this.columns = new LinkedBlockingQueue<>();
        terrainUpdateLock = new ReentrantLock();

        createColumns(rows, columns, startOffset);
    }

    private void createColumns(int rows, int columns, float startOffset) {
        renderColumns = new TerrainColumn[columns];
        TerrainColumn renderColumn = new TerrainColumn(rows, 0, startOffset, startOffset + columns * getColumnWidth());

        for (int i = 0; i < columns; i++) {
            ArrayList<TerrainType> column = new ArrayList<>();
            for (int j = 0; j < rows; j++) {
                column.add(Math.random() < 0.5f ? TerrainType.GRASS : TerrainType.WATER);
            }
            this.columns.add(column);

            renderColumn.setOffset(1.f + startOffset + i * getColumnWidth());
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

    public void reset(float startOffset) {
        for (int i = 0; i < renderColumns.length; i++) {
            renderColumns[i].setOffset(i * getColumnWidth() + startOffset);
        }
    }
}

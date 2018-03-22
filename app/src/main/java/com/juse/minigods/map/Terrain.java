package com.juse.minigods.map;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by LukasW on 2018-03-11.
 * A terrain
 */

public class Terrain {
    enum TerrainType {
        GRASS, WATER, SLOPE
    }

    private LinkedBlockingQueue<ArrayList<TerrainType>> columns;

    public Terrain(int rows, int columns) {
        this.columns = new LinkedBlockingQueue<>();

        createColumns(rows, columns);
    }

    private void createColumns(int rows, int columns) {
        ArrayList<TerrainType> column = new ArrayList<>();
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                column.add(Math.random() < 0.5f ? TerrainType.GRASS : TerrainType.WATER);
            }

            this.columns.add(column);
            column.clear();
        }
    }

    private int getColumnsSize() {
        return columns.size();
    }

    private int getRows() {
        return columns.peek().size();
    }

    private LinkedBlockingQueue<ArrayList<TerrainType>> getColumns() {
        return columns;
    }

    public void reset() {
    }
}

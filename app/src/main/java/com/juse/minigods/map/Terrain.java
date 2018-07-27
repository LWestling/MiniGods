package com.juse.minigods.map;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by LukasW on 2018-03-11.
 * A terrain, column by column since they will get deleted when the go past the screen.
 */

public class Terrain {
    enum TerrainType {
        GRASS, WATER, SLOPE
    }

    private LinkedBlockingQueue<ArrayList<TerrainType>> columns;
    private TerrainColumn renderColumn;

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

        renderColumn = new TerrainColumn(rows);
    }

    public int getColumnsSize() {
        return columns.size();
    }

    public float getColumnWidth() {
        return 2.f;
    }

    public int getRows() {
        return columns.peek().size();
    }

    public LinkedBlockingQueue<ArrayList<TerrainType>> getColumns() {
        return columns;
    }

    public TerrainColumn getRenderColumn() {
        return renderColumn;
    }

    public void reset() {
    }
}

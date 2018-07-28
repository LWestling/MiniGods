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
    private TerrainColumn renderColumns[];

    public Terrain(int rows, int columns, float startOffset) {
        this.columns = new LinkedBlockingQueue<>();

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
        for (TerrainColumn renderColumn : renderColumns) {
            renderColumn.update(-dt * terrainMovement); // test
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

    public void reset(float startOffset) {
        for (int i = 0; i < renderColumns.length; i++) {
            renderColumns[i].setOffset(i * getColumnWidth() + startOffset);
        }
    }
}

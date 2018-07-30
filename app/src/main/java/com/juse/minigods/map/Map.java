package com.juse.minigods.map;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class Map {
    private Vector2f position;
    private Vector2i gridSize;

    private Terrain terrain;
    private WaterGrid[] waterGrids;

    public Map(Vector2f topLeft, Vector2i gridSize) {
        terrain = new Terrain(gridSize, new Vector2f(topLeft.x(), 0.f));
        position = topLeft;
        this.gridSize = gridSize;

        createWaterGrid();
    }

    private void createWaterGrid() {
        waterGrids = new WaterGrid[2];
        int waterRows = 3;

        Vector2f scale = new Vector2f(terrain.getColumnWidth(), -1 * position.y() / waterRows);
        Vector2i size = new Vector2i(gridSize.x(), waterRows);

        waterGrids[0] = new WaterGrid(new Vector3f(position.x(), -0.25f, position.y()), scale, size);
        waterGrids[1] = new WaterGrid(new Vector3f(position.x(), -0.25f, gridSize.y()), scale, size);
    }

    public Vector2f position() {
        return position;
    }

    public Vector2i getGridSize() {
        return gridSize;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public WaterGrid[] getWaterGrids() {
        return waterGrids;
    }

    public void reset() {
        terrain.reset();
    }

    public void update(float dt, float mapSpeed) {
        terrain.update(dt, mapSpeed);
    }

    public Vector2f getPosition() {
        return position;
    }
}

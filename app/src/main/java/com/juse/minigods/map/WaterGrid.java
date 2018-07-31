package com.juse.minigods.map;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.ArrayList;

public class WaterGrid {
    private ArrayList<Vector3f> positions;
    private ArrayList<Integer> indices;
    private Vector3f position;
    private Vector2f scale;
    private Vector2i size;
    private Matrix4f world;
    
    WaterGrid(Vector3f position, Vector2f scale, Vector2i size) {
        this.position = position;
        this.scale = scale;
        this.size = size;

        world = new Matrix4f().translate(position).scale(scale.x, 1.f, scale.y);
        generateWaterGrid();
    }

    private void generateWaterGrid() {
        positions = new ArrayList<>();
        indices = new ArrayList<>();

        // Not optimized in anyway
        for (int z = 0; z < size.y + 1; z++) {
            for (int x = 0; x < size.x + 1; x++) {
                positions.add(new Vector3f(x, (float)Math.random() * 0.3f, z));
            }
        }

        for (int row = 0; row < size.y; row++) {
            for (int halfColumn = 0; halfColumn < size.x * 2; halfColumn++) { // half column
                if (halfColumn % 2 == 0) {
                    indices.add(toIndex(halfColumn / 2, row));
                    indices.add(toIndex(halfColumn / 2 + 1, row));
                    indices.add(toIndex(halfColumn / 2, row + 1));
                } else {
                    indices.add(toIndex(halfColumn / 2 + 1, row));
                    indices.add(toIndex(halfColumn / 2 + 1, row + 1));
                    indices.add(toIndex(halfColumn / 2, row + 1));
                }
            }
        }
    }

    public ArrayList<Vector3f> getPositions() {
        return positions;
    }

    public ArrayList<Integer> getIndices() {
        return indices;
    }

    private int toIndex(int column, int row) {
        return column + row * (size.x + 1);
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector2f getScale() {
        return scale;
    }

    public Vector2i getSize() {
        return size;
    }

    public Matrix4f getWorld() {
        return world;
    }
}

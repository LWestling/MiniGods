package com.juse.minigods.map;

import org.joml.Vector3f;

import java.util.ArrayList;

public class TerrainColumn {
    private static final int TRIANGLE_SIZE = 3;
    private ArrayList<Vector3f> columnVertices;
    private ArrayList<Integer> columnIndices;

    TerrainColumn(int height) {
        generateTerrain(height);
    }

    private void generateTerrain(int height) {
        columnVertices = new ArrayList<>();
        columnIndices = new ArrayList<>();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < 2; col++) {
                columnVertices.add(new Vector3f(col, 0.f, row));
            }
        }

        for (int tri = 0; tri < height * 2; tri++) {
            if (tri % 2 == 0) { // if even it's tri, tri + 1, tri + 2
                for (int i = 0; i < TRIANGLE_SIZE; i++)
                    columnIndices.add(tri + i);
            } else { // if odd it's tri + 2, tri + 1, tri + 3 (backwards and then loop back)
                for (int i = TRIANGLE_SIZE - 1; i > - TRIANGLE_SIZE - 1; i--)
                    columnIndices.add(tri + (i < 0 ? -i : i));
            }
        }
    }

    public ArrayList<Vector3f> getColumnVertices() {
        return columnVertices;
    }

    public ArrayList<Integer> getColumnIndices() {
        return columnIndices;
    }
}

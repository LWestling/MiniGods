package com.juse.minigods.map;

import com.flowpowered.noise.Noise;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.ArrayList;

public class WaterGrid {
    private int seed;
    private ArrayList<Float> vertexData;
    private Vector2i size;
    private Matrix4f world;
    
    WaterGrid(Vector3f position, Vector2f scale, Vector2i size) {
        this.size = size;
        this.seed = (int)(Math.random() * Integer.MAX_VALUE);

        world = new Matrix4f().translate(position).scale(scale.x, 1.f, scale.y);
        generateWaterGrid();
    }

    private void generateWaterGrid() {
        vertexData = new ArrayList<>();

        ArrayList<Vector3f> normals = new ArrayList<>();
        for (int row = 0; row < size.y; row++) {
            for (int halfColumn = 0; halfColumn < (size.x + 1) * 2; halfColumn++) { // half column
                Vector3f pos1, pos2, pos3;
                if (halfColumn % 2 == 0) {
                    pos1 = new Vector3f(halfColumn / 2, 0.f, row);
                    pos1.y = getHeight((int) pos1.x(), (int) pos1.z());
                    pos2 = new Vector3f(halfColumn / 2 + 1, 0.f, row);
                    pos2.y = getHeight((int) pos2.x(), (int) pos2.z());
                    pos3 = new Vector3f(halfColumn / 2, 0.f, row + 1);
                    pos3.y = getHeight((int) pos3.x(), (int) pos3.z());
                    addVertexData(pos1, pos2, pos3);
                } else {
                    pos1 = new Vector3f(halfColumn / 2 + 1, 0.f, row);
                    pos1.y = getHeight((int) pos1.x(), (int) pos1.z());
                    pos2 = new Vector3f(halfColumn / 2 + 1, 0.f, row + 1);
                    pos2.y = getHeight((int) pos2.x(), (int) pos2.z());
                    pos3 = new Vector3f(halfColumn / 2, 0.f, row + 1);
                    pos3.y = getHeight((int) pos3.x(), (int) pos3.z());
                    addVertexData(pos1, pos2, pos3);
                }
                Vector3f normal = new Vector3f(pos3).sub(pos1).cross(pos2.sub(pos1));
                normals.add(normal);
                normals.add(normal);
                normals.add(normal);
            }
        }

        for (Vector3f normal : normals) {
            addVertexData(normal);
        }
        // Not optimized in anyway
        /*
        Vector3f position = new Vector3f();
        for (int z = 0; z < size.y + 1; z++) {
            for (int x = 0; x < size.x + 1; x++) {
                position.set(x, (float) Math.random() * 0.3f, z);
                vertexData.add(position.x());
                vertexData.add(position.y());
                vertexData.add(position.z());
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
        */
    }

    private float getHeight(int x, int y) {
        return (float) Noise.valueNoise3D(x, 0, y, seed);
    }

    private void addVertexData(Vector3f... vectors) {
        for (Vector3f vec : vectors) {
            vertexData.add(vec.x());
            vertexData.add(vec.y());
            vertexData.add(vec.z());
        }
    }

    public ArrayList<Float> getVertexData() {
        return vertexData;
    }

    public Vector2i getSize() {
        return size;
    }

    public Matrix4f getWorld() {
        return world;
    }
}

package com.juse.minigods.map;

import com.flowpowered.noise.Noise;
import com.flowpowered.noise.NoiseQuality;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WaterGrid {
    private FloatBuffer vertexBuffer;
    private Vector2i size;
    private Matrix4f world;
    private Lock vertexDataUpdateLock;

    private int seed;
    private float offset;
    
    WaterGrid(Vector3f position, Vector2f scale, Vector2i size) {
        this.offset = 0;
        this.size = size;
        this.seed = (int)(Math.random() * Integer.MAX_VALUE);
        vertexDataUpdateLock = new ReentrantLock();

        world = new Matrix4f().translate(position).scale(scale.x, 1.f, scale.y);

        vertexBuffer = ByteBuffer
                .allocateDirect(size.x * size.y * 6 * 2 * 3 * Float.BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        generateWaterGrid(offset);
    }

    private void generateWaterGrid(float offset) {
        vertexDataUpdateLock.lock();

        try {
            vertexBuffer.position(0);
            ArrayList<Vector3f> normals = new ArrayList<>();
            for (int row = 0; row < size.y; row++) {
                for (int halfColumn = 0; halfColumn < size.x * 2; halfColumn++) { // half column
                    Vector3f pos1, pos2, pos3;
                    if (halfColumn % 2 == 0) {
                        pos1 = new Vector3f(halfColumn / 2, 0.f, row);
                        pos2 = new Vector3f(halfColumn / 2 + 1, 0.f, row);
                        pos3 = new Vector3f(halfColumn / 2, 0.f, row + 1);

                        loadHeights(offset, pos1, pos2, pos3);
                        addVertexData(pos1, pos2, pos3);
                    } else {
                        pos1 = new Vector3f(halfColumn / 2 + 1, 0.f, row);
                        pos2 = new Vector3f(halfColumn / 2 + 1, 0.f, row + 1);
                        pos3 = new Vector3f(halfColumn / 2, 0.f, row + 1);

                        loadHeights(offset, pos1, pos2, pos3);
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

            vertexBuffer.flip();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            vertexDataUpdateLock.unlock();
        }
    }

    private void loadHeights(float xOffset, Vector3f... vector3fs) {
        for (Vector3f vector3f : vector3fs) {
            vector3f.y = getHeight(vector3f.x() + xOffset, vector3f.z());
        }
    }

    public void update(float dt) {
        offset += dt * 0.75f;

        // This is SUPER INEFFICIENT, ONLY FOR TESTING
        generateWaterGrid(offset);
    }

    private float getHeight(float x, float y) {
        return (float) Noise.valueCoherentNoise3D(x, 0.f, y, seed, NoiseQuality.BEST);
    }

    private void addVertexData(Vector3f... vectors) {
        for (Vector3f vec : vectors) {
            vertexBuffer.put(vec.x());
            vertexBuffer.put(vec.y());
            vertexBuffer.put(vec.z());
        }
    }

    public FloatBuffer getVertexData() {
        return vertexBuffer;
    }

    public Vector2i getSize() {
        return size;
    }

    public Matrix4f getWorld() {
        return world;
    }

    public Lock getVertexDataUpdateLock() {
        return vertexDataUpdateLock;
    }
}

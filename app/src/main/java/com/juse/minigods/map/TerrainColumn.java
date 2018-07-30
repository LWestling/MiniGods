package com.juse.minigods.map;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class TerrainColumn implements Cloneable{
    private static final int TRIANGLE_SIZE = 3;
    private float offset, minOffset, resetOffset;
    private ArrayList<VertexData> columnVertexData;
    private ArrayList<Integer> columnIndices;

    TerrainColumn(int height, float startOffset, float minOffset, float resetOffset) {
        generateTerrain(height);
        this.offset = startOffset;
        this.minOffset = minOffset;
        this.resetOffset = resetOffset;
    }

    /**
     * Copies what needs to be
     * @param terrainColumn column to copy values from
     */
    TerrainColumn(TerrainColumn terrainColumn) {
        offset = terrainColumn.offset;
        minOffset = terrainColumn.minOffset;
        resetOffset = terrainColumn.resetOffset;

        columnVertexData = new ArrayList<>();
        for (VertexData vertexData : terrainColumn.columnVertexData) {
            columnVertexData.add(new VertexData(vertexData));
        }
        columnIndices = terrainColumn.columnIndices;
    }

    private void generateTerrain(int height) {
        columnVertexData = new ArrayList<>();
        columnIndices = new ArrayList<>();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < 2; col++) {
                columnVertexData.add(
                        new VertexData(
                                new Vector3f(col, 0.f, row),
                                new Vector2f(col, row)
                        )
                );
            }
        }

        // bottom row towards player to block view
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                columnVertexData.add(
                        new VertexData(
                                new Vector3f(col, -row, height - 1 + row),
                                new Vector2f(col, row)
                        )
                );
            }
        }

        for (int tri = 0; tri < (height + 1) * 2 /* Two extra triangle for bottom*/; tri++) {
            if (tri % 2 == 0) { // if even it's tri, tri + 1, tri + 2
                for (int i = 0; i < TRIANGLE_SIZE; i++)
                    columnIndices.add(tri + i);
            } else { // if odd it's tri + 2, tri + 1, tri + 3 (backwards and then loop back)
                for (int i = TRIANGLE_SIZE - 1; i > - TRIANGLE_SIZE - 1; i--)
                    columnIndices.add(tri + (i < 0 ? -i : i));
            }
        }
    }

    public FloatBuffer getVertexData() {
        // java is so efficient sometimes
        FloatBuffer floatBuffer = ByteBuffer
                .allocateDirect(columnVertexData.size() * Float.BYTES * 5)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        for (int i = 0; i < columnVertexData.size(); i++) {
            VertexData vertexData = columnVertexData.get(i);

            floatBuffer.put(vertexData.position.x());
            floatBuffer.put(vertexData.position.y());
            floatBuffer.put(vertexData.position.z());

            floatBuffer.put(vertexData.texCoordinate.x());
            floatBuffer.put(vertexData.texCoordinate.y());
        }

        floatBuffer.flip();
        return floatBuffer;
    }

    public ArrayList<VertexData> getColumnVertexData() {
        return columnVertexData;
    }

    public ArrayList<Integer> getColumnIndices() {
        return columnIndices;
    }

    public float getOffset() {
        return offset;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public void update(float moveOffset) {
        offset += moveOffset;

        if (offset < minOffset) {
            float diff = offset - minOffset;
            offset = resetOffset + diff;
        }
    }

    public class VertexData {
        private Vector3f position;
        private Vector2f texCoordinate;

        public VertexData(Vector3f position, Vector2f texCoordinate) {
            this.position = position;
            this.texCoordinate = texCoordinate;
        }

        public VertexData(VertexData vertexData) {
            this.position = vertexData.position;
            this.texCoordinate = vertexData.texCoordinate;
        }

        public Vector3f getPosition() {
            return position;
        }

        public Vector2f getTexCoordinate() {
            return texCoordinate;
        }
    }
}

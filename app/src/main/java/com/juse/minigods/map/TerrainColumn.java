package com.juse.minigods.map;

import com.flowpowered.noise.Noise;
import com.flowpowered.noise.NoiseQuality;
import com.juse.minigods.Utils.GlobalGameSeed;
import com.juse.minigods.rendering.renderers.RendererInterface;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class TerrainColumn implements Cloneable{
    private final static float TEXTURE_COUNT = 1.f;
    private int height, loops;
    private float offset, startOffset, minOffset, resetOffset, columnWidth;
    private ArrayList<VertexData> columnVertexData;
    private float heights[][];

    TerrainColumn(int height, float startOffset, float minOffset, float resetOffset, float columnWidth) {
        this.height = height;

        this.offset = startOffset;
        this.minOffset = minOffset;

        this.startOffset = startOffset;
        this.resetOffset = resetOffset;

        this.columnWidth = columnWidth;
        loops = 0;

        heights = new float[2][height / 2 + 2];
        generateHeights();
        generateTerrain(height);
    }

    private void generateHeights() {
        for (int x = 0; x < 2; x++) {
            float xPos = x + (loops * (resetOffset - minOffset) + startOffset) / columnWidth;
            System.out.println(getHeight(xPos, 0) + ":" + xPos + ":" + loops);
            for (int z = 0; z < height / 2 + 2; z++) {
                heights[x][z] = getHeight(xPos, z);
            }
        }
    }

    private void addTriangleVertexData(int row, float endYOffset, float endZOffset,
                                       float triangleHeight, float triangleWidth,
                                       int textureTop, int textureBot) {
        float texX = startOffset - minOffset, texEndX = texX;

        float z = row * triangleHeight;
        float endZ = z + triangleHeight + endZOffset;

        Vector3f vecs[] = new Vector3f[4];
        vecs[0] = new Vector3f(0.f, heights[0][row] , z);
        vecs[1] = new Vector3f(triangleWidth, heights[1][row], z);
        vecs[2] = new Vector3f(0.f, heights[0][row + 1] + endYOffset, endZ);
        vecs[3] = new Vector3f(triangleWidth, heights[1][row + 1] + endYOffset, endZ);

        // ugh
        Vector3f normalTop = new Vector3f(vecs[0]).sub(vecs[2]).cross(new Vector3f(vecs[0]).sub(vecs[1])).normalize();
        Vector3f normalBot = new Vector3f(vecs[1]).sub(vecs[2]).cross(new Vector3f(vecs[1]).sub(vecs[3])).normalize();

        // top
        columnVertexData.add(
                new VertexData(
                        vecs[0],
                        normalTop,
                        new Vector2f(texX, 0.f)
                )
        );
        columnVertexData.add(
                new VertexData(
                        vecs[1],
                        normalTop,
                        new Vector2f(texEndX, 0.f)
                )
        );
        columnVertexData.add(
                new VertexData(
                        vecs[2],
                        normalTop,
                        new Vector2f(texX, 1.f)
                )
        );

        texX = textureBot / TEXTURE_COUNT;
        texEndX = (textureBot + 1) / TEXTURE_COUNT;

        // bot
        columnVertexData.add(
                new VertexData(
                        vecs[1],
                        normalBot,
                        new Vector2f(texEndX, 0)
                )
        );
        columnVertexData.add(
                new VertexData(
                        vecs[3],
                        normalBot,
                        new Vector2f(texEndX, 1.f)
                )
        );
        columnVertexData.add(
                new VertexData(
                        vecs[2],
                        normalBot,
                        new Vector2f(texX, 1.f)
                )
        );
    }

    private void addTriangleVertexData2(int row, float endYOffset,
                                        float triangleHeight, float triangleWidth,
                                        int textureTop, int textureBot) {
        // float texX = textureTop / TEXTURE_COUNT, texEndX = (textureTop + 1) / TEXTURE_COUNT;
        float texX = startOffset - minOffset, texEndX = texX;

        float z = row * triangleHeight;
        float endZ = z + triangleHeight;

        Vector3f vecs[] = new Vector3f[4];
        vecs[0] = new Vector3f(0.f, heights[0][row], z);
        vecs[1] = new Vector3f(triangleWidth, heights[1][row], z);
        vecs[2] = new Vector3f(0.f, heights[0][row + 1] + endYOffset, endZ);
        vecs[3] = new Vector3f(triangleWidth, heights[1][row + 1] + endYOffset, endZ);

        // ugh
        Vector3f normalTop = new Vector3f(vecs[0]).sub(vecs[2]).cross(new Vector3f(vecs[0]).sub(vecs[3])).normalize();
        Vector3f normalBot = new Vector3f(vecs[0]).sub(vecs[3]).cross(new Vector3f(vecs[0]).sub(vecs[1])).normalize();

        // top
        columnVertexData.add(
                new VertexData(
                        vecs[0],
                        normalTop,
                        new Vector2f(texX, 0.f)
                )
        );
        columnVertexData.add(
                new VertexData(
                        vecs[3],
                        normalTop,
                        new Vector2f(texEndX, 0.f)
                )
        );
        columnVertexData.add(
                new VertexData(
                        vecs[2],
                        normalTop,
                        new Vector2f(texX, 1.f)
                )
        );

        texX = textureBot / TEXTURE_COUNT;
        texEndX = (textureBot + 1) / TEXTURE_COUNT;

        // bot
        columnVertexData.add(
                new VertexData(
                        vecs[0],
                        normalBot,
                        new Vector2f(texEndX, row)
                )
        );
        columnVertexData.add(
                new VertexData(
                        vecs[1],
                        normalBot,
                        new Vector2f(texEndX, 1.f)
                )
        );
        columnVertexData.add(
                new VertexData(
                        vecs[3],
                        normalBot,
                        new Vector2f(texX, 1.f)
                )
        );
    }

    private void generateTerrain(int height) {
        columnVertexData = new ArrayList<>();
        float triangleHeight = 2.f;

        for (int row = 0; row < height / 2; row++) {
          //  if (Math.random() < 0.5f)
           //     addTriangleVertexData2(row, 0.f, triangleHeight, 1.f,
            //            (int) (Math.random() * TEXTURE_COUNT), (int) (Math.random() * TEXTURE_COUNT));
            // else
                addTriangleVertexData(row, 0.f, 0.f, triangleHeight, 1.f,
                        (int) (Math.random() * TEXTURE_COUNT), (int) (Math.random() * TEXTURE_COUNT));
        }

        addTriangleVertexData(height / 2, -14.f, 15.f, triangleHeight,
                1.f, 0, 0);
    }

    public FloatBuffer getVertexData() {
        // java is so efficient sometimes, should probably just use this instead of using a VertexData class at all..
        FloatBuffer floatBuffer = ByteBuffer
                .allocateDirect(columnVertexData.size() * RendererInterface.FLOAT_BYTES * 8)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        for (int i = 0; i < columnVertexData.size(); i++) {
            VertexData vertexData = columnVertexData.get(i);

            floatBuffer.put(vertexData.position.x());
            floatBuffer.put(vertexData.position.y());
            floatBuffer.put(vertexData.position.z());

            floatBuffer.put(vertexData.normal.x());
            floatBuffer.put(vertexData.normal.y());
            floatBuffer.put(vertexData.normal.z());

            floatBuffer.put(vertexData.texCoordinate.x());
            floatBuffer.put(vertexData.texCoordinate.y());
        }

        floatBuffer.flip();
        return floatBuffer;
    }

    private float getHeight(float x, float z) {
        return ((float) Noise.valueCoherentNoise3D(x, 0.f, z, GlobalGameSeed.SEED, NoiseQuality.FAST) - .5f) * .4f;
    }

    public ArrayList<VertexData> getColumnVertexData() {
        return columnVertexData;
    }

    public float getOffset() {
        return offset;
    }

    public void resetOffset(float offset) {
        this.offset = offset;
        this.startOffset = offset;
        this.loops = 0;

        generateHeights();
        generateTerrain(height);
    }

    public void update(float moveOffset) {
        offset += moveOffset;

        if (offset < minOffset) {
            float diff = offset - minOffset;
            offset = resetOffset + diff;
            loops++;

            generateHeights();
            generateTerrain(height);
        }
    }

    public class VertexData {
        private Vector3f position;
        private Vector3f normal;
        private Vector2f texCoordinate;

        VertexData(Vector3f position, Vector3f normal, Vector2f texCoordinate) {
            this.position = position;
            this.normal = normal;
            this.texCoordinate = texCoordinate;
        }

        VertexData(VertexData vertexData) {
            this.position = vertexData.position;
            this.texCoordinate = vertexData.texCoordinate;
            this.normal = vertexData.normal;
        }

        public Vector3f getPosition() {
            return position;
        }

        public Vector2f getTexCoordinate() {
            return texCoordinate;
        }
    }
}

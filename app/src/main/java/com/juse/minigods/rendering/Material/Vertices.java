package com.juse.minigods.rendering.Material;

import android.opengl.GLES31;

import java.nio.FloatBuffer;

public class Vertices {
    private final static int FLOAT_SIZE = 4, VECTOR_SIZE = 3; // 4 bytes

    private int vertexLocation, vertexCount, vertexOffset;
    private int drawFlag;
    private int vbo[], vao[];

    private FloatBuffer vertexBuffer;

     Vertices(FloatBuffer vertexBuffer, int vertexLocation,
                    int vertexCount, int vertexOffset, int drawFlag) {
        this.vertexBuffer = vertexBuffer;
        this.vertexLocation = vertexLocation;
        this.vertexCount = vertexCount;
        this.vertexOffset = vertexOffset;
        this.drawFlag = drawFlag;

        createData();
    }

    public void deleteData() {
        GLES31.glDeleteBuffers(vbo.length, vbo, 0);
        GLES31.glDeleteVertexArrays(vao.length, vao, 0);
    }

    public void updateData(FloatBuffer newData) {
        // no sync
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vbo[0]);
        GLES31.glBufferSubData(GLES31.GL_ARRAY_BUFFER, getVertexOffset(), vertexBuffer.capacity() * FLOAT_SIZE, newData);
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, 0);

        /*
        ByteBuffer buffer = (ByteBuffer) GLES31.glMapBufferRange(GLES31.GL_ARRAY_BUFFER, getVertexOffset(),
                vertexBuffer.capacity() * FLOAT_SIZE, GLES31.GL_MAP_WRITE_BIT | GLES30.GL_MAP_UNSYNCHRONIZED_BIT);
        buffer.position(0);
        buffer.asFloatBuffer().put(newData); */
    }

    private void createData() {
        this.vbo = new int[1]; // combine vbo and vao indices?

        GLES31.glGenBuffers(vbo.length, vbo, 0);
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vbo[0]);
        GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER,
                vertexBuffer.capacity() * FLOAT_SIZE, vertexBuffer, drawFlag);

        createVAO();
    }

    private void createVAO() {
        this.vao = new int[1];

        GLES31.glGenVertexArrays(vao.length, vao, 0);
        GLES31.glBindVertexArray(vao[0]); // Another example of pretty weird java code, c++ <3
        GLES31.glEnableVertexAttribArray(0); // uses currently bound vao
        GLES31.glVertexAttribPointer(getVertexLocation(), VECTOR_SIZE, GLES31.GL_FLOAT, false, 0, getVertexOffset());
    }

    public int getVertexLocation() {
        return vertexLocation;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getVertexOffset() {
        return vertexOffset;
    }

    public int[] getVbo() {
        return vbo;
    }

    public int[] getVao() {
        return vao;
    }

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }
}


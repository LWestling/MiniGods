package com.juse.minigods.rendering.Material;

import android.opengl.GLES31;

import java.nio.FloatBuffer;

public class Vertices {
    private final static int FLOAT_SIZE = 4, VECTOR_SIZE = 3; // 4 bytes

    private int vertexLocation, vertexCount, vertexOffset;
    private int vbo[], vao[];

    private FloatBuffer vertexBuffer;

    public Vertices(FloatBuffer vertexBuffer, int vertexLocation, int vertexCount, int vertexOffset) {
        this.vertexBuffer = vertexBuffer;
        this.vertexLocation = vertexLocation;
        this.vertexCount = vertexCount;

        createData();
    }

    public void deleteData() {
        GLES31.glDeleteBuffers(vbo.length, vbo, 0);
        GLES31.glDeleteVertexArrays(vao.length, vao, 0);
    }

    private void createData() {
        this.vbo = new int[1]; // combine vbo and vao indices?

        GLES31.glGenBuffers(vbo.length, vbo, 0);
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vbo[0]);
        GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER,
                vertexBuffer.capacity() * FLOAT_SIZE, vertexBuffer, GLES31.GL_STATIC_DRAW);

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


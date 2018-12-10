package com.juse.minigods.rendering.Material;

import android.opengl.GLES31;

import java.nio.FloatBuffer;

public class Vertices {
    private int vertexCount, attributeSize[], vertexLocations[], stride[], offset[];
    private int drawFlag;
    private int vbo[], vao[];

    private FloatBuffer vertexBuffer;

     Vertices(FloatBuffer vertexBuffer, int vertexCount, int drawFlag, int attributeSize[], int vertexLocations[], int stride[], int offset[]) {
        this.vertexBuffer = vertexBuffer;
        this.vertexCount = vertexCount;
        this.attributeSize = attributeSize;
        this.vertexLocations = vertexLocations;
        this.stride = stride;
        this.offset = offset;
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
        GLES31.glBufferSubData(GLES31.GL_ARRAY_BUFFER, 0, vertexBuffer.capacity() * 4, newData);
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, 0);
    }

    private void createData() {
        this.vbo = new int[1]; // combine vbo and vao indices?

        GLES31.glGenBuffers(vbo.length, vbo, 0);
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vbo[0]);
        GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, drawFlag);

        createVAO();
    }

    // Move VAO to the shader as this is like the input layout (per shader)
    private void createVAO() {
        this.vao = new int[1];

        GLES31.glGenVertexArrays(vao.length, vao, 0);
        GLES31.glBindVertexArray(vao[0]); // Another example of pretty weird java code, c++ <3

        for (int i = 0; i < vertexLocations.length; i++) {
            GLES31.glEnableVertexAttribArray(vertexLocations[i]); // uses currently bound vao
            GLES31.glVertexAttribPointer(vertexLocations[i], attributeSize[i], GLES31.GL_FLOAT, false, stride[i], offset[i]);
        }
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int[] getVao() {
        return vao;
    }
}


package com.juse.minigods.rendering;

import android.opengl.GLES31;

import java.nio.FloatBuffer;

/**
 * Created by LukasW on 2018-03-08.
 * A material, it has a id to a render pass it will use and its data
 */

public class Material {
    private final static int FLOAT_SIZE = 4, VECTOR_SIZE = 3; // 4 bytes
    private FloatBuffer floatBuffer;
    private int renderPass;
    private int vertexLocation;
    private int vertexCount;
    private int vbo[], vao[];

    private FloatBuffer uniformBuffers[]; // change to one float buffer with all shiet in?
    private int uniformLocations[];

    public Material(int renderPass, FloatBuffer floatBuffer, int vertexLocation, int vertexCount) {
        this.floatBuffer = floatBuffer;
        this.renderPass = renderPass;
        this.vertexLocation = vertexLocation;
        this.vertexCount = vertexCount;

        uniformLocations = new int[0];
        createOGLData();
    }

    public Material(int renderPass, FloatBuffer floatBuffer, int vertexLocation,
                    int vertexCount, FloatBuffer uniformBuffer, int uniformLocation) {
        this.floatBuffer = floatBuffer;
        this.renderPass = renderPass;
        this.vertexLocation = vertexLocation;
        this.vertexCount = vertexCount;

        setUniforms(new FloatBuffer[] {uniformBuffer}, new int[] {uniformLocation});
        createOGLData();
    }

    // only matrix 4f right now
    public void setUniforms(FloatBuffer buffers[], int locations[]) {
        uniformBuffers = buffers;
        uniformLocations = locations;
    }

    /*
    private void createUBO() {
        ubo = new int[uniformBuffers.length];
        GLES31.glGenBuffers(ubo.length, ubo, 0);

        for (int i = 0; i < ubo.length; i++) {
            GLES31.glBindBuffer(GLES31.GL_UNIFORM_BUFFER, ubo[i]);
            GLES31.glBufferData(GLES31.GL_UNIFORM_BUFFER, uniformBuffers[i].capacity() * FLOAT_SIZE,
                    uniformBuffers[i], GLES31.GL_DYNAMIC_DRAW); // does the hint do anything?
        }
    } */

    public void updateUniform(FloatBuffer buffer, int index) {
        uniformBuffers[index] = buffer;
    }

    public void deleteData() {
        GLES31.glDeleteBuffers(vbo.length, vbo, 0);
        GLES31.glDeleteVertexArrays(vao.length, vao, 0);
    }

    private void createOGLData() {
        this.vbo = new int[1]; // combine vbo and vao indices?

        GLES31.glGenBuffers(vbo.length, vbo, 0);
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vbo[0]);
        GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER,
                floatBuffer.capacity() * FLOAT_SIZE, floatBuffer, GLES31.GL_STATIC_DRAW);

        createVAO();
    }

    private void createVAO() {
        this.vao = new int[1];

        GLES31.glGenVertexArrays(vao.length, vao, 0);
        GLES31.glBindVertexArray(vao[0]); // Another example of pretty weird java code, c++ <3
        GLES31.glEnableVertexAttribArray(0); // uses currently bound vao
        GLES31.glVertexAttribPointer(vertexLocation, VECTOR_SIZE, GLES31.GL_FLOAT, false, 0, getVertexOffset());
    }

    public FloatBuffer getFloatBuffer() {
        return floatBuffer;
    }

    public int getRenderPass() {
        return renderPass;
    }

    public int[] getVBO() {
        return vbo;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getVertexOffset() {
        return 0; // todo
    }

    public int[] getVAO() {
        return vao;
    }

    public int[] getUniformLocations() {
        return uniformLocations;
    }

    public FloatBuffer[] getUniformBuffers() {
        return uniformBuffers;
    }
}

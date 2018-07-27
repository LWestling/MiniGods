package com.juse.minigods.rendering.Material;

import android.opengl.GLES20;
import android.opengl.GLES31;

import java.nio.IntBuffer;

public class Indices {
    private IntBuffer intBuffer; // Can be replaced with ShortBuffer
    private int glBuffer[];
    private int offset, size;

    Indices(IntBuffer intBuffer, int offset, int size) {
        this.intBuffer = intBuffer;
        this.offset = offset;
        this.size = size;

        createData();
    }

    private void createData() {
        glBuffer = new int[1];

        GLES31.glGenBuffers(glBuffer.length, glBuffer, 0);
        GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, glBuffer[0]);
        GLES31.glBufferData(GLES31.GL_ELEMENT_ARRAY_BUFFER, size * Integer.BYTES, intBuffer, GLES20.GL_STATIC_DRAW);
    }

    public int getOffset() {
        return offset;
    }

    public int getSize() {
        return size;
    }

    public int getGlBufferLocation() {
        return glBuffer[0];
    }
}

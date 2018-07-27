package com.juse.minigods.rendering.Material;

import java.nio.FloatBuffer;

public class Uniforms {
    private FloatBuffer uniformBuffers[]; // change to one float buffer with all data in? (TODO)
    private int uniformLocations[];

    Uniforms(int uniformLocations[], FloatBuffer... uniformBuffers) {
        this.uniformBuffers = uniformBuffers;
        this.uniformLocations = uniformLocations;
    }

    public void updateUniform(FloatBuffer buffer, int index) {
        uniformBuffers[index] = buffer;
    }

    public FloatBuffer[] getUniformBuffers() {
        return uniformBuffers;
    }

    public int[] getUniformLocations() {
        return uniformLocations;
    }
}


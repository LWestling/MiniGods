package com.juse.minigods.rendering.Material;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class MaterialBuilder {
    private Uniforms uniforms;
    private Vertices vertices;
    private Indices indices;

    public MaterialBuilder() {
        uniforms = null;
        vertices = null;
        indices = null;
    }

    public void addVertices(FloatBuffer vertexBuffer, int vertexCount, int drawFlag,
                            int vertexLocations[], int strides[], int offsets[]) {
        vertices = new Vertices(vertexBuffer, vertexCount, drawFlag, vertexLocations, strides, offsets);
    }

    public void addUniforms(int uniformLocations[], FloatBuffer... uniformBuffers) {
        uniforms = new Uniforms(uniformLocations, uniformBuffers);
    }

    public void addIndices(IntBuffer intBuffer, int size, int offset) {
        indices = new Indices(intBuffer, offset, size);
    }

    public Vertices getVertices() {
        return vertices;
    }

    public Uniforms getUniforms() {
        return uniforms;
    }

    public Indices getIndices() {
        return indices;
    }
}

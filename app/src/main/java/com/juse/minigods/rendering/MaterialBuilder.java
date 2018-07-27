package com.juse.minigods.rendering;

import com.juse.minigods.rendering.Material.Indices;
import com.juse.minigods.rendering.Material.Uniforms;
import com.juse.minigods.rendering.Material.Vertices;

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

    public void addVertices(FloatBuffer vertexBuffer, int vertexLocation, int vertexCount, int vertexOffset) {
        vertices = new Vertices(vertexBuffer, vertexLocation, vertexCount, vertexOffset);
    }

    public void addUniforms(FloatBuffer uniformBuffers[], int uniformLocations[]) {
        uniforms = new Uniforms(uniformBuffers, uniformLocations);
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

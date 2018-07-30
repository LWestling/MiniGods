package com.juse.minigods.rendering.Material;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class MaterialBuilder {
    private Uniforms uniforms;
    private Vertices vertices;
    private Indices indices;
    private ImageTexture imageTexture;

    public MaterialBuilder() {
        uniforms = null;
        vertices = null;
        indices = null;
        imageTexture = null;
    }

    public void setVertices(FloatBuffer vertexBuffer, int vertexCount, int drawFlag,
                            int attributeSize[], int vertexLocations[], int strides[], int offsets[]) {
        vertices = new Vertices(vertexBuffer, vertexCount, drawFlag, attributeSize, vertexLocations, strides, offsets);
    }

    public void setUniforms(int uniformLocations[], FloatBuffer... uniformBuffers) {
        uniforms = new Uniforms(uniformLocations, uniformBuffers);
    }

    public void setIndices(IntBuffer intBuffer, int size, int offset) {
        indices = new Indices(intBuffer, offset, size);
    }

    public void setImageTexture(ImageTexture imageTexture) {
        this.imageTexture = imageTexture;
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

    public ImageTexture getImageTexture() {
        return imageTexture;
    }
}

package com.juse.minigods.rendering.Material;

/**
 * Created by LukasW on 2018-03-08.
 * A material, it has a id to a render pass it will use and its data
 */

public class Material {
    private int renderPass;

    private Uniforms uniforms; // Instead of material class just use these to make a more data oriented approach. Something to think about.
    private Vertices vertices;
    private Indices indices;

    public Material(MaterialBuilder materialBuilder) {
        // is null if not added
        vertices = materialBuilder.getVertices();
        uniforms = materialBuilder.getUniforms();
        indices = materialBuilder.getIndices();
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

    public int getRenderPass() {
        return renderPass;
    }

    public Uniforms getUniforms() {
        return uniforms;
    }

    public Vertices getVertices() {
        return vertices;
    }

    public Indices getIndices() {
        return indices;
    }
}

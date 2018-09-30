package com.juse.minigods.rendering.model;

import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.opengl.GLES31;

import com.juse.minigods.Utils.DataUtils;
import com.juse.minigods.rendering.Material.ImageTexture;
import com.juse.minigods.rendering.Material.Material;
import com.juse.minigods.rendering.Material.MaterialBuilder;
import com.juse.minigods.reporting.CrashManager;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_STATIC_DRAW;

/**
 * Helper class to create animatedModelMaterial
 */
public class AnimatedModelMaterial {
    private final static int VERTEX_FLOATS = 8;

    private Material material;
    private Model model;

    private float time;
    private int currentAnimation;

    private Buffer buffer;
    private int vbo[], vao[];

    private Uniform uniforms[];

    public AnimatedModelMaterial(Model model, String animation) {
        this.model = model;
        this.uniforms = new Uniform[2];
        currentAnimation = model.getAnimationIndex(animation);
    }

    public void buildMaterial(int renderPass, AssetManager assetManager, MaterialBuilder builder, AnimatedShaderInfo info) {
        // vec3, vec3, vec2, ivec4, vec4
        ByteBuffer byteBuffer = ByteBuffer
                .allocateDirect(getVertexCount() * (Float.BYTES * 8 + Integer.BYTES * 4 + Float.BYTES * 4))
                .order(ByteOrder.nativeOrder());
        for (int vertex = 0; vertex < getVertexCount(); vertex++) {
            for (int comp = 0; comp < VERTEX_FLOATS; comp++) {
                byteBuffer.putFloat(model.vertices[vertex * VERTEX_FLOATS + comp]);
            }
            for (int comp = 0; comp < 4; comp++) {
                byteBuffer.putInt(model.boneIds[vertex][comp]);
            }
            for (int comp = 0; comp < 4; comp++) {
                byteBuffer.putFloat(model.boneWeights[vertex][comp]);
            }
        }
        buffer = byteBuffer.flip();

        uniforms[0] = new Uniform();
        uniforms[0].buffer = DataUtils.ToBuffer(model.boneFinalTransformation);
        uniforms[0].location = info.boneUniformLocation;
        uniforms[0].size = 50;

        try {
            builder.setImageTexture(
                    new ImageTexture(
                            BitmapFactory.decodeStream(
                                    assetManager.open("textures/" + model.textures[0])
                            )
                    )
            );
        } catch (IOException e) {
            CrashManager.ReportCrash(CrashManager.CrashType.IO, "Not Found", e);
        }

        if (model.indices != null) {
            builder.setIndices(DataUtils.ToBuffer(model.indices), model.indices.length, 0);
        }

        material = new Material(renderPass, builder);

        createData(info);
    }

    private void createData(AnimatedShaderInfo info) {
        this.vbo = new int[1]; // combine vbo and vao indices?

        GLES31.glGenBuffers(vbo.length, vbo, 0);
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vbo[0]);
        GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER, buffer.capacity(), buffer, GL_STATIC_DRAW);

        createVAO(info);
    }

    // Move VAO to the shader as this is like the input layout (per shader)
    private void createVAO(AnimatedShaderInfo info) {
        this.vao = new int[1];

        int stride = buffer.capacity() / getVertexCount();

        GLES31.glGenVertexArrays(vao.length, vao, 0);
        GLES31.glBindVertexArray(vao[0]); // Another example of pretty weird java code, c++ <3

        GLES31.glEnableVertexAttribArray(info.vertexLoc); // uses currently bound vao
        GLES31.glVertexAttribPointer(info.vertexLoc, 3, GLES31.GL_FLOAT, false, stride, 0);

        GLES31.glEnableVertexAttribArray(info.normalLoc); // uses currently bound vao
        GLES31.glVertexAttribPointer(info.normalLoc, 3, GLES31.GL_FLOAT, false, stride, Float.BYTES * 3);

        GLES31.glEnableVertexAttribArray(info.uvLoc); // uses currently bound vao
        GLES31.glVertexAttribPointer(info.uvLoc, 2, GLES31.GL_FLOAT, false, stride, Float.BYTES * 6);

        GLES31.glEnableVertexAttribArray(info.boneIdLocation); // uses currently bound vao
        GLES31.glVertexAttribIPointer(info.boneIdLocation, 4, GLES31.GL_INT, stride, Float.BYTES * 8);

        GLES31.glEnableVertexAttribArray(info.boneWeightLocation); // uses currently bound vao
        GLES31.glVertexAttribPointer(info.boneWeightLocation, 4, GLES31.GL_FLOAT, false, stride, Float.BYTES * 8 + Integer.BYTES * 4);
    }

    public void setAnimation(String animation) {
        currentAnimation = model.getAnimationIndex(animation);
    }

    public void update(float dt) {
        time += dt;
        model.updateBoneTransformations(currentAnimation, time);
    }

    public Material getMaterial() {
        return material;
    }

    public int getVertexCount() {
        return model.vertices.length / VERTEX_FLOATS;
    }

    public void setLocation(int modelUniformLoc, FloatBuffer buffer) {
        uniforms[1] = new Uniform();
        uniforms[1].buffer = buffer;
        uniforms[1].location = modelUniformLoc;
        uniforms[1].size = 1;
    }

    public void updateLocation(FloatBuffer buffer) {
        uniforms[1].buffer = buffer;
    }

    public Uniform[] getUniforms() {
        return uniforms;
    }

    public static class AnimatedShaderInfo {
        public int vertexLoc, normalLoc, uvLoc;
        public int boneIdLocation, boneWeightLocation, boneUniformLocation;
    }

    public int[] getVbo() {
        return vbo;
    }

    public int[] getVao() {
        return vao;
    }

    public class Uniform {
        public int location, size;
        public FloatBuffer buffer;
    }
}

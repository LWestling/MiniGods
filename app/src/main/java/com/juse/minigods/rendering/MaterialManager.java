package com.juse.minigods.rendering;

import android.opengl.GLES31;
import android.util.SparseArray;

import com.juse.minigods.game.Game;
import com.juse.minigods.rendering.Material.ImageTexture;
import com.juse.minigods.rendering.Material.Indices;
import com.juse.minigods.rendering.Material.Material;
import com.juse.minigods.rendering.Material.Uniforms;
import com.juse.minigods.rendering.Material.Vertices;

import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_LESS;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES31.glUseProgram;

/**
 * Created by LukasW on 2018-03-09.
 * Manages materials
 */

public class MaterialManager {
    private SparseArray<ArrayList<Material>> renderPassBuckets;
    private ArrayList<RenderPass> renderPasses;

    public MaterialManager() {
        renderPassBuckets = new SparseArray<>();
        renderPasses = new ArrayList<>();

    }

    public int createRenderPass(int vertexShader, int fragmentShader, ShaderManager shaderManager) {
        int id = renderPasses.size();
        renderPasses.add(new RenderPass(vertexShader, fragmentShader, shaderManager));
        renderPassBuckets.put(id, new ArrayList<>());
        return id;
    }

    public void addMaterial(Material material) {
        renderPassBuckets.get(material.getRenderPass()).add(material);
    }

    public void render(int renderPass, int cameraLocation, int lightLocation, int cameraPosLocation) {
        RenderPass pass = renderPasses.get(renderPass);

        glUseProgram(pass.getProgram());
        glEnable(GLES31.GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);

        Game.GetCamera().bindGraphicsData(cameraLocation);

        Vector4f lightPos = Game.getUniversalLightPosition();
        Vector3f cameraPos = Game.CAMERA_START_POS;
        glUniform4f(lightLocation, lightPos.x(), lightPos.y(), lightPos.z(), lightPos.w());
        glUniform4f(cameraPosLocation, cameraPos.x(), cameraPos.y(), cameraPos.z(), 1.f);

        for (Material material : renderPassBuckets.get(renderPass)) {
            render(material);
        }
    }

    private void render(Material material) {
        // todo: change this to data oriented way.
        Uniforms uniforms = material.getUniforms();
        if (uniforms != null) {
            uniformMatrices(uniforms);
        }

        Vertices vertices = material.getVertices();
        Indices indices = material.getIndices();
        ImageTexture imageTexture = material.getImageTexture();

        // pretty awful code, opt
        if (vertices != null) {
            for (int vao : vertices.getVao()) { // always 1
                GLES31.glBindVertexArray(vao);

                if (imageTexture != null) {
                    glActiveTexture(GL_TEXTURE0);
                    glBindTexture(GL_TEXTURE_2D, imageTexture.getTextureId());
                }

                if (indices != null) {
                    GLES31.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indices.getGlBufferLocation());
                    GLES31.glDrawElements(GL_TRIANGLES, indices.getSize(), GL_UNSIGNED_INT, indices.getOffset());
                    GLES31.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
                } else {
                    GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, vertices.getVertexCount());
                }

                GLES31.glBindVertexArray(0);
            }
        }
    }

    private void uniformMatrices(Uniforms uniforms) {
        for (int i = 0; i < uniforms.getUniformLocations().length; i++) {
            GLES31.glUniformMatrix4fv(uniforms.getUniformLocations()[i], 1,
                    false, uniforms.getUniformBuffers()[i]);
        }
    }
}

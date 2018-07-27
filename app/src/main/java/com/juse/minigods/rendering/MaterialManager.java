package com.juse.minigods.rendering;

import android.opengl.GLES31;
import android.util.SparseArray;

import com.juse.minigods.rendering.Material.Indices;
import com.juse.minigods.rendering.Material.Material;
import com.juse.minigods.rendering.Material.Uniforms;
import com.juse.minigods.rendering.Material.Vertices;

import org.joml.Vector3f;

import java.util.ArrayList;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static android.opengl.GLES31.glUseProgram;

/**
 * Created by LukasW on 2018-03-09.
 * Manages materials
 */

public class MaterialManager {
    private SparseArray<ArrayList<Material>> renderPassBuckets;
    private ArrayList<RenderPass> renderPasses;
    private static CameraProjectionManager cameraProjectionManager; // THIS IS FOR TESTING; MOVE CAMERA TO LOGIC, OR JUST MAKE IT A CONSTANT

    public MaterialManager() {
        renderPassBuckets = new SparseArray<>();
        renderPasses = new ArrayList<>();

        cameraProjectionManager = new CameraProjectionManager();
        cameraProjectionManager.updateCamera(new Vector3f(0, 0.2f, 1.f),
                new Vector3f(0.f, -0.33f, -1.f).normalize());
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

    public void render(int renderPass) {
        RenderPass pass = renderPasses.get(renderPass);

        glUseProgram(pass.getProgram());
        for (Material material : renderPassBuckets.get(renderPass)) {
            render(material);
        }
    }

    private void render(Material material) {
        cameraProjectionManager.bindGraphicsData(1);

        // todo: change this to data oriented way.
        Uniforms uniforms = material.getUniforms();
        if (uniforms != null) {
            for (int i = 0; i < uniforms.getUniformLocations().length; i++) {
                GLES31.glUniformMatrix4fv(uniforms.getUniformLocations()[i], 1,
                        false, uniforms.getUniformBuffers()[i]);
            }
        }

        Vertices vertices = material.getVertices();
        Indices indices = material.getIndices();
        if (vertices != null) {
            for (int vao : vertices.getVao()) { // always 1
                GLES31.glBindVertexArray(vao);

                if (indices != null) {
                    GLES31.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indices.getGlBufferLocation());
                    GLES31.glDrawElements(GL_TRIANGLES, indices.getSize(), GL_UNSIGNED_INT, indices.getOffset());
                    GLES31.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
                } else {
                    GLES31.glDrawArrays(GLES31.GL_TRIANGLES, vertices.getVertexOffset(), vertices.getVertexCount());
                }

                GLES31.glBindVertexArray(0);
            }
        }
    }

    public static void updateCamera(Vector3f pos, Vector3f lookAlong) {
        cameraProjectionManager.updateCamera(pos, lookAlong);
    }
}

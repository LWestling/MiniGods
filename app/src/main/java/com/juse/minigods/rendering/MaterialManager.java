package com.juse.minigods.rendering;

import android.opengl.GLES31;
import android.util.SparseArray;

import java.util.ArrayList;

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
        renderPassBuckets.put(id, new ArrayList<Material>());
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
        for (int i = 0; i < material.getUniformLocations().length; i++) {
            GLES31.glUniformMatrix4fv(material.getUniformLocations()[i], 1,
                    false, material.getUniformBuffers()[i]);
        }

        for (int vao : material.getVAO()) { // always 1
            GLES31.glBindVertexArray(vao);
            GLES31.glDrawArrays(GLES31.GL_TRIANGLES, material.getVertexOffset(), material.getVertexCount());
            GLES31.glBindVertexArray(0);
        }
    }
}

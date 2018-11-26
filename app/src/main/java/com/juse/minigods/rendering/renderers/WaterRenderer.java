package com.juse.minigods.rendering.renderers;

import android.content.res.AssetManager;
import android.opengl.GLES31;

import com.juse.minigods.Utils.DataUtils;
import com.juse.minigods.map.WaterGrid;
import com.juse.minigods.rendering.Material.Material;
import com.juse.minigods.rendering.Material.MaterialBuilder;
import com.juse.minigods.rendering.MaterialManager;
import com.juse.minigods.rendering.ShaderManager;
import com.juse.minigods.reporting.CrashManager;

import java.io.IOException;
import java.util.concurrent.locks.Lock;

public class WaterRenderer implements RendererInterface {
    private static final String VS = "water/vertex", FS = "water/fragment";
    private int renderPass;
    private WaterGrid[] waterGrids;
    private Material[] waterGridMaterials;

    public WaterRenderer(WaterGrid[] waterGrids) {
        this.waterGrids = waterGrids;
    }

    @Override
    public void setup(ShaderManager shaderManager, MaterialManager materialManager, AssetManager assets) {
        int vs, fs;
        try {
            vs = loadShader(shaderManager, assets, GLES31.GL_VERTEX_SHADER, VS);
            fs = loadShader(shaderManager, assets, GLES31.GL_FRAGMENT_SHADER, FS);
        } catch (IOException e) {
            CrashManager.ReportCrash(CrashManager.CrashType.IO, "Couldn't load shader", e);
            return;
        }

        renderPass = materialManager.createRenderPass(vs, fs, shaderManager);

        int attrSize[] = {3, 3};
        int attrPos[] = {0, 1};
        int strides[] = {0, 0};

        waterGridMaterials = new Material[waterGrids.length];
        for (int i = 0; i < waterGrids.length; i++) {
            WaterGrid waterGrid = waterGrids[i];
            int offsets[] = {0, FLOAT_BYTES * waterGrid.getVertexData().capacity() / 2};

            MaterialBuilder builder = new MaterialBuilder();
            waterGrid.getVertexDataUpdateLock().lock();
            try {
                builder.setVertices(
                        waterGrid.getVertexData(),
                        waterGrid.getVertexData().capacity() / 2, GLES31.GL_DYNAMIC_DRAW,
                        attrSize, attrPos, strides, offsets);
            } finally {
                waterGrid.getVertexDataUpdateLock().unlock();
            }
            /*
            builder.setIndices(
                    DataUtils.ToBuffer(waterGrid.getIndices().toArray(new Integer[] {})),
                    waterGrid.getIndices().size(), 0);
                    */
            builder.setUniforms(new int[] {2}, DataUtils.ToBuffer(waterGrid.getWorld()));

            waterGridMaterials[i] = new Material(renderPass, builder);
            materialManager.addMaterial(waterGridMaterials[i]);
        }
    }

    @Override
    public void render(ShaderManager shaderManager, MaterialManager materialManager) {
        materialManager.render(renderPass, 3, 4, 5);
    }

    @Override
    public void update(MaterialManager materialManager) {
        for (int i = 0; i < waterGrids.length; i++) {
            Material material = waterGridMaterials[i];
            WaterGrid waterGrid = waterGrids[i];
            Lock lock = waterGrid.getVertexDataUpdateLock();

            if(lock.tryLock()) {
                try {
                    material.getVertices().updateData(waterGrid.getVertexData());
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}

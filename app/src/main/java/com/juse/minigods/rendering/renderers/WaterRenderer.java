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

import org.joml.Vector3f;

import java.io.IOException;

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

        int attrSize[] = {3};
        int attrPos[] = {0};
        int strides[] = {0};
        int offsets[] = {0};

        waterGridMaterials = new Material[waterGrids.length];
        for (int i = 0; i < waterGrids.length; i++) {
            WaterGrid waterGrid = waterGrids[i];

            MaterialBuilder builder = new MaterialBuilder();
            builder.setVertices(
                    DataUtils.ToBuffer(waterGrid.getPositions().toArray(new Vector3f[]{})),
                    waterGrid.getPositions().size(), GLES31.GL_DYNAMIC_DRAW,
                    attrSize, attrPos, strides, offsets);
            builder.setIndices(
                    DataUtils.ToBuffer(waterGrid.getIndices().toArray(new Integer[] {})),
                    waterGrid.getIndices().size(), 0);
            builder.setUniforms(new int[] {1}, DataUtils.ToBuffer(waterGrid.getWorld()));

            waterGridMaterials[i] = new Material(renderPass, builder);
            materialManager.addMaterial(waterGridMaterials[i]);
        }
    }

    @Override
    public void render(ShaderManager shaderManager, MaterialManager materialManager) {
        materialManager.render(renderPass, 2, 3, 4);
    }

    @Override
    public void update(MaterialManager materialManager) {

    }
}
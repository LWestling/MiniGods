package com.juse.minigods.rendering.renderers;

import android.content.res.AssetManager;

import com.juse.minigods.Utils.DataUtils;
import com.juse.minigods.map.Terrain;
import com.juse.minigods.map.TerrainColumn;
import com.juse.minigods.rendering.Material.Material;
import com.juse.minigods.rendering.MaterialBuilder;
import com.juse.minigods.rendering.MaterialManager;
import com.juse.minigods.rendering.ShaderManager;
import com.juse.minigods.reporting.CrashManager;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_VERTEX_SHADER;

/**
 * Created by LukasW on 2018-03-22.
 * Renders the terrain but does not change any data
 */

public class TerrainRenderer implements RendererInterface {
    private final static String VS = "terrain/vertex", FS = "terrain/fragment";

    private Terrain terrain;
    private int renderPass;

    public TerrainRenderer(Terrain terrain) {
        this.terrain = terrain;
    }

    public void setup(ShaderManager shaderManager, MaterialManager materialManager, AssetManager assetManager) {
        int vs, fs;
        try {
            vs = shaderManager.createShader(GL_VERTEX_SHADER,
                    assetManager.open(String.format(ShaderManager.SHADER_PATH_FORMAT, VS)));
            fs = shaderManager.createShader(GL_FRAGMENT_SHADER,
                    assetManager.open(String.format(ShaderManager.SHADER_PATH_FORMAT, FS)));
        } catch (IOException e) {
            e.printStackTrace();
            CrashManager.ReportCrash(CrashManager.CrashType.GRAPHICS, "Error loading: " +
                    "vs / fs in terrain renderer", e);
            return;
        }

        renderPass = materialManager.createRenderPass(vs, fs, shaderManager);

        TerrainColumn column = terrain.getRenderColumn();
        Vector3f[] vertices = column.getColumnVertices().toArray(new Vector3f[] {});
        Integer[] integers = column.getColumnIndices().toArray(new Integer[] {});

        MaterialBuilder materialBuilder = new MaterialBuilder();
        materialBuilder.addVertices(DataUtils.ToBuffer(vertices), 0, vertices.length, 0);
        materialBuilder.addIndices(DataUtils.ToBuffer(integers), integers.length, 0);
        for (int i = 0; i < terrain.getColumnsSize(); i++) {
            // translation uniform
            materialBuilder.addUniforms(
                    (new FloatBuffer[] {DataUtils.ToBuffer(new Matrix4f().translate(terrain.getColumnWidth() * i, 0.f, 0.f).scale(terrain.getColumnWidth(), 1.f, 1.f))}),
                    (new int[] {2})
            );

            materialManager.addMaterial(new Material(materialBuilder));
        }
    }

    public void render(ShaderManager shaderManager, MaterialManager materialManager) {
        materialManager.render(renderPass);
    }
}

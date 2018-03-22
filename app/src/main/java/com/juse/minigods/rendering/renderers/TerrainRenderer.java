package com.juse.minigods.rendering.renderers;

import android.content.res.AssetManager;

import com.juse.minigods.Utils.DataUtils;
import com.juse.minigods.map.Terrain;
import com.juse.minigods.rendering.Material;
import com.juse.minigods.rendering.MaterialManager;
import com.juse.minigods.rendering.ShaderManager;
import com.juse.minigods.reporting.CrashManager;

import org.joml.Vector3f;

import java.io.IOException;

import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_VERTEX_SHADER;

/**
 * Created by LukasW on 2018-03-22.
 * Renders the terrain but does not change any data
 */

public class TerrainRenderer implements RendererInterface {
    private Terrain terrain;
    public final static float WIDTH = 2.5f, HEIGHT = 2.5f;
    public final static String VS = "terrain/vertex", FS = "terrain/fragment";

    // replace with better later for some displacement
    public final static Vector3f SQUARE[] = {
            new Vector3f(-1.f, 1.f, 0.f),
            new Vector3f(1.f, -1.f, 0.f),
            new Vector3f(-1.f, -1.f, 0.f),

            new Vector3f(-1.f, 1.f, 0.f),
            new Vector3f(1.f, 1.f, 0.f),
            new Vector3f(1.f, -1.f, 0.f),
    };

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
            CrashManager.ReportCrash(CrashManager.CrashType.GRAPHICS, "Error loading" +
                    " vs / fs in terrain renderer", e);
            return;
        }

        renderPass = materialManager.createRenderPass(vs, fs, shaderManager);
        Material terrainMaterial = new Material(renderPass, DataUtils.ToBuffer(SQUARE), 0);
        materialManager.addMaterial(terrainMaterial);
    }

    public void render(ShaderManager shaderManager, MaterialManager materialManager) {
        materialManager.render(renderPass);
    }
}

package com.juse.minigods.rendering.renderers;

import android.content.res.AssetManager;

import com.juse.minigods.Utils.DataUtils;
import com.juse.minigods.Utils.ObjLoader;
import com.juse.minigods.game.Obstacle;
import com.juse.minigods.rendering.Material.Material;
import com.juse.minigods.rendering.Material.MaterialBuilder;
import com.juse.minigods.rendering.MaterialManager;
import com.juse.minigods.rendering.ShaderManager;
import com.juse.minigods.reporting.CrashManager;

import org.joml.Matrix4f;

import java.io.IOException;
import java.util.ArrayList;

import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_VERTEX_SHADER;

/**
 * Created by LukasW on 2018-03-22.
 * Renders obstacles, but does not change any data
 */

public class ObstacleRenderer implements RendererInterface {
    private final static String VS = "vertex", FS = "fragment";
    private ArrayList<Obstacle> obstacles;
    private int renderPass;

    public ObstacleRenderer(ArrayList<Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    public void setup(ShaderManager shaderManager, MaterialManager materialManager, AssetManager assetManager) {
        int vs, fs;
        try {
            vs = loadShader(shaderManager, assetManager, GL_VERTEX_SHADER, VS);
            fs = loadShader(shaderManager, assetManager, GL_FRAGMENT_SHADER, FS);
        } catch (IOException e) {
            CrashManager.ReportCrash(CrashManager.CrashType.GRAPHICS, "Error loading: " +
                    "vs / fs in terrain renderer", e);
            return;
        }

        renderPass = materialManager.createRenderPass(vs, fs, shaderManager);

        ObjLoader objLoader = new ObjLoader("tree/lowpolytree", ObjLoader.ModelType.Quads);
        MaterialBuilder builder = objLoader.load(assetManager);
        builder.addUniforms(new int[] {3}, DataUtils.ToBuffer(new Matrix4f().translate(0.f, 0.f, 3.f).scale(5)));
        materialManager.addMaterial(new Material(renderPass, builder));
    }

    public void render(ShaderManager shaderManager, MaterialManager materialManager) {
        materialManager.render(renderPass, 3);
    }

    public void update(MaterialManager materialManager) {

    }
}

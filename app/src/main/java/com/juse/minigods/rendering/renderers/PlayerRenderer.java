package com.juse.minigods.rendering.renderers;

import android.content.res.AssetManager;
import android.graphics.BitmapFactory;

import com.juse.minigods.Utils.DataUtils;
import com.juse.minigods.game.Player;
import com.juse.minigods.rendering.Material.ImageTexture;
import com.juse.minigods.rendering.Material.Material;
import com.juse.minigods.rendering.Material.MaterialBuilder;
import com.juse.minigods.rendering.MaterialManager;
import com.juse.minigods.rendering.ShaderManager;
import com.juse.minigods.reporting.CrashManager;

import org.joml.Matrix4f;

import java.io.IOException;

import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_VERTEX_SHADER;

/**
 * Created by LukasW on 2018-03-22.
 * Renders the player but does not change any data
 */

public class PlayerRenderer implements RendererInterface {
    private final static String VS = "player/vertex", FS = "player/fragment";

    // before player model is done
    private static final float[] model = {
           0.f, 1.f, 0.f,   0.f, 0.f, 1.f,  0.f, 0.f,
           1.f, 1.f, 0.f,   0.f, 0.f, 1.f,  1.f, 0.f,
           0.f, 0.f, 0.f,   0.f, 0.f, 1.f,  0.f, 1.f,

           1.f, 1.f, 0.f,   0.f, 0.f, 1.f,  1.f, 0.f,
           1.f, 0.f, 0.f,   0.f, 0.f, 1.f,  1.f, 1.f,
           0.f, 0.f, 0.f,   0.f, 0.f, 1.f,  0.f, 1.f,
    };

    private Player player;
    private Material playerMaterial;
    private int renderPass;

    public PlayerRenderer(Player player) {
        this.player = player;
    }

    public void setup(ShaderManager shaderManager, MaterialManager materialManager, AssetManager assets) {
        int vs, fs;
        try {
            vs = loadShader(shaderManager, assets, GL_VERTEX_SHADER, VS);
            fs = loadShader(shaderManager, assets, GL_FRAGMENT_SHADER, FS);
        } catch (IOException e) {
            CrashManager.ReportCrash(CrashManager.CrashType.GRAPHICS, "Error loading: " +
                    "vs / fs in terrain renderer", e);
            return;
        }

        renderPass = materialManager.createRenderPass(vs, fs, shaderManager);

        MaterialBuilder builder = new MaterialBuilder();
        builder.addVertices(DataUtils.ToBuffer(model), 6, GL_STATIC_DRAW,
                new int[] {3, 3, 2}, new int[] {0, 1, 2},
                new int[] {Float.BYTES * 8, Float.BYTES * 8, Float.BYTES * 8},
                new int[] {0, Float.BYTES * 3, Float.BYTES * 6});
        builder.addUniforms(new int[] {4}, DataUtils.ToBuffer(new Matrix4f().translate(0.f, 0.f, 3.f)));

        try {
            builder.addImageTexture(new ImageTexture(BitmapFactory.decodeStream(assets.open("textures/player.png"))));
        } catch (IOException e) {
            CrashManager.ReportCrash(CrashManager.CrashType.IO, "Not Found", e);
        }

        playerMaterial = new Material(renderPass, builder);
        materialManager.addMaterial(playerMaterial);
    }

    public void render(ShaderManager shaderManager, MaterialManager materialManager) {
        materialManager.render(renderPass, 3, 5, 6);
    }

    public void update(MaterialManager materialManager) {
        playerMaterial.getUniforms().updateUniform(DataUtils.ToBuffer(new Matrix4f().translate(player.getPosition())), 0);
    }
}

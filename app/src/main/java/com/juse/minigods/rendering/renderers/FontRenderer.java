package com.juse.minigods.rendering.renderers;

import android.content.res.AssetManager;

import com.juse.minigods.Utils.DataUtils;
import com.juse.minigods.rendering.Font.Font;
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

public class FontRenderer implements RendererInterface{
    private final static String VS = "font/vertex", FS = "font/fragment";

    private Font font;
    private int renderPass;

    // before player model is done
    private static final float[] model = {
            0.f, 1.f, 0.f,   0.f, 0.f,
            1.f, 1.f, 0.f,   1.f, 0.f,
            0.f, 0.f, 0.f,   0.f, 1.f,

            1.f, 1.f, 0.f,   1.f, 0.f,
            1.f, 0.f, 0.f,   1.f, 1.f,
            0.f, 0.f, 0.f,   0.f, 1.f,
    };

    public FontRenderer(Font font) {
        this.font = font;
    }

    @Override
    public void setup(ShaderManager shaderManager, MaterialManager materialManager, AssetManager assets) {
        font.createTexture();

        int vs, fs;
        try {
            vs = loadShader(shaderManager, assets, GL_VERTEX_SHADER, VS);
            fs = loadShader(shaderManager, assets, GL_FRAGMENT_SHADER, FS);
        } catch (IOException e) {
            CrashManager.ReportCrash(CrashManager.CrashType.GRAPHICS, "Error loading: " +
                    "vs / fs in font renderer", e);
            return;
        }

        renderPass = materialManager.createRenderPass(vs, fs, shaderManager);

        MaterialBuilder materialBuilder = new MaterialBuilder();
        materialBuilder.setUniforms(new int[] {2}, DataUtils.ToBuffer(new Matrix4f().scale(0.4f)));
        materialBuilder.setVertices(DataUtils.ToBuffer(model), 6, GL_STATIC_DRAW,
                new int[] {3, 2}, new int[] {0, 1}, new int[] {Float.BYTES * 5, Float.BYTES * 5}, new int[] {0, Float.BYTES * 3});

        /* THis works, Some fucking how
        try {
            materialBuilder.setImageTexture(new ImageTexture(BitmapFactory.decodeStream(assets.open("textures/font.png"))));
        } catch (IOException e) {
            CrashManager.ReportCrash(CrashManager.CrashType.IO, "Not Found", e);
        } */
        materialBuilder.setImageTexture(font.getImageTexture());

        materialManager.addMaterial(new Material(renderPass, materialBuilder));
    }

    @Override
    public void render(ShaderManager shaderManager, MaterialManager materialManager) {
        materialManager.render(renderPass);
    }

    @Override
    public void update(MaterialManager materialManager) {

    }
}

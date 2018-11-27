package com.juse.minigods.rendering.renderers;

import android.content.res.AssetManager;
import android.graphics.BitmapFactory;

import com.juse.minigods.Utils.DataUtils;
import com.juse.minigods.map.Terrain;
import com.juse.minigods.map.TerrainColumn;
import com.juse.minigods.rendering.Material.ImageTexture;
import com.juse.minigods.rendering.Material.Material;
import com.juse.minigods.rendering.Material.MaterialBuilder;
import com.juse.minigods.rendering.MaterialManager;
import com.juse.minigods.rendering.ShaderManager;
import com.juse.minigods.reporting.CrashManager;

import org.joml.Matrix4f;

import java.io.IOException;
import java.util.concurrent.locks.Lock;

import static android.opengl.GLES20.GL_DYNAMIC_DRAW;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_VERTEX_SHADER;

/**
 * Created by LukasW on 2018-03-22.
 * Renders the terrain but does not change any data
 */

public class TerrainRenderer implements RendererInterface {
    private final static int BASE_LOCATION = 3;
    private final static String VS = "terrain/vertex", FS = "terrain/fragment";
    private final static String TEXTURE_PATH = "textures/%s.png", GRASS = "grass", SAND = "sand";

    private Terrain terrain;
    private Material columns[];
    private int renderPass;

    public TerrainRenderer(Terrain terrain) {
        this.terrain = terrain;
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

        TerrainColumn[] renderColumns = terrain.getRenderColumns();
        columns = new Material[terrain.getColumnsSize()];

        ImageTexture grass;
        try {
            grass = new ImageTexture(BitmapFactory.decodeStream(assetManager.open(String.format(TEXTURE_PATH, GRASS))));
        } catch (IOException e) {
            CrashManager.ReportCrash(CrashManager.CrashType.IO, "Not Found", e);
            return;
        }

        for (int i = 0; i < columns.length; i++) {
            TerrainColumn renderColumn = renderColumns[i];

            // build vertices
            MaterialBuilder materialBuilder = new MaterialBuilder();
            materialBuilder.setVertices(renderColumn.getVertexData(), renderColumn.getColumnVertexData().size(),
                    GL_DYNAMIC_DRAW, new int[] {3, 3, 2}, new int[] {0, 1, 2},
                    new int[] {FLOAT_BYTES * 8, FLOAT_BYTES * 8, FLOAT_BYTES * 8}, new int[] {0, FLOAT_BYTES * 3, FLOAT_BYTES * 6});
            materialBuilder.setImageTexture(grass);

            // translation uniform
            materialBuilder.setUniforms(
                    (new int[]{BASE_LOCATION}),
                    DataUtils.ToBuffer(new Matrix4f()
                            .translate(renderColumn.getOffset(), 0.f, 0.f)
                            .scale(terrain.getColumnWidth() + 0.005f, 1.f, 1.f))
            );

            columns[i] = new Material(renderPass, materialBuilder);
            materialManager.addMaterial(columns[i]);
        }
    }

    public void render(ShaderManager shaderManager, MaterialManager materialManager) {
        materialManager.render(renderPass, BASE_LOCATION + 1, BASE_LOCATION + 2, BASE_LOCATION + 3);
    }

    public void update(MaterialManager materialManager) {
        Lock terrainUpdateLock = terrain.getTerrainUpdateLock();

        terrainUpdateLock.lock();
        try {
            TerrainColumn[] renderColumns = terrain.getRenderColumns();
            for (int i = 0; i < columns.length; i++) {
                columns[i].getUniforms().updateUniform(
                        DataUtils.ToBuffer(new Matrix4f()
                                .translate(renderColumns[i].getOffset(), 0.f, 0.f)
                                .scale(terrain.getColumnWidth(), 1.f, 1.f)),
                        0
                );
                columns[i].getVertices().updateData(renderColumns[i].getVertexData());
            }
        } finally {
            terrainUpdateLock.unlock();
        }
    }
}

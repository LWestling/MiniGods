package com.juse.minigods.rendering.renderers;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

import com.juse.minigods.Utils.DataUtils;
import com.juse.minigods.map.Terrain;
import com.juse.minigods.map.TerrainColumn;
import com.juse.minigods.rendering.Material.ImageTexture;
import com.juse.minigods.rendering.Material.Material;
import com.juse.minigods.rendering.Material.MaterialBuilder;
import com.juse.minigods.rendering.Material.Vertices;
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
    private final static String VS = "terrain/vertex", FS = "terrain/fragment";

    private Terrain terrain;
    private Material columns[];
    private int renderPass;
    private Bitmap tileTexture;

    public TerrainRenderer(Terrain terrain, Bitmap bitmap) {
        this.terrain = terrain;
        this.tileTexture = bitmap;
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

        for (int i = 0; i < columns.length; i++) {
            TerrainColumn renderColumn = renderColumns[i];
            Integer[] integers = renderColumn.getColumnIndices().toArray(new Integer[]{});

            // build vertices
            MaterialBuilder materialBuilder = new MaterialBuilder();
            materialBuilder.addVertices(renderColumn.getVertexData(), renderColumn.getColumnVertexData().size(),
                    GL_DYNAMIC_DRAW, new int[] {3, 2}, new int[] {0, 1},
                    new int[] {Float.BYTES * 5, Float.BYTES * 5}, new int[] {0, Float.BYTES * 3});
            materialBuilder.addIndices(DataUtils.ToBuffer(integers), integers.length, 0);
            materialBuilder.addImageTexture(new ImageTexture(tileTexture));

            // translation uniform
            materialBuilder.addUniforms(
                    (new int[]{3}),
                    DataUtils.ToBuffer(new Matrix4f()
                            .translate(renderColumn.getOffset(), 0.f, 0.f)
                            .scale(terrain.getColumnWidth() + 0.005f, 1.f, 1.f))
            );

            columns[i] = new Material(renderPass, materialBuilder);
            materialManager.addMaterial(columns[i]);
        }
    }

    public void render(ShaderManager shaderManager, MaterialManager materialManager) {
        materialManager.render(renderPass, 2);
    }

    public void update(MaterialManager materialManager) {
        Lock terrainUpdateLock = terrain.getTerrainUpdateLock();
        if(terrainUpdateLock.tryLock()) {
            try {
                TerrainColumn[] renderColumns = terrain.getRenderColumns();
                for (int i = 0; i < columns.length; i++) {
                    Vertices vertices = columns[i].getVertices();

                    //vertices.updateData(renderColumns[i].getVertexData());
                    columns[i].getUniforms().updateUniform(
                            DataUtils.ToBuffer(new Matrix4f()
                                    .translate(renderColumns[i].getOffset(), 0.f, 0.f)
                                    .scale(terrain.getColumnWidth(), 1.f, 1.f)),
                            0
                    );
                }
            } finally {
                terrainUpdateLock.unlock();
            }
        }
    }
}

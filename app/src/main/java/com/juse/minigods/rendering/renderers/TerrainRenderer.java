package com.juse.minigods.rendering.renderers;

import android.content.res.AssetManager;

import com.juse.minigods.Utils.DataUtils;
import com.juse.minigods.map.Terrain;
import com.juse.minigods.map.TerrainColumn;
import com.juse.minigods.rendering.Material.Material;
import com.juse.minigods.rendering.Material.MaterialBuilder;
import com.juse.minigods.rendering.Material.Vertices;
import com.juse.minigods.rendering.MaterialManager;
import com.juse.minigods.rendering.ShaderManager;
import com.juse.minigods.reporting.CrashManager;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;

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

        TerrainColumn[] renderColumns = terrain.getRenderColumns();
        columns = new Material[terrain.getColumnsSize()];

        for (int i = 0; i < columns.length; i++) {
            TerrainColumn renderColumn = renderColumns[i];
            Vector3f[] vertices = renderColumn.getColumnVertices().toArray(new Vector3f[]{});
            Integer[] integers = renderColumn.getColumnIndices().toArray(new Integer[]{});

            // build vertices
            MaterialBuilder materialBuilder = new MaterialBuilder();
            materialBuilder.addVertices(DataUtils.ToBuffer(vertices), 0,
                    vertices.length, 0, GL_DYNAMIC_DRAW);
            materialBuilder.addIndices(DataUtils.ToBuffer(integers), integers.length, 0);

            // translation uniform
            materialBuilder.addUniforms(
                    (new int[]{2}),
                    DataUtils.ToBuffer(new Matrix4f()
                            .translate(renderColumn.getOffset(), 0.f, 0.f)
                            .scale(terrain.getColumnWidth(), 1.f, 1.f))
            );

            columns[i] = new Material(materialBuilder);
            materialManager.addMaterial(columns[i]);
        }
    }

    public void render(ShaderManager shaderManager, MaterialManager materialManager) {
        materialManager.render(renderPass);
    }

    public void update(MaterialManager materialManager) {
        TerrainColumn[] renderColumns = terrain.getRenderColumns();
        for (int i = 0; i < columns.length; i++) {
            Vertices vertices = columns[i].getVertices();
            Vector3f[] vertexData = renderColumns[i].getColumnVertices().toArray(new Vector3f[] {});

            vertices.updateData(DataUtils.ToBuffer(vertexData));
            columns[i].getUniforms().updateUniform(
                    DataUtils.ToBuffer(new Matrix4f()
                            .translate(renderColumns[i].getOffset(), 0.f, 0.f)
                            .scale(terrain.getColumnWidth(), 1.f, 1.f)),
            0
            );
        }
    }
}

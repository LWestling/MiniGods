package com.juse.minigods.rendering.renderers;

import android.content.res.AssetManager;

import com.juse.minigods.rendering.MaterialManager;
import com.juse.minigods.rendering.ShaderManager;

import java.io.IOException;

/**
 * Created by LukasW on 2018-03-22.
 * Renders any component in the game, ex: Terrain or player
 */

public interface RendererInterface {
    int FLOAT_BYTES = 4;

    void setup(ShaderManager shaderManager, MaterialManager materialManager, AssetManager assets);
    void render(ShaderManager shaderManager, MaterialManager materialManager);
    void update(MaterialManager materialManager);

    // help functions, commonly used (always used?)
    default int loadShader(ShaderManager shaderManager, AssetManager assetManager, int shaderType, String shaderPath) throws IOException {
        return shaderManager.createShader(shaderType,
                assetManager.open(String.format(ShaderManager.SHADER_PATH_FORMAT, shaderPath)));
    }
}

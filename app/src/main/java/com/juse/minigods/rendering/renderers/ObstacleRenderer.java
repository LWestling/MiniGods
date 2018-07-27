package com.juse.minigods.rendering.renderers;

import android.content.res.AssetManager;

import com.juse.minigods.game.Obstacle;
import com.juse.minigods.rendering.MaterialManager;
import com.juse.minigods.rendering.ShaderManager;

import java.util.ArrayList;

/**
 * Created by LukasW on 2018-03-22.
 * Renders obstacles, but does not change any data
 */

public class ObstacleRenderer implements RendererInterface {
    private ArrayList<Obstacle> obstacles;

    public ObstacleRenderer(ArrayList<Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    public void setup(ShaderManager shaderManager, MaterialManager materialManager, AssetManager assets) {

    }

    public void render(ShaderManager shaderManager, MaterialManager materialManager) {

    }

    public void update(MaterialManager materialManager) {

    }
}

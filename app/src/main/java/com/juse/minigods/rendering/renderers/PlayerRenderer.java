package com.juse.minigods.rendering.renderers;

import android.content.res.AssetManager;

import com.juse.minigods.game.Player;
import com.juse.minigods.rendering.MaterialManager;
import com.juse.minigods.rendering.ShaderManager;

/**
 * Created by LukasW on 2018-03-22.
 * Renders the player but does not change any data
 */

public class PlayerRenderer implements RendererInterface {
    private Player player;

    public PlayerRenderer(Player player) {
        this.player = player;
    }

    public void setup(ShaderManager shaderManager, MaterialManager materialManager, AssetManager assets) {
    }

    public void render(ShaderManager shaderManager, MaterialManager materialManager) {

    }

    public void update(MaterialManager materialManager) {

    }
}

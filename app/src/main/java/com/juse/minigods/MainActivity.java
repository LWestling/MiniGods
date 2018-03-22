package com.juse.minigods;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.juse.minigods.game.Game;
import com.juse.minigods.rendering.GameRenderer;
import com.juse.minigods.rendering.renderers.ObstacleRenderer;
import com.juse.minigods.rendering.renderers.PlayerRenderer;
import com.juse.minigods.rendering.renderers.RendererInterface;
import com.juse.minigods.rendering.renderers.TerrainRenderer;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private GLSurfaceView glSurfaceView;
    private Game game;
    private GameRenderer gameRenderer;
    private boolean running;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupGame();
        setupGameRenderer();

        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLConfigChooser(true);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setRenderer(gameRenderer);

        setContentView(glSurfaceView);

        running = true;
        new Thread(() -> {
            game.startGameSession();
            while (running) {
                game.update();
            }
        });
    }

    private void setupGame() {
        game = new Game();
    }

    private void setupGameRenderer() {
        ArrayList<RendererInterface> renderers = new ArrayList<>();
        renderers.add(new PlayerRenderer(game.getPlayer()));
        renderers.add(new ObstacleRenderer(game.getObstacles()));
        renderers.add(new TerrainRenderer(game.getTerrain()));

        gameRenderer = new GameRenderer(getAssets());
        gameRenderer.setupRenderers(renderers);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
}

package com.juse.minigods;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

import com.juse.minigods.game.Game;
import com.juse.minigods.game.Highscore;
import com.juse.minigods.map.Map;
import com.juse.minigods.rendering.Font.Font;
import com.juse.minigods.rendering.Font.TextCache;
import com.juse.minigods.rendering.GameRenderer;
import com.juse.minigods.rendering.renderers.FontRenderer;
import com.juse.minigods.rendering.renderers.ObstacleRenderer;
import com.juse.minigods.rendering.renderers.PlayerRenderer;
import com.juse.minigods.rendering.renderers.RendererInterface;
import com.juse.minigods.rendering.renderers.TerrainRenderer;
import com.juse.minigods.rendering.renderers.WaterRenderer;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private GLSurfaceView glSurfaceView;
    private Game game;
    private GameRenderer gameRenderer;
    private FontRenderer fontRenderer;
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

        Highscore highscore = new Highscore();
        running = true;
        new Thread(() -> {
            TextCache cache = fontRenderer.getTextCache();
            game.startGameSession(cache);
            game.setHighscore(highscore.getHighscore(getApplicationContext()));

            while (running) {
                game.update(cache);
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            highscore.overwriteHighscore(getApplicationContext(), game.getHighscore());
        }).start();
    }

    private void setupGame() {
        game = new Game();
    }

    private void setupGameRenderer() {
        Map map = game.getMap();

        ArrayList<RendererInterface> rendererList = new ArrayList<>();
        rendererList.add(new ObstacleRenderer(game.getObstacles()));
        rendererList.add(new TerrainRenderer(map.getTerrain()));
        rendererList.add(new WaterRenderer(map.getWaterGrids()));
        rendererList.add(new PlayerRenderer(game.getPlayer()));

        fontRenderer = new FontRenderer(new Font("font", "fontData2", getAssets()));
        rendererList.add(fontRenderer);

        gameRenderer = new GameRenderer(getAssets());
        gameRenderer.setupRendererList(rendererList);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                game.pressDown(x, y);
                // lastPos = new Vector2f(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                /*
                lookAlong.add(new Vector2f(x, y).sub(lastPos).mul(0.004f));

                Matrix3f lookRotation = new Matrix3f().rotationXYZ(-lookAlong.y(), -lookAlong.x(), 0.f);
                game.updateCamera(Game.CAMERA_START_POS, new Vector3f(lookDirection).mul(lookRotation));

                lastPos = new Vector2f(x, y); */
                break;
            case MotionEvent.ACTION_UP:
                game.pressUp(x, y);
                // MaterialManager.updateCamera(new Vector3f(0.f, 0.1f, 3.f), new Vector3f(0.f, -0.33f, -1.f).normalize());
                break;
        }

        return true;
    }

}

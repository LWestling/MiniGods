package com.juse.minigods;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

import com.juse.minigods.game.Game;
import com.juse.minigods.rendering.GameRenderer;
import com.juse.minigods.rendering.renderers.ObstacleRenderer;
import com.juse.minigods.rendering.renderers.PlayerRenderer;
import com.juse.minigods.rendering.renderers.RendererInterface;
import com.juse.minigods.rendering.renderers.TerrainRenderer;
import com.juse.minigods.reporting.CrashManager;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private GLSurfaceView glSurfaceView;
    private Game game;
    private GameRenderer gameRenderer;
    private boolean running;

    private Vector2f lastPos, lookAlong;
    private Vector3f lookDirection;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lookAlong = new Vector2f(0.f, 0.f);
        lookDirection = new Vector3f(0.f, -0.1f, -1.f);

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
                try {
                    Thread.sleep(25);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setupGame() {
        game = new Game();
    }

    private void setupGameRenderer() {
        ArrayList<RendererInterface> rendererList = new ArrayList<>();
        rendererList.add(new PlayerRenderer(game.getPlayer()));
        rendererList.add(new ObstacleRenderer(game.getObstacles()));
        try {
            rendererList.add(new TerrainRenderer(game.getTerrain(), BitmapFactory.decodeStream(getAssets().open("textures/grass.png"))));
        } catch (Exception e) {
            CrashManager.ReportCrash(CrashManager.CrashType.IO, "Not Found", e);
        }

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

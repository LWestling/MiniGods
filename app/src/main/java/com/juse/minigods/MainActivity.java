package com.juse.minigods;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

import com.juse.minigods.game.Game;
import com.juse.minigods.rendering.GameRenderer;
import com.juse.minigods.rendering.MaterialManager;
import com.juse.minigods.rendering.renderers.ObstacleRenderer;
import com.juse.minigods.rendering.renderers.PlayerRenderer;
import com.juse.minigods.rendering.renderers.RendererInterface;
import com.juse.minigods.rendering.renderers.TerrainRenderer;

import org.joml.Matrix3f;
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
    private float testY;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lookAlong = new Vector2f(0.f, 0.f);
        lookDirection = new Vector3f(0.f, -0.1f, -1.f);
        testY = 0.1f;

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
        ArrayList<RendererInterface> rendererList = new ArrayList<>();
        rendererList.add(new PlayerRenderer(game.getPlayer()));
        rendererList.add(new ObstacleRenderer(game.getObstacles()));
        rendererList.add(new TerrainRenderer(game.getTerrain()));

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
                lastPos = new Vector2f(x, y);
                testY += 0.1f;
                break;
            case MotionEvent.ACTION_MOVE:
                lookAlong.add(new Vector2f(x, y).sub(lastPos).mul(0.004f));

                Matrix3f lookRotation = new Matrix3f().rotationXYZ(-lookAlong.y(), -lookAlong.x(), 0.f);
                MaterialManager.updateCamera(new Vector3f(0.f, testY, 1.f), new Vector3f(lookDirection).mul(lookRotation));

                lastPos = new Vector2f(x, y);
                break;
            case MotionEvent.ACTION_UP:
                // MaterialManager.updateCamera(new Vector3f(0.f, 0.1f, 3.f), new Vector3f(0.f, -0.33f, -1.f).normalize());
                break;
        }

        return true;
    }

}

package com.juse.minigods;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import com.juse.minigods.Legality.Logger;
import com.juse.minigods.Utils.AudioManager;
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

public class GameActivity extends Activity {
    private Game game;
    private Highscore highscore;
    private GameRenderer gameRenderer;
    private FontRenderer fontRenderer;
    private boolean running;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupGame();
        setupGameRenderer();

        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLConfigChooser(true);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setRenderer(gameRenderer);

        setContentView(glSurfaceView);

        highscore = new Highscore();
        running = true;
        new Thread(() -> {
            TextCache cache = fontRenderer.getTextCache();
            game.setHighscore(highscore.getHighscore(getApplicationContext()));
            game.startGameSession(cache);

            while (running) {
                game.update(cache);
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        highscore.overwriteHighscore(getApplicationContext(), game.getHighscore());
    }

    @Override
    protected void onStop() {
        super.onStop();

        game.stop();
    }

    private void setupGame() {
        AudioManager audioManager = new AudioManager(this);
        audioManager.playMusic(this, AudioManager.Music.MAIN_MUSIC, true);
        game = new Game(audioManager, (score, isHighscore) -> {
            Bundle bundle = new Bundle();
            bundle.putInt("score", score);
            Logger.AnalyticsLog(this, "new_score", bundle);

            if (isHighscore) {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                if (account != null) {
                    Games.getLeaderboardsClient(this, account).submitScore(getString(R.string.leaderboard_id), score);
                }
            }
        });
    }

    private void setupGameRenderer() {
        Map map = game.getMap();

        ArrayList<RendererInterface> rendererList = new ArrayList<>();
        rendererList.add(new ObstacleRenderer(game.getObstacles()));
        rendererList.add(new TerrainRenderer(map.getTerrain()));
        rendererList.add(new WaterRenderer(map.getWaterGrids()));
        rendererList.add(new PlayerRenderer(this, game.getPlayer()));

        fontRenderer = new FontRenderer(new Font("bitFont", "bitFont", getAssets()));
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
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                game.pressUp(x, y);
                break;
        }

        return true;
    }

}

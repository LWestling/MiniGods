package com.juse.minigods;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
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
import com.juse.minigods.rendering.GameRenderer;
import com.juse.minigods.rendering.Sprite.Sprites;
import com.juse.minigods.rendering.renderers.FontRenderer;
import com.juse.minigods.rendering.renderers.ObstacleRenderer;
import com.juse.minigods.rendering.renderers.PlayerRenderer;
import com.juse.minigods.rendering.renderers.RendererInterface;
import com.juse.minigods.rendering.renderers.SpriteRenderer;
import com.juse.minigods.rendering.renderers.TerrainRenderer;
import com.juse.minigods.rendering.renderers.WaterRenderer;

import java.io.IOException;
import java.util.ArrayList;

public class GameActivity extends Activity {
    private Game game;
    private Highscore highscore;
    private GameRenderer gameRenderer;
    private FontRenderer fontRenderer;
    private SpriteRenderer spriteRenderer;
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

        SharedPreferences sharedPreferences = getSharedPreferences("general", Context.MODE_PRIVATE);
        boolean firstTime = sharedPreferences.getBoolean("first_time", true);

        new Thread(() -> {
            game.setHighscore(highscore.getHighscore(getApplicationContext()));
            game.setRenderCaches(fontRenderer.getTextCache(), spriteRenderer.getSpriteCache());
            game.showTutorial(firstTime);

            sharedPreferences.edit().putBoolean("first_time", false).apply();

            while (running) {
                game.update();
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

            if (isHighscore && score > 50)
                Logger.AnalyticsProperty(this, "player_type", "has_reached_50");
            else if (isHighscore && score > 5)
                Logger.AnalyticsProperty(this, "player_type", "has_reached_5");

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

        try {
            spriteRenderer = new SpriteRenderer(
                    new Sprites(BitmapFactory.decodeStream(getAssets().open("textures/icons.png")), 19, 28)
            );
            rendererList.add(spriteRenderer);
        } catch (IOException e) {
            Logger.CrashlyticsLog(e);
        }

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
                game.pressDown();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                game.pressUp();
                break;
        }

        return true;
    }

}

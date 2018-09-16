package com.juse.minigods.game;

import com.juse.minigods.map.Map;
import com.juse.minigods.map.Terrain;
import com.juse.minigods.rendering.CameraProjectionManager;
import com.juse.minigods.rendering.Font.TextCache;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by LukasW on 2018-03-22.
 * The game container that contains all game components, like terrain and player
 * and manipulates those components.
 */

public class Game {
    private static final float SCORE_POWER = 2.f, SPEED_POWER = 1.035f,
        SCORE_MUL = 0.0065f, SPEED_MUL = 0.013f, SPEED_START = 7.f,
        PLAYER_START_SPEED = 10.f, PLAYER_BASE_FALL_MUL = 0.45f, TREE_TIMER = 2.9f; // change with difficulty or something?
    private static final int ROWS = 14, COLUMNS = 18;

    private static final Vector3f START_POS = new Vector3f(-6.f, 1.2f, 3.f);
    public static final Vector3f CAMERA_START_POS = new Vector3f(0.f, 6.f, 14.f);
    private static final Vector3f CAMERA_START_DIR = new Vector3f(0.f, -2.25f, -1.f);

    private Map map;
    private Player player;
    private GameTimer gameTimer, pauseTimer;
    private UIManager uiManager;
    private Random random;

    private ConcurrentLinkedQueue<Obstacle> obstacles;

    private float score, pauseTime;

    private float playerSpeed, playerFallMultiplier;
    private float totalTime, mapSpeed, treeTimer;

    private static Vector4f universalLightPosition;
    private static Quaternionf universalLightRotation;
    private static CameraProjectionManager cameraProjectionManager; // THIS IS FOR TESTING; MOVE CAMERA TO LOGIC, OR JUST MAKE IT A CONSTANT

    private boolean gameOver, startNewGameSession;
    private int highscore;

    public Game() {
        player = new Player(new Vector3f(START_POS));
        gameTimer = new GameTimer();
        pauseTimer = new GameTimer();
        map = new Map(new Vector2f(-18.f, -6.f), new Vector2i(COLUMNS, ROWS));
        random = new Random();
        uiManager = new UIManager();

        playerSpeed = PLAYER_START_SPEED;
        playerFallMultiplier = PLAYER_BASE_FALL_MUL;

        cameraProjectionManager = new CameraProjectionManager();
        cameraProjectionManager.updateCamera(CAMERA_START_POS, CAMERA_START_DIR.normalize());

        obstacles = new ConcurrentLinkedQueue<>();
    }

    // This starts a new game session, reset player and such
    public void startGameSession(TextCache cache) {
        universalLightPosition = new Vector4f(5.f, 25.f, 0.f, 1.f);
        universalLightRotation = new Quaternionf();

        player.setPosition(new Vector3f(START_POS));
        player.setVelocity(new Vector3f(0.f, 0.f, playerSpeed * playerFallMultiplier));

        uiManager.setOverlayIngame(cache, highscore);

        obstacles.clear();
        map.reset();

        gameTimer.resetTimer();
        score = 0;
        totalTime = 0;
        gameOver = false;
        startNewGameSession = false;
        treeTimer = TREE_TIMER;
    }

    // todo refactor, lot of that stuff lol
    public void pauseGame(float dt) {
        pauseTime = dt;
        pauseTimer.resetTimer();
    }

    public void update(TextCache cache) {
        uiManager.update(cache);

        if (isGamePaused()) {
            return;
        }

        if (isGameOver()) {
            if (startNewGameSession) {
                uiManager.hideOverlayGameover(cache);
                startGameSession(cache);
            }
        } else {
            playingUpdate();

            if (isGameOver()) {
                uiManager.hideOverlayIngame(cache);
                uiManager.setOverlayGameover(cache, highscore);
                pauseGame(1.25f);

                if (score > highscore) {
                    highscore = (int) score;
                }
            }
        }
    }

    private void playingUpdate() {
        float dt = gameTimer.calcDeltaTime();
        universalLightRotation.rotate(dt * .05f, 0.f, 0.f);

        updateGame(dt);
        map.update(dt, mapSpeed);
        updateObstacles(dt);
        updatePlayer(dt);

        uiManager.setCurrentScore((int) getScore());
    }

    private void updatePlayer(float dt) {
        player.update(dt);
        if (player.getPosition().z < -player.getRadius() || player.getPosition().z() > ROWS)
            gameOver = true;
    }

    private void updateObstacles(float dt) {
        for (Obstacle obstacle : obstacles) {
            obstacle.update(dt, mapSpeed, map.getPosition().x());
            if (obstacle.isOffMap())
                obstacles.remove(); // first tree added will always be first to be removed
            if (player.getTilePosition().distance(obstacle.getTilePosition()) < player.getRadius()) {
                gameOver = true;
                return;
            }
        }
    }

    private void updateGame(float dt) {
        if ((treeTimer -= dt) <= 0.f) {
            spawnObstacleLine();
            treeTimer = (float) Math.pow(TREE_TIMER, 1 / mapSpeed * 1.4f) - .7f;
        }

        totalTime += dt;
        mapSpeed = (float) (Math.pow(totalTime, SPEED_POWER) * SPEED_MUL) + SPEED_START;
        score = (float) Math.pow(totalTime, SCORE_POWER) * SCORE_MUL;
    }

    private void spawnObstacleLine() {
        Terrain terrain = map.getTerrain();
        obstacles.add(new Obstacle(
                new Vector3f(
                        map.getPosition().x() + terrain.getWidth() - 1.f,
                        0.f,
                        random.nextInt(map.getTerrain().getRows() - 1)
                )
        ));
    }

    public Map getMap() {
        return map;
    }

    public Player getPlayer() {
        return player;
    }

    public ConcurrentLinkedQueue<Obstacle> getObstacles() {
        return obstacles;
    }

    public static CameraProjectionManager GetCamera() {
        return cameraProjectionManager;
    }

    public static Vector4f getUniversalLightPosition() {
        return new Vector4f(universalLightPosition).rotate(universalLightRotation);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public float getScore() {
        return score;
    }

    public void pressDown(float x, float y) {
        if (!isGamePaused()) {
            if (isGameOver()) {
                startNewGameSession = true;
            } else {
                player.setVelocity(new Vector3f(0.f, 0.f, -playerSpeed));
            }
        }
    }

    private boolean isGamePaused() {
        return pauseTime != 0 && pauseTimer.calcTimeSinceReset() < pauseTime;
    }

    public void pressUp(float x, float y) {
        player.setVelocity(new Vector3f(0.f, 0.f, playerSpeed * playerFallMultiplier));
    }

    public void setHighscore(int highscore) {
        this.highscore = highscore;
    }

    public int getHighscore() {
        return highscore;
    }
}

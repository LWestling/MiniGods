package com.juse.minigods.game;

import com.juse.minigods.map.Terrain;
import com.juse.minigods.rendering.CameraProjectionManager;

import org.joml.Vector3f;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by LukasW on 2018-03-22.
 * The game container that contains all game components, like terrain and player
 * and manipulates those components.
 */

public class Game {
    private static final float SCORE_POWER = 3.f, SPEED_POWER = 1.1f,
        SCORE_MUL = 0.001f, SPEED_MUL = 0.02f, SPEED_START = 2.3f, START_OFFSET = -19.f,
        PLAYER_START_SPEED = 6.5f, PLAYER_BASE_FALL_MUL = 0.35f, TREE_TIMER = 0.5f; // change with difficulty or something?
    private static final int ROWS = 13, COLUMNS = 18;

    private static final Vector3f START_POS = new Vector3f(-3.f, 0.f, 3.f);
    private static final Vector3f CAMERA_START_POS = new Vector3f(0.f, 6.0f, 11.5f);
    private static final Vector3f CAMERA_START_DIR = new Vector3f(0.f, -1.f, -1.f);

    private Terrain terrain;
    private Player player;
    private GameTimer gameTimer;
    private Random random;

    private ConcurrentLinkedQueue<Obstacle> obstacles;

    private float score;

    private float playerSpeed, playerFallMultiplier;
    private float totalTime, mapSpeed, treeTimer;

    private static CameraProjectionManager cameraProjectionManager; // THIS IS FOR TESTING; MOVE CAMERA TO LOGIC, OR JUST MAKE IT A CONSTANT

    public Game() {
        player = new Player(new Vector3f(START_POS));
        gameTimer = new GameTimer();
        terrain = new Terrain(ROWS, COLUMNS, START_OFFSET); // test numbers
        random = new Random();

        playerSpeed = PLAYER_START_SPEED;
        playerFallMultiplier = PLAYER_BASE_FALL_MUL;

        cameraProjectionManager = new CameraProjectionManager();
        cameraProjectionManager.updateCamera(CAMERA_START_POS, CAMERA_START_DIR.normalize());

        obstacles = new ConcurrentLinkedQueue<>();
    }

    // This starts a new game session, reset player and such
    public void startGameSession() {
        player.setPosition(new Vector3f(START_POS));
        player.setVelocity(new Vector3f(0.f, 0.f, playerSpeed * playerFallMultiplier));

        obstacles.clear();
        terrain.reset(START_OFFSET);

        gameTimer.resetTimer();
        score = 0;
        totalTime = 0;
        treeTimer = TREE_TIMER;
    }

    public void update() {
        float dt = gameTimer.calcDeltaTime();

        // test
        if ((treeTimer -= dt) <= 0.f) {
            spawnObstacleLine();
            treeTimer = TREE_TIMER + mapSpeed * 0.0001f; // less tree if faster to balance it out
        }

        updateGame(dt);

        terrain.update(dt, mapSpeed);

        for (Obstacle obstacle : obstacles) {
            obstacle.update(dt, mapSpeed, START_OFFSET);
            if (obstacle.isOffMap())
                obstacles.remove(); // first tree added will always be first to be removed
            if (player.getPosition().distance(obstacle.getPosition()) < player.getRadius()) {
                startGameSession();
                return;
            }
        }

        player.update(dt);
        if (player.getPosition().z < -player.getRadius() || player.getPosition().z() > ROWS - 1)
            startGameSession();
    }

    private void updateGame(float dt) {
        totalTime += dt;
        mapSpeed = (float) (Math.pow(totalTime, SPEED_POWER) * SPEED_MUL) + SPEED_START;
        score += Math.pow(totalTime, SCORE_POWER) * SCORE_MUL;
    }

    private void spawnObstacleLine() {
        obstacles.add(new Obstacle(
                new Vector3f(
                        START_OFFSET + COLUMNS * terrain.getColumnWidth() - 1.f,
                        0.f,
                        random.nextInt(terrain.getRows() - 1)
                )
        ));
    }

    public Terrain getTerrain() {
        return terrain;
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

    public float getScore() {
        return score;
    }

    public void pressDown(float x, float y) {
        player.setVelocity(new Vector3f(0.f, 0.f, -playerSpeed));
    }

    public void pressUp(float x, float y) {
        player.setVelocity(new Vector3f(0.f, 0.f, playerSpeed * playerFallMultiplier));
    }
}

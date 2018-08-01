package com.juse.minigods.game;

import com.juse.minigods.map.Map;
import com.juse.minigods.map.Terrain;
import com.juse.minigods.rendering.CameraProjectionManager;

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
    private static final float SCORE_POWER = 3.f, SPEED_POWER = 1.15f,
        SCORE_MUL = 0.001f, SPEED_MUL = 0.015f, SPEED_START = 2.7f,
        PLAYER_START_SPEED = 9.5f, PLAYER_BASE_FALL_MUL = 0.45f, TREE_TIMER = 1.8f; // change with difficulty or something?
    private static final int ROWS = 14, COLUMNS = 18;

    private static final Vector3f START_POS = new Vector3f(-6.f, 0.f, 3.f);
    public static final Vector3f CAMERA_START_POS = new Vector3f(0.f, 6.f, 14.f);
    private static final Vector3f CAMERA_START_DIR = new Vector3f(0.f, -2.25f, -1.f);

    private Map map;
    private Player player;
    private GameTimer gameTimer;
    private Random random;

    private ConcurrentLinkedQueue<Obstacle> obstacles;

    private float score;

    private float playerSpeed, playerFallMultiplier;
    private float totalTime, mapSpeed, treeTimer;

    private static Vector4f universalLightPosition;
    private static Quaternionf universalLightRotation;
    private static CameraProjectionManager cameraProjectionManager; // THIS IS FOR TESTING; MOVE CAMERA TO LOGIC, OR JUST MAKE IT A CONSTANT

    public Game() {
        player = new Player(new Vector3f(START_POS));
        gameTimer = new GameTimer();
        map = new Map(new Vector2f(-18.f, -6.f), new Vector2i(COLUMNS, ROWS));
        random = new Random();

        playerSpeed = PLAYER_START_SPEED;
        playerFallMultiplier = PLAYER_BASE_FALL_MUL;

        cameraProjectionManager = new CameraProjectionManager();
        cameraProjectionManager.updateCamera(CAMERA_START_POS, CAMERA_START_DIR.normalize());

        obstacles = new ConcurrentLinkedQueue<>();
    }

    // This starts a new game session, reset player and such
    public void startGameSession() {
        universalLightPosition = new Vector4f(5.f, 25.f, 0.f, 1.f);
        universalLightRotation = new Quaternionf();

        player.setPosition(new Vector3f(START_POS));
        player.setVelocity(new Vector3f(0.f, 0.f, playerSpeed * playerFallMultiplier));

        obstacles.clear();
        map.reset();

        gameTimer.resetTimer();
        score = 0;
        totalTime = 0;
        treeTimer = TREE_TIMER;
    }

    public void update() {
        float dt = gameTimer.calcDeltaTime();
        universalLightRotation.rotate(dt * .05f, 0.f, 0.f);

        updateGame(dt);

        // test
        if ((treeTimer -= dt) <= 0.f) {
            spawnObstacleLine();
            treeTimer = (float) Math.pow(TREE_TIMER, 1 / mapSpeed) - .6f;
            System.out.println("T: " + treeTimer);
        }

        map.update(dt, mapSpeed);

        for (Obstacle obstacle : obstacles) {
            obstacle.update(dt, mapSpeed, map.getPosition().x());
            if (obstacle.isOffMap())
                obstacles.remove(); // first tree added will always be first to be removed
            if (player.getPosition().distance(obstacle.getPosition()) < player.getRadius()) {
                startGameSession();
                return;
            }
        }

        player.update(dt);
        if (player.getPosition().z < -player.getRadius() || player.getPosition().z() > ROWS)
            startGameSession();
    }

    private void updateGame(float dt) {
        totalTime += dt;
        mapSpeed = (float) (Math.pow(totalTime, SPEED_POWER) * SPEED_MUL) + SPEED_START;
        score += Math.pow(totalTime, SCORE_POWER) * SCORE_MUL;
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

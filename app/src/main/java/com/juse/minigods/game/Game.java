package com.juse.minigods.game;

import com.juse.minigods.map.Terrain;
import com.juse.minigods.rendering.CameraProjectionManager;

import org.joml.Vector3f;

import java.util.ArrayList;

/**
 * Created by LukasW on 2018-03-22.
 * The game container that contains all game components, like terrain and player
 * and manipulates those components.
 */

public class Game {
    private static final float SCORE_POWER = 3.f, SPEED_POWER = 1.25f,
        SCORE_MUL = 0.001f, SPEED_MUL = 0.01f, START_OFFSET = -13.f; // change with difficulty or something?

    private static final Vector3f START_POS = new Vector3f(0.f, 0.f, 0.f);
    public static final Vector3f CAMERA_START_POS = new Vector3f(0.f, 2.f, 9.f);
    private static final Vector3f CAMERA_START_DIR = new Vector3f(0.f, -1.1f, -1.f);

    private Terrain terrain;
    private Player player;
    private ArrayList<Obstacle> obstacles;
    private GameTimer gameTimer;
    private float score;

    private static CameraProjectionManager cameraProjectionManager; // THIS IS FOR TESTING; MOVE CAMERA TO LOGIC, OR JUST MAKE IT A CONSTANT

    public Game() {
        player = new Player(START_POS);
        obstacles = new ArrayList<>();
        gameTimer = new GameTimer();
        terrain = new Terrain(11, 8, START_OFFSET); // test numbers

        cameraProjectionManager = new CameraProjectionManager();
        cameraProjectionManager.updateCamera(CAMERA_START_POS, CAMERA_START_DIR.normalize());
    }

    // This starts a new game session, reset player and such
    public void startGameSession() {
        player.setPosition(START_POS);
        obstacles.clear();
        terrain.reset(START_OFFSET);

        gameTimer.resetTimer();
        score = 0;
    }

    public void update() {
        float dt = gameTimer.calcDeltaTime();
        float mapSpeed = (float) (Math.pow(dt, SPEED_POWER) * SPEED_MUL);
        score += Math.pow(dt, SCORE_POWER) * SCORE_MUL;

        player.update(dt);
        terrain.update(dt, mapSpeed);
        obstacles.forEach(consumer -> consumer.update(dt, mapSpeed));

        if (Math.random() < 0.01f) // test
            spawnObstacleLine();
    }

    private void spawnObstacleLine() {
        obstacles.add(new Obstacle(
                new Vector3f(
                        500.f,
                        0.f,
                        (float) (Math.random() * 25.f)
                )
        ));
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public Player getPlayer() {
        return player;
    }

    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }

    public GameTimer getGameTimer() {
        return gameTimer;
    }

    public void updateCamera(Vector3f pos, Vector3f lookAlong) {
        cameraProjectionManager.updateCamera(pos, lookAlong);
    }

    public static CameraProjectionManager GetCamera() {
        return cameraProjectionManager;
    }

    public float getScore() {
        return score;
    }
}

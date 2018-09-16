package com.juse.minigods.game;

import com.juse.minigods.rendering.Font.TextCache;

import java.util.Locale;

/**
 * Simple class just to do some organisation, could probably be made "nice" but not really necessary.
 */
public class UIManager {
    // todo replace with localised?
    private final static String SCORE_FORMAT = "SCORE:\n%d.", HIGHSCORE_FORMAT = "HIGHSCORE:\n%d.",
            MSG_GRATZ = "NEW HIGHSCORE!", GAMEOVER_SCORE = "SCORE: %d.", CONTINUE = "PRESS TO PLAY AGAIN!";

    private enum UIState { NONE, IN_GAME, GAME_OVER }
    private UIState currentState;

    private int currentScore;

    // Text keys
    private int ingameScore, ingameHighscore;
    private int gameoverHighscoreMessage, gameoverScore, gameoverMessage;

    public UIManager() {
        currentState = UIState.NONE;
        currentScore = 0;
    }

    public void setOverlayIngame(TextCache cache, int highscore) {
        ingameScore = cache.addStringToRender(
                new TextCache.Text(String.format(Locale.ENGLISH, SCORE_FORMAT, 0), -0.9f, 0.75f)
        );
        ingameHighscore = cache.addStringToRender(
                new TextCache.Text(String.format(Locale.ENGLISH, HIGHSCORE_FORMAT, highscore), -0.9f, -0.75f)
        );

        currentState = UIState.IN_GAME;
    }

    public void setOverlayGameover(TextCache cache, int highscore) {
        if (currentScore > highscore) {
            gameoverHighscoreMessage = cache.addStringToRender(
                    new TextCache.Text(MSG_GRATZ, -0.9f, 0.65f, 2.f)
            );
        }

        gameoverScore = cache.addStringToRender(
                new TextCache.Text(String.format(Locale.ENGLISH, GAMEOVER_SCORE, currentScore), -0.45f, 0.2f, 2)
        );
        gameoverMessage = cache.addStringToRender(
                new TextCache.Text(CONTINUE, -0.55f, -.1f) // todo get x to center text somehow plz
        );

        currentState = UIState.GAME_OVER;
    }

    public void hideOverlayIngame(TextCache cache) {
        cache.removeStringToRender(ingameScore, ingameHighscore);
    }

    public void hideOverlayGameover(TextCache cache) {
        cache.removeStringToRender(gameoverScore, gameoverMessage, gameoverHighscoreMessage);
    }

    public void setCurrentScore(int score) {
        currentScore = score;
    }

    public void update(TextCache cache) {
        switch (currentState) {
            case IN_GAME:
                cache.updateString(ingameScore, String.format(Locale.ENGLISH, SCORE_FORMAT, currentScore));
                break;
            case GAME_OVER:
                break;
        }
    }
}

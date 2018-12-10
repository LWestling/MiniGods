package com.juse.minigods.game;

import com.juse.minigods.game.ui.TutorialUI;
import com.juse.minigods.rendering.Font.TextCache;
import com.juse.minigods.rendering.Sprite.SpriteCache;

import java.util.Locale;

/**
 * Simple class just to do some organisation, could probably be made "nice" but not really necessary.
 */
public class UIManager {
    // todo replace with localised?
    private final static String SCORE_FORMAT = "SCORE\n%d", HIGHSCORE_FORMAT = "HIGHSCORE\n%d",
            MSG_GRATZ = "NEW HIGHSCORE", GAMEOVER_SCORE = "SCORE %d", CONTINUE = "TOUCH THE SCREEN\nTO PLAY AGAIN";
    private enum UIState { NONE, IN_GAME, GAME_OVER, SIMPLE_TUTORIAL }
    private UIState currentState;

    private int currentScore;

    // Text keys
    private int ingameScore, ingameHighscore;
    private int gameoverHighscoreMessage, gameoverScore, gameoverMessage;

    // Caches, to render text and sprites
    private TextCache textCache;
    private SpriteCache spriteCache;
    private TutorialUI tutorialUI;

    public UIManager() {
        currentState = UIState.NONE;
        currentScore = 0;
        tutorialUI = new TutorialUI();
    }

    public void setCaches(TextCache textCache, SpriteCache spriteCache) {
        this.textCache = textCache;
        this.spriteCache = spriteCache;
    }

    public void setOverlayIngame(int highscore) {
        ingameScore = textCache.addStringToRender(
                new TextCache.Text(String.format(Locale.ENGLISH, SCORE_FORMAT, 0), -0.9f, 0.75f)
        );
        ingameHighscore = textCache.addStringToRender(
                new TextCache.Text(String.format(Locale.ENGLISH, HIGHSCORE_FORMAT, highscore), -0.9f, -0.75f)
        );

        currentState = UIState.IN_GAME;
    }

    public void setOverlayGameover(int highscore) {
        if (currentScore > highscore) {
            gameoverHighscoreMessage = textCache.addStringToRender(
                    new TextCache.Text(MSG_GRATZ, -0.9f, 0.65f, 1.f)
            );
        }

        gameoverScore = textCache.addStringToRender(
                new TextCache.Text(String.format(Locale.ENGLISH, GAMEOVER_SCORE, currentScore), -0.45f, 0.2f,1.f)
        );
        gameoverMessage = textCache.addStringToRender(
                new TextCache.Text(CONTINUE, -0.55f, -.1f) // todo get x to center text somehow plz
        );

        currentState = UIState.GAME_OVER;
    }

    public void setOverlayTutorial() {
        tutorialUI.setup(textCache, spriteCache);
        currentState = UIState.SIMPLE_TUTORIAL;
    }

    public void hideOverlayIngame() {
        textCache.removeStringToRender(ingameScore, ingameHighscore);
    }

    public void hideOverlayGameover() {
        textCache.removeStringToRender(gameoverScore, gameoverMessage, gameoverHighscoreMessage);
    }

    public void hideTutorialUI() {
        currentState = UIState.NONE;
        tutorialUI.hide(textCache, spriteCache);
    }

    public void setCurrentScore(int score) {
        currentScore = score;
    }

    public void update(float dt) {
        switch (currentState) {
            case IN_GAME:
                textCache.updateString(ingameScore, String.format(Locale.ENGLISH, SCORE_FORMAT, currentScore));
                break;
            case SIMPLE_TUTORIAL:
                tutorialUI.update(dt, textCache, spriteCache);
            case GAME_OVER:
                break;
        }
    }
}

package com.juse.minigods.game.ui;

import com.juse.minigods.rendering.Font.TextCache;
import com.juse.minigods.rendering.Sprite.SpriteCache;
import com.juse.minigods.rendering.Sprite.Sprites;

public class TutorialUI {
    private static final String HOW_TO_PLAY = "HOLD TO MOVE UP\nOR IT WILL MOVE DOWN";
    private int howToPlayText;
    private int hand, arrow;
    private double totalTime;

    public void setup(TextCache textCache, SpriteCache spriteCache) {
        howToPlayText = textCache.addStringToRender(
                new TextCache.Text(HOW_TO_PLAY, -.7f, -.75f)
        );

        totalTime = 0.f;

        hand = spriteCache.addSpriteToRender(
                new SpriteCache.RenderSprite(Sprites.SpriteId.HAND_PRESS, -.25f, 0.f)
        );
        arrow = spriteCache.addSpriteToRender(
                new SpriteCache.RenderSprite(Sprites.SpriteId.UP_ARROW, .25f, 0.f)
        );
    }

    public void update(float dt, TextCache textCache, SpriteCache spriteCache) {
        totalTime += (double) dt;
        double sinVal = Math.cos(totalTime);

        if ((totalTime % 6.28) - 3.14 < 0) {
            spriteCache.update(hand, Sprites.SpriteId.HAND_HOVER, -.1f, 0.f);
            spriteCache.update(arrow, Sprites.SpriteId.DOWN_ARROW,.1f, (float) (sinVal * 0.3));
        } else {
            spriteCache.update(hand, Sprites.SpriteId.HAND_PRESS, -.1f, 0.f);
            spriteCache.update(arrow, Sprites.SpriteId.UP_ARROW,.1f, (float) (sinVal * 0.3));
        }
    }

    public void hide(TextCache textCache, SpriteCache spriteCache) {
        textCache.removeStringToRender(howToPlayText);
        spriteCache.removeSpritesToRender(hand, arrow);
    }
}

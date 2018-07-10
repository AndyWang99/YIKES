package com.sodirea.flickeringinthedark.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sodirea.flickeringinthedark.FlickeringInTheDark;
import com.sodirea.flickeringinthedark.states.PlayState;

import java.util.Random;

public class Platform {

    public static final int MIN_HOLE_WIDTH = 120;
    public static final int MAX_ADDITIONAL_HOLE_WIDTH = 120;
    private int holeWidth;
    private Random xGenerator;
    private Texture platform;
    private Vector2 position;
    private Rectangle bounds1;
    private Rectangle bounds2;
    private boolean isCleared;

    public Platform(float y) {
        xGenerator = new Random();
        holeWidth = MIN_HOLE_WIDTH + xGenerator.nextInt(MAX_ADDITIONAL_HOLE_WIDTH);
        platform = new Texture("platform.png");
        position = new Vector2(xGenerator.nextInt(FlickeringInTheDark.WIDTH - holeWidth), y);
        bounds1 = new Rectangle(position.x - platform.getWidth(), position.y, platform.getWidth(), platform.getHeight());
        bounds2 = new Rectangle(position.x + holeWidth, position.y, platform.getWidth(), platform.getHeight());
        isCleared = false;
    }

    public int getHoleWidth() {
        return holeWidth;
    }

    public Rectangle getBounds1() {
        return bounds1;
    }
    public Rectangle getBounds2() {
        return bounds2;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Texture getTexture() {
        return platform;
    }

    public void reposition() {
        isCleared = false;
        position.set(xGenerator.nextInt(FlickeringInTheDark.WIDTH - holeWidth), position.y + PlayState.PLATFORM_INTERVALS * PlayState.NUM_PLATFORMS);
        bounds1.setPosition(position.x - platform.getWidth(), position.y);
        bounds2.setPosition(position.x + holeWidth, position.y);
    }

    public void cleared() {
        isCleared = true;
    }

    public void update(float dt) {
        bounds1.setPosition(position.x - platform.getWidth(), position.y);
        bounds2.setPosition(position.x + holeWidth, position.y);
        if (isCleared) {
            if (position.x < FlickeringInTheDark.WIDTH / 2 && position.x != -holeWidth) { // hole is closer to left, so move right platform over to left side
                position.x -= (position.x + holeWidth) / 10;
            } else if (position.x >= FlickeringInTheDark.WIDTH / 2 && position.x != FlickeringInTheDark.WIDTH) {
                position.x += (FlickeringInTheDark.WIDTH - position.x) / 10;
            }
        }
    }

    public void render(SpriteBatch sb) {
        sb.draw(platform, position.x - platform.getWidth(), position.y);
        sb.draw(platform, position.x + holeWidth, position.y);
    }

    public void dispose() {
        platform.dispose();
    }

}

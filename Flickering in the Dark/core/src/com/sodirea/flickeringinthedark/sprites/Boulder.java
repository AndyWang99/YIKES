package com.sodirea.flickeringinthedark.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.sodirea.flickeringinthedark.FlickeringInTheDark;

import java.util.Random;

import static com.sodirea.flickeringinthedark.sprites.Ball.GRAVITY;
import static com.sodirea.flickeringinthedark.sprites.Ball.SCALING_FACTOR;

public class Boulder {

    private static final int MIN_VELOCITY = 10;
    private static final int MAX_ADDITIONAL_VELOCITY = 10;
    private Texture wall;
    private Texture boulder;
    private Vector2 position;
    private Vector2 velocity;
    private Circle bounds;
    private boolean isOnPlatform;
    private Platform currentPlatform; // pointer to the current platform the boulder is sitting on

    public Boulder(float x, float y) {
        wall = new Texture("wall.png");
        boulder = new Texture("boulder.png");
        position = new Vector2(x, y);
        velocity = new Vector2(MIN_VELOCITY + new Random().nextInt(MAX_ADDITIONAL_VELOCITY), 0);
        bounds = new Circle(position.x + boulder.getWidth() / 2, position.y + boulder.getHeight() / 2, boulder.getWidth() / 2);
    }

    public Vector2 getPosition() {
        return position;
    }

    public Texture getTexture(){
        return boulder;
    }

    public Circle getBounds() {
        return bounds;
    }

    public void reposition(float x, float y) {
        position.set(x, y);
        velocity.set(MIN_VELOCITY + new Random().nextInt(MAX_ADDITIONAL_VELOCITY), 0);
        bounds.setPosition(position.x + boulder.getWidth() / 2, position.y + boulder.getHeight() / 2);
    }

    public void onOverlapWithAnyPlatform(Array<Platform> platformArray) {
        boolean setToFalse = true;
        for (Platform platform : platformArray) {
            if (Intersector.overlaps(bounds, platform.getBounds1()) || Intersector.overlaps(bounds, platform.getBounds2())) {
                isOnPlatform = true;
                setToFalse = false;
                currentPlatform = platform;
            }
        }
        if (setToFalse) {
            isOnPlatform = false;
            currentPlatform = null;
        }
    }

    public void update(float dt, Array<Platform> platformArray) {
        onOverlapWithAnyPlatform(platformArray);
        position.add(SCALING_FACTOR * velocity.x, SCALING_FACTOR * velocity.y);
        if (!isOnPlatform) { // add gravity to velocity if off the ground
            velocity.y += GRAVITY;
        } else {
            velocity.y = 0;
            position.y = currentPlatform.getPosition().y + currentPlatform.getTexture().getHeight();
        }
        if (position.x <= wall.getWidth()) {
            position.x = wall.getWidth();
            velocity.x = -velocity.x;
        }
        if (position.x + boulder.getWidth() >= FlickeringInTheDark.WIDTH - wall.getWidth()) {
            position.x = FlickeringInTheDark.WIDTH - wall.getWidth() - boulder.getWidth();
            velocity.x = -velocity.x;
        }
        bounds.setPosition(position.x + boulder.getWidth() / 2, position.y + boulder.getHeight() / 2);
    }

    public void render(SpriteBatch sb) {
        sb.draw(boulder, position.x, position.y);
    }

    public void dispose() {
        wall.dispose();
        boulder.dispose();
    }
}


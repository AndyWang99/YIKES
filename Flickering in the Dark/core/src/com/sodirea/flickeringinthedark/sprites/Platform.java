package com.sodirea.flickeringinthedark.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.sodirea.flickeringinthedark.FlickeringInTheDark;
import com.sodirea.flickeringinthedark.states.PlayState;

import java.util.Random;

import static com.sodirea.flickeringinthedark.states.PlayState.PIXELS_TO_METERS;

public class Platform {

    public static final int MIN_HOLE_WIDTH = 120;
    public static final int MAX_ADDITIONAL_HOLE_WIDTH = 60;
    private int holeWidth;
    private Random xGenerator;
    private Texture platform;
    private Vector2 position;
    private Rectangle bounds1;
    private Rectangle bounds2;
    private boolean isCleared;
    private boolean bridgePlaced;

    private BodyDef platformBodyDef;
    private Body platformBody;
    private PolygonShape platformBox;
    private BodyDef platformBodyDef2;
    private Body platformBody2;
    private PolygonShape platformBox2;

    public Platform(float y, World world) {
        xGenerator = new Random();
        holeWidth = MIN_HOLE_WIDTH + xGenerator.nextInt(MAX_ADDITIONAL_HOLE_WIDTH);
        platform = new Texture("platform.png");
        position = new Vector2(xGenerator.nextInt(FlickeringInTheDark.WIDTH - holeWidth), y);
        bounds1 = new Rectangle(position.x - platform.getWidth(), position.y, platform.getWidth(), platform.getHeight());
        bounds2 = new Rectangle(position.x + holeWidth, position.y, platform.getWidth(), platform.getHeight());
        isCleared = false;
        bridgePlaced = false;

        platformBodyDef = new BodyDef();
        platformBodyDef.position.set((position.x-platform.getWidth()/2)*PIXELS_TO_METERS, (position.y+platform.getHeight()/2)*PIXELS_TO_METERS);
        platformBody = world.createBody(platformBodyDef);
        platformBox = new PolygonShape();
        platformBox.setAsBox(platform.getWidth() / 2 * PIXELS_TO_METERS, platform.getHeight() / 2 * PIXELS_TO_METERS);
        platformBody.createFixture(platformBox, 0.0f);
        platformBody.setUserData(this);

        platformBodyDef2 = new BodyDef();
        platformBodyDef2.position.set((position.x+platform.getWidth()/2+holeWidth)*PIXELS_TO_METERS, (position.y+platform.getHeight()/2)*PIXELS_TO_METERS);
        platformBody2 = world.createBody(platformBodyDef2);
        platformBox2 = new PolygonShape();
        platformBox2.setAsBox(platform.getWidth() / 2 * PIXELS_TO_METERS, platform.getHeight() / 2 * PIXELS_TO_METERS);
        platformBody2.createFixture(platformBox2, 0.0f);
        platformBody2.createFixture(platformBox2, 0.0f);
        platformBody2.setUserData(this);

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

    public void reposition(World world) {
        isCleared = false;
        bridgePlaced = false;
        position.set(xGenerator.nextInt(FlickeringInTheDark.WIDTH - holeWidth), position.y + PlayState.PLATFORM_INTERVALS * PlayState.NUM_PLATFORMS);
        bounds1.setPosition(position.x - platform.getWidth(), position.y);
        bounds2.setPosition(position.x + holeWidth, position.y);
        platformBodyDef.position.set((position.x-platform.getWidth()/2)*PIXELS_TO_METERS, (position.y+platform.getHeight()/2)*PIXELS_TO_METERS);
        platformBody = world.createBody(platformBodyDef);
        platformBody.createFixture(platformBox, 0.0f);
        platformBody.setUserData(this);

        platformBodyDef2.position.set((position.x+platform.getWidth()/2+holeWidth)*PIXELS_TO_METERS, (position.y+platform.getHeight()/2)*PIXELS_TO_METERS);
        platformBody2 = world.createBody(platformBodyDef2);
        platformBody2.createFixture(platformBox2, 0.0f);
        platformBody2.setUserData(this);
    }

    public void cleared() {
        isCleared = true;
    }

    public boolean getIsCleared() {
        return isCleared;
    }

    public void update(float dt, World world) {
        bounds1.setPosition(position.x - platform.getWidth(), position.y);
        bounds2.setPosition(position.x + holeWidth, position.y);
        if (isCleared) {
            if (position.x < FlickeringInTheDark.WIDTH / 2 && position.x != -holeWidth) { // hole is closer to left, so move right platform over to left side
                position.x -= (position.x + holeWidth) / 10;
            } else if (position.x >= FlickeringInTheDark.WIDTH / 2 && position.x != FlickeringInTheDark.WIDTH) {
                position.x += (FlickeringInTheDark.WIDTH - position.x) / 10;
            }
            if (!bridgePlaced) {
                platformBodyDef.position.set(platformBody2.getPosition().x-platform.getWidth()/2*PIXELS_TO_METERS, (position.y + platform.getHeight() / 2) * PIXELS_TO_METERS);
                platformBody = world.createBody(platformBodyDef);
                platformBody.createFixture(platformBox, 0.0f);
                platformBody.setUserData(this);
                bridgePlaced = true;
            }
        }
    }

    public void render(SpriteBatch sb) {
        sb.draw(platform, position.x - platform.getWidth(), position.y);
        sb.draw(platform, position.x + holeWidth, position.y);
    }

    public void dispose() {
        platform.dispose();
        platformBox.dispose();
        platformBox2.dispose();
    }
}

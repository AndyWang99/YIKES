package com.sodirea.flickeringinthedark.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.sodirea.flickeringinthedark.FlickeringInTheDark;
import com.sodirea.flickeringinthedark.sprites.Ball;
import com.sodirea.flickeringinthedark.sprites.Boulder;
import com.sodirea.flickeringinthedark.sprites.Platform;

import java.util.Random;

public class PlayState extends State {

    public static final int PLATFORM_INTERVALS = 190;
    public static final int NUM_PLATFORMS = 6;
    public static final float PIXELS_TO_METERS = 0.01f;
    public static final int GRAVITY = -500;
    public static final float TIME_STEP = 1 / 300f;
    private Texture bg;
    private Texture ground;
    private Texture wall;
    private Texture ballTexture;
    private Ball ball;
    private int score;
    private Preferences prefs;
    private BitmapFont squrave;
    private Platform platform1;
    private Platform platform2;
    private Platform platform3;
    private Platform platform4;
    private Platform platform5;
    private Platform platform6;
    private Array<Platform> platformArray;
    private float lastPlatformTouchedPositionY;
    private Boulder boulder1;
    private Boulder boulder2;
    private Boulder boulder3;
    private Boulder boulder4;
    private Boulder boulder5;
    private Boulder boulder6;
    private Array<Boulder> boulderArray;
    private Random boulderGenerator;

    private float totalTimePassed;
    private boolean startCamera;
    private World world;
    private BodyDef groundBodyDef;
    private Body groundBody;
    private PolygonShape groundBox;
    private BodyDef wallBodyDef;
    private Body wallBody;
    private PolygonShape wallBox;
    private FixtureDef wallFixtureDef;
    private Fixture wallFixture;
    private BodyDef wallBodyDef2;
    private Body wallBody2;
    private PolygonShape wallBox2;
    private FixtureDef wallFixtureDef2;
    private Fixture wallFixture2;

    public PlayState(GameStateManager gsm) {
        super(gsm);
        Box2D.init();
        world = new World(new Vector2(0, GRAVITY), true);
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                if (contact.getFixtureA().getBody().getUserData() instanceof Ball || contact.getFixtureB().getBody().getUserData() instanceof Ball) { // if the foot sensor fixture of the ball touches a platform, and the lowest point of the ball is higher than the highest point of the touched platform, then clear the platform
                    ball.addNumberOfFootContacts(); // add to the total number of contact points
                    Platform platform = null;
                    if (contact.getFixtureA().getBody().getUserData() instanceof Platform) {
                        platform = (Platform) contact.getFixtureA().getBody().getUserData();
                    } else if (contact.getFixtureB().getBody().getUserData() instanceof Platform) {
                        platform = (Platform) contact.getFixtureB().getBody().getUserData();
                    }
                    if (platform != null && ball.getPosition().y > platform.getPosition().y + platform.getTexture().getHeight() && !platform.getIsCleared()) {
                        platform.cleared();
                        if (lastPlatformTouchedPositionY == 0) {
                            score++;
                        } else {
                            score += (platform.getPosition().y - lastPlatformTouchedPositionY) / PLATFORM_INTERVALS; // if they skipped a platform (as in they didn't clear one), then the next time they clear a platform, add score equal to the number of platforms they have skipped + the current platform
                        }
                        if (!startCamera) {
                            startCamera = true;
                        }
                        lastPlatformTouchedPositionY = platform.getPosition().y;
                    }
                }
            }

            @Override
            public void endContact(Contact contact) {
                if (contact.getFixtureA().getBody().getUserData() instanceof Ball || contact.getFixtureB().getBody().getUserData() instanceof Ball) {
                    ball.lessNumberOfFootContacts();
                }
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });
        startCamera = false;
        cam.setToOrtho(false, FlickeringInTheDark.WIDTH, FlickeringInTheDark.HEIGHT);
        bg = new Texture("bg.png");
        ground = new Texture("ground.png");
        wall = new Texture("wall.png");
        ballTexture = new Texture("ball.png");
        ball = new Ball(cam.position.x - ballTexture.getWidth() / 2, ground.getHeight(), world);
        score = 0;
        squrave = new BitmapFont(Gdx.files.internal("squrave.fnt"), false);
        prefs = Gdx.app.getPreferences("Prefs");
        platform1 = new Platform(ground.getHeight() + PLATFORM_INTERVALS, world);
        platform2 = new Platform(ground.getHeight() + 2 * PLATFORM_INTERVALS, world);
        platform3 = new Platform(ground.getHeight() + 3 * PLATFORM_INTERVALS, world);
        platform4 = new Platform(ground.getHeight() + 4 * PLATFORM_INTERVALS, world);
        platform5 = new Platform(ground.getHeight() + 5 * PLATFORM_INTERVALS, world);
        platform6 = new Platform(ground.getHeight() + 6 * PLATFORM_INTERVALS, world);
        platformArray = new Array<Platform>();
        platformArray.add(platform1);
        platformArray.add(platform2);
        platformArray.add(platform3);
        platformArray.add(platform4);
        platformArray.add(platform5);
        platformArray.add(platform6);
        lastPlatformTouchedPositionY = 0;
        boulder1 = new Boulder(PLATFORM_INTERVALS, -100, world);
        boulder2 = new Boulder(2 * PLATFORM_INTERVALS, -100, world);
        boulder3 = new Boulder(3 * PLATFORM_INTERVALS, -100, world);
        boulder4 = new Boulder(4 * PLATFORM_INTERVALS, -100, world);
        boulder5 = new Boulder(5 * PLATFORM_INTERVALS, -100, world);
        boulder6 = new Boulder(6 * PLATFORM_INTERVALS, -100, world);
        boulderArray = new Array<Boulder>();
        boulderArray.add(boulder1);
        boulderArray.add(boulder2);
        boulderArray.add(boulder3);
        boulderArray.add(boulder4);
        boulderArray.add(boulder5);
        boulderArray.add(boulder6);
        boulderGenerator = new Random();
        totalTimePassed = 0;

        groundBodyDef = new BodyDef();
        groundBodyDef.position.set(ground.getWidth() / 2 * PIXELS_TO_METERS, ground.getHeight() / 2 * PIXELS_TO_METERS);
        groundBody = world.createBody(groundBodyDef);
        groundBox = new PolygonShape();
        groundBox.setAsBox(ground.getWidth() / 2 * PIXELS_TO_METERS, ground.getHeight() / 2 * PIXELS_TO_METERS);
        groundBody.createFixture(groundBox, 0.0f);

        wallBodyDef = new BodyDef();
        wallBodyDef.position.set(wall.getWidth() / 2 * PIXELS_TO_METERS, wall.getHeight() / 2 * PIXELS_TO_METERS);
        wallBody = world.createBody(wallBodyDef);
        wallBox = new PolygonShape();
        wallBox.setAsBox(wall.getWidth() / 2 * PIXELS_TO_METERS, wall.getHeight() / 2 * PIXELS_TO_METERS);
        wallFixtureDef = new FixtureDef();
        wallFixtureDef.shape = wallBox;
        wallFixtureDef.density = 0.0f;
        wallFixtureDef.friction = 0.0f;
        wallFixture = wallBody.createFixture(wallFixtureDef);

        wallBodyDef2 = new BodyDef();
        wallBodyDef2.position.set((cam.position.x + cam.viewportWidth / 2 - wall.getWidth() + wall.getWidth() / 2) * PIXELS_TO_METERS, wall.getHeight() / 2 * PIXELS_TO_METERS);
        wallBody2 = world.createBody(wallBodyDef2);
        wallBox2 = new PolygonShape();
        wallBox2.setAsBox(wall.getWidth() / 2 * PIXELS_TO_METERS, wall.getHeight() / 2 * PIXELS_TO_METERS);
        wallFixtureDef2 = new FixtureDef();
        wallFixtureDef2.shape = wallBox2;
        wallFixtureDef2.density = 0.0f;
        wallFixtureDef2.friction = 0.0f;
        wallFixture2 = wallBody2.createFixture(wallFixtureDef2);
    }

    @Override
    protected void handleInput() {
        if (Gdx.input.justTouched() && ball.getNumberOfFootContacts() > 0) {
            ball.setBodyLinearVelocity(ball.getBodyLinearVelocity().x, 50f);
        }
        float accelX = Gdx.input.getAccelerometerX();
        if (accelX > 0) {
            ball.setBodyLinearVelocity(-20f * accelX, ball.getBodyLinearVelocity().y);
        }
        if (accelX < 0) {
            ball.setBodyLinearVelocity(-20f * accelX, ball.getBodyLinearVelocity().y);
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        wallBody.setTransform(new Vector2(wallBody.getPosition().x, cam.position.y * PIXELS_TO_METERS), wallBody.getAngle());
        wallBody2.setTransform(new Vector2(wallBody2.getPosition().x, cam.position.y * PIXELS_TO_METERS), wallBody2.getAngle());
        ball.update(dt);
        for (Boulder boulder: boulderArray) {
            boulder.update(dt);
        }
        for (int i = 0; i < platformArray.size; i++) {
            Platform platform = platformArray.get(i);
            platform.update(dt);
            if (platform.getPosition().y + platform.getTexture().getHeight() < cam.position.y - cam.viewportHeight / 2) {
                platform.reposition(platform.getPosition().y + PLATFORM_INTERVALS * NUM_PLATFORMS);
                if (boulderGenerator.nextBoolean()) {
                    boulderArray.get(i).reposition(platform.getPosition().x, platform.getPosition().y + platform.getTexture().getHeight());
                }
            }
        }
        if (startCamera) {
            if (totalTimePassed < 60) {
                totalTimePassed += dt;
            }
            cam.position.y += 4 * Ball.SCALING_FACTOR * (Math.pow(1.02, totalTimePassed) + 2);
            cam.update();
        }
        if (cam.position.y - cam.viewportHeight / 2 > ball.getPosition().y + ball.getTexture().getHeight()) {
            gsm.set(new PlayState(gsm));
        }
        world.step(TIME_STEP, 6, 2);
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(bg, 0, cam.position.y - cam.viewportHeight / 2);
        sb.draw(ground, 0, 0);
        sb.draw(wall, 0, cam.position.y - cam.viewportHeight / 2);
        sb.draw(wall, cam.position.x + cam.viewportWidth / 2 - wall.getWidth(), cam.position.y - cam.viewportHeight / 2);
        ball.render(sb);
        for (Platform platform : platformArray) {
            platform.render(sb);
        }
        for (Boulder boulder : boulderArray) {
            boulder.render(sb);
        }
        squrave.draw(sb, Integer.toString(score), cam.position.x, cam.position.y+cam.viewportHeight/2, 0, Align.center, false);
        sb.end();
    }

    @Override
    public void dispose() {
        bg.dispose();
        ground.dispose();
        wall.dispose();
        ballTexture.dispose();
        ball.dispose();
        squrave.dispose();
        platform1.dispose();
        platform2.dispose();
        platform3.dispose();
        platform4.dispose();
        platform5.dispose();
        platform6.dispose();
        boulder1.dispose();
        boulder2.dispose();
        boulder3.dispose();
        boulder4.dispose();
        boulder5.dispose();
        boulder6.dispose();
        groundBox.dispose();
        wallBox.dispose();
        wallBox2.dispose();
    }
}

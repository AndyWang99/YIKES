package com.sodirea.yikes.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
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
import com.sodirea.yikes.Yikes;
import com.sodirea.yikes.sprites.Ball;
import com.sodirea.yikes.sprites.Boulder;
import com.sodirea.yikes.sprites.Platform;

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
    private Texture deathscreen;
    private Ball ball;
    private Sound jump;
    private Sound gameover;
    private Sound menuclick;
    private BitmapFont squrave;
    private Preferences prefs;

    private boolean dead;
    private Vector2 deathscreenPos;
    private int score;
    private int airJumpsRemaining;
    private float totalTimePassed;
    private boolean startCamera;

    private Array<Platform> platformArray;
    private float lastPlatformTouchedPositionY;
    private Array<Boulder> boulderArray;
    private Random boulderGenerator;

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
                // if the foot sensor fixture of the ball touches a platform, and the lowest point of the ball is higher than the highest point of the touched platform, then clear the platform
                if (contact.getFixtureA().getBody().getUserData() instanceof Ball || contact.getFixtureB().getBody().getUserData() instanceof Ball) {
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
        cam.setToOrtho(false, Yikes.WIDTH, Yikes.HEIGHT);

        bg = new Texture("bg.png");
        ground = new Texture("ground.png");
        wall = new Texture("wall.png");
        ballTexture = new Texture("ball.png");
        deathscreen = new Texture("deathscreen.png");
        ball = new Ball(cam.position.x - ballTexture.getWidth() / 2, ground.getHeight(), world);
        jump = Gdx.audio.newSound(Gdx.files.internal("jump.mp3"));
        gameover = Gdx.audio.newSound(Gdx.files.internal("gameover.wav"));
        menuclick = Gdx.audio.newSound(Gdx.files.internal("menuclick.wav"));
        squrave = new BitmapFont(Gdx.files.internal("squrave.fnt"), false);

        startCamera = false;
        dead = false;
        deathscreenPos = new Vector2(cam.position.x - cam.viewportWidth/2 - deathscreen.getWidth(), cam.position.y);
        score = 0;
        totalTimePassed = 0;
        prefs = Gdx.app.getPreferences("Prefs");
        if (prefs.getBoolean("doublejump Toggle", false)) { // if they own double jump, give them an air jump
            airJumpsRemaining = 1;
        } else {
            airJumpsRemaining = 0;
        }

        platformArray = new Array<Platform>();
        boulderArray = new Array<Boulder>();
        // initializing the two arrays with platforms and boulders
        for (int i = 1; i <= NUM_PLATFORMS; i++) {
            platformArray.add(new Platform(ground.getHeight() + i * PLATFORM_INTERVALS, world));
            boulderArray.add(new Boulder(i * PLATFORM_INTERVALS, -100, world));
        }
        boulderGenerator = new Random();

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
        if (!dead) {
            // if they touch the screen and the foot sensor is in contact with something other than the ball, then jump
            if (Gdx.input.justTouched() && ball.getNumberOfFootContacts() > 0) {
                ball.setBodyLinearVelocity(ball.getBodyLinearVelocity().x, 50f);
                // if they own double jump and their foot sensor is in contact with something, then give them their air jumps back
                if (prefs.getBoolean("DOUBLE JUMP Toggle", false)) {
                    airJumpsRemaining = 1;
                }
                jump.play(1f);
            // if their foot sensor is not in contact with anything, but they have air jumps, then jump
            } else if (Gdx.input.justTouched() && ball.getNumberOfFootContacts() == 0 && airJumpsRemaining > 0) {
                ball.setBodyLinearVelocity(ball.getBodyLinearVelocity().x, 50f);
                airJumpsRemaining--;
                jump.play(1f);
            }
        } else { // if they are dead, then pressing on the screen take sthem back to the menu state
            if (Gdx.input.justTouched()) {
                menuclick.play(1f);
                if (deathscreenPos.x+deathscreen.getWidth()/2 < cam.position.x) {
                    deathscreenPos.x = cam.position.x - deathscreen.getWidth()/2;
                } else {
                    gsm.set(new MenuState(gsm));
                }
            }
        }
        // player's horizontal movements are controlled by tilting the screen
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
        if (dead) {
            // controls the animation for sliding the death screen from the left side of the screen
            if (deathscreenPos.x+deathscreen.getWidth()/2 < cam.position.x) {
                deathscreenPos.x += (cam.position.x - deathscreenPos.x+deathscreen.getWidth()/2) / 50;
            }
            deathscreenPos.y = cam.position.y;
        }
        wallBody.setTransform(new Vector2(wallBody.getPosition().x, cam.position.y * PIXELS_TO_METERS), wallBody.getAngle());
        wallBody2.setTransform(new Vector2(wallBody2.getPosition().x, cam.position.y * PIXELS_TO_METERS), wallBody2.getAngle());
        ball.update(dt);
        for (Boulder boulder: boulderArray) {
            boulder.update(dt);
        }
        for (int i = 0; i < platformArray.size; i++) {
            Platform platform = platformArray.get(i);
            platform.update(dt);
            // if a platform falls below the screen, then reposition the platform by putting it above the screen
            if (platform.getPosition().y + platform.getTexture().getHeight() < cam.position.y - cam.viewportHeight / 2) {
                platform.reposition(platform.getPosition().y + PLATFORM_INTERVALS * NUM_PLATFORMS);
                // 50% chance to also reposition a boulder with the newly repositioned platform
                if (boulderGenerator.nextBoolean()) {
                    boulderArray.get(i).reposition(platform.getPosition().x, platform.getPosition().y + platform.getTexture().getHeight());
                }
            }
        }
        // if startCamera is true, then start scrolling upwards, while scaling the scroll speed with time
        if (startCamera) {
            if (totalTimePassed < 60) {
                totalTimePassed += dt;
            }
            cam.position.y += 4 * Ball.SCALING_FACTOR * (Math.pow(1.02, totalTimePassed) + 2);
            cam.update();
        }
        // checks if the ball fell under the screen, i.e. game over
        if (cam.position.y - cam.viewportHeight / 2 > ball.getPosition().y + ball.getTexture().getHeight() && !dead) {
            // if their score this time is greater than this high score in preferences, then put a new high score
            if (prefs.getInteger("highscore", 0) < score) {
                prefs.putInteger("highscore", score);
                prefs.flush();
            }
            dead = true;
            gameover.play(1f);
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
        if (dead) {
            sb.draw(deathscreen, deathscreenPos.x, deathscreenPos.y);
            squrave.getData().setScale(0.5f, 0.5f);
            squrave.draw(sb, "GAME OVER", deathscreenPos.x+deathscreen.getWidth()/2, deathscreenPos.y+deathscreen.getHeight() - deathscreen.getHeight() / 10, 0, Align.center, false);
            squrave.getData().setScale(0.35f, 0.35f);
            squrave.draw(sb, Integer.toString(score), deathscreenPos.x+deathscreen.getWidth()/2+deathscreen.getWidth()/4, deathscreenPos.y+deathscreen.getHeight()/2+deathscreen.getHeight()/12 - deathscreen.getHeight() / 20, 0, Align.left, false);
            squrave.draw(sb, "SCORE", deathscreenPos.x+deathscreen.getWidth()/20, deathscreenPos.y+deathscreen.getHeight()/2+deathscreen.getHeight()/12 - deathscreen.getHeight() / 20, 0, Align.left, false);
            squrave.draw(sb, Integer.toString(prefs.getInteger("highscore", 0)), deathscreenPos.x+deathscreen.getWidth()/2+deathscreen.getWidth()/4,deathscreenPos.y+deathscreen.getHeight()/4+deathscreen.getHeight()/12 - deathscreen.getHeight() / 20, 0, Align.left, false);
            squrave.draw(sb, "HIGHSCORE", deathscreenPos.x+deathscreen.getWidth()/20,deathscreenPos.y+deathscreen.getHeight()/4+deathscreen.getHeight()/12 - deathscreen.getHeight() / 20, 0, Align.left, false);
        }
        squrave.getData().setScale(1f, 1f);
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
        jump.dispose();
        gameover.dispose();
        menuclick.dispose();
        deathscreen.dispose();
        squrave.dispose();
        groundBox.dispose();
        wallBox.dispose();
        wallBox2.dispose();
    }
}

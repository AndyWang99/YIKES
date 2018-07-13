package com.sodirea.flickeringinthedark.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.sodirea.flickeringinthedark.FlickeringInTheDark;
import com.sodirea.flickeringinthedark.sprites.Ball;
import com.sodirea.flickeringinthedark.sprites.Boulder;
import com.sodirea.flickeringinthedark.sprites.Platform;

public class PlayState extends State {

    public static final int PLATFORM_INTERVALS = 190;
    public static final int NUM_PLATFORMS = 6;
    public static final float PIXELS_TO_METERS = 0.01f;
    private Texture bg;
    private Texture ground;
    private Texture wall;
    private Texture ballTexture;
    private Ball ball;
    private Platform platform1;
    private Platform platform2;
    private Platform platform3;
    private Platform platform4;
    private Platform platform5;
    private Platform platform6;
    private Array<Platform> platformArray;
    private Boulder boulder1;

    private float totalTimePassed;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private BodyDef ballBodyDef;
    private Body ballBody;
    private CircleShape ballCircle;
    private FixtureDef ballFixtureDef;
    private Fixture ballFixture;
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
        world = new World(new Vector2(0, -500), true);
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                if (contact.getFixtureA().getBody().getUserData() != null
                        && contact.getFixtureB().getBody().getUserData() != null
                        && (contact.getFixtureA().getBody().getUserData() instanceof Ball || contact.getFixtureB().getBody().getUserData() instanceof Ball)) {
                    Platform platform;
                    if (contact.getFixtureA().getBody().getUserData() instanceof Platform) {
                        platform = (Platform) contact.getFixtureA().getBody().getUserData();
                    } else {
                        platform = (Platform) contact.getFixtureB().getBody().getUserData();
                    }
                    if (ball.getPosition().y > platform.getPosition().y +platform.getTexture().getHeight()) {
                        platform.cleared();
                        System.out.println("CLEARED");
                    }
                }
            }
            @Override
            public void endContact(Contact contact) { }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) { }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) { }
        });

        debugRenderer = new Box2DDebugRenderer();
        cam.setToOrtho(false, FlickeringInTheDark.WIDTH, FlickeringInTheDark.HEIGHT);
        bg = new Texture("bg.png");
        ground = new Texture("ground.png");
        wall = new Texture("wall.png");
        ballTexture = new Texture("ball.png");
        ball = new Ball(cam.position.x - ballTexture.getWidth() / 2, ground.getHeight());
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
        boulder1 = new Boulder(platform2.getPosition().x, platform6.getPosition().y + platform6.getTexture().getHeight());
        totalTimePassed = 0;

        ballBodyDef = new BodyDef();
        ballBodyDef.type = BodyDef.BodyType.DynamicBody;
        ballBodyDef.position.set((ball.getPosition().x+ball.getTexture().getWidth()/2) * PIXELS_TO_METERS, (ball.getPosition().y+ball.getTexture().getHeight()/2) * PIXELS_TO_METERS); // convert pixel coordinates to physics ball coodinates
        ballBody = world.createBody(ballBodyDef);
        ballCircle = new CircleShape();
        ballCircle.setRadius((ball.getTexture().getWidth()/2) * PIXELS_TO_METERS);
        ballFixtureDef = new FixtureDef();
        ballFixtureDef.shape = ballCircle;
        ballFixtureDef.density = 0.5f;
        ballFixtureDef.friction = 0.4f;
        ballFixture = ballBody.createFixture(ballFixtureDef);
        ballBody.setUserData(ball);

        groundBodyDef = new BodyDef();
        groundBodyDef.position.set(ground.getWidth()/2*PIXELS_TO_METERS, ground.getHeight()/2*PIXELS_TO_METERS);
        groundBody = world.createBody(groundBodyDef);
        groundBox = new PolygonShape();
        groundBox.setAsBox(ground.getWidth() / 2 * PIXELS_TO_METERS, ground.getHeight() / 2 * PIXELS_TO_METERS);
        groundBody.createFixture(groundBox, 0.0f);

        wallBodyDef = new BodyDef();
        wallBodyDef.position.set(wall.getWidth()/2*PIXELS_TO_METERS, wall.getHeight()/2*PIXELS_TO_METERS);
        wallBody = world.createBody(wallBodyDef);
        wallBox = new PolygonShape();
        wallBox.setAsBox(wall.getWidth() / 2 * PIXELS_TO_METERS, wall.getHeight() / 2 * PIXELS_TO_METERS);
        wallFixtureDef = new FixtureDef();
        wallFixtureDef.shape = wallBox;
        wallFixtureDef.density = 0.0f;
        wallFixtureDef.friction = 0.0f;
        wallFixture = wallBody.createFixture(wallFixtureDef);

        wallBodyDef2 = new BodyDef();
        wallBodyDef2.position.set((cam.position.x + cam.viewportWidth / 2 - wall.getWidth() + wall.getWidth()/2) * PIXELS_TO_METERS, wall.getHeight()/2*PIXELS_TO_METERS);
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
        if (Gdx.input.justTouched() && ballBody.getLinearVelocity().y == 0) {
            ballBody.setLinearVelocity(ballBody.getLinearVelocity().x, 50f);
        }
        float accelX = Gdx.input.getAccelerometerX();
        if (accelX > 0) {
            ballBody.setLinearVelocity(-20f * accelX, ballBody.getLinearVelocity().y);
        }
        if (accelX < 0) {
            ballBody.setLinearVelocity(-20f * accelX, ballBody.getLinearVelocity().y);
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        ball.setPosition(ballBody.getPosition().x/PIXELS_TO_METERS-ball.getTexture().getWidth()/2, ballBody.getPosition().y/PIXELS_TO_METERS-ball.getTexture().getHeight()/2); // convert physics ball coordinates back to render/pixel coordinates
        wallBody.setTransform(new Vector2(wallBody.getPosition().x, cam.position.y*PIXELS_TO_METERS), wallBody.getAngle());
        wallBody2.setTransform(new Vector2(wallBody2.getPosition().x, cam.position.y*PIXELS_TO_METERS), wallBody2.getAngle());
        ball.update(dt);
        boulder1.update(dt, platformArray);
        for (int i = 0; i < platformArray.size; i++) {
            Platform platform = platformArray.get(i);
            platform.update(dt, world);
            if (platform.getPosition().y + platform.getTexture().getHeight() < cam.position.y - cam.viewportHeight / 2) {
                platform.reposition(world);
            }
        }
        if (totalTimePassed < 60) {
            totalTimePassed += dt;
        }
        cam.position.y += 4 * Ball.SCALING_FACTOR * (Math.pow(1.02, totalTimePassed) + 2);
        cam.update();
        if (cam.position.y - cam.viewportHeight / 2 > ball.getPosition().y + ball.getTexture().getHeight()) {
            gsm.set(new PlayState(gsm));
        }
        world.step(1/300f, 6, 2);
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
        boulder1.render(sb);
        sb.end();
        debugRenderer.render(world, cam.combined);

    }

    @Override
    public void dispose() {
        bg.dispose();
        ground.dispose();
        wall.dispose();
        ballTexture.dispose();
        ball.dispose();
        platform1.dispose();
        platform2.dispose();
        platform3.dispose();
        platform4.dispose();
        platform5.dispose();
        platform6.dispose();
        boulder1.dispose();
        ballCircle.dispose();
        groundBox.dispose();
        wallBox.dispose();
        wallBox2.dispose();
    }
}

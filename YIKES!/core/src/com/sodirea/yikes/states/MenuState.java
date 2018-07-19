package com.sodirea.yikes.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.sodirea.yikes.Yikes;
import com.sodirea.yikes.sprites.Ball;
import com.sodirea.yikes.sprites.Boulder;
import com.sodirea.yikes.sprites.Platform;

import java.util.Random;

import static com.sodirea.yikes.states.PlayState.GRAVITY;
import static com.sodirea.yikes.states.PlayState.NUM_PLATFORMS;
import static com.sodirea.yikes.states.PlayState.PIXELS_TO_METERS;
import static com.sodirea.yikes.states.PlayState.PLATFORM_INTERVALS;
import static com.sodirea.yikes.states.PlayState.TIME_STEP;

public class MenuState extends State {

    private Texture bg;
    private Texture ground;
    private Texture wall;
    private Texture ballTexture;
    private Ball ball;
    private Sound menuclick;
    private Texture shopBtn;
    private BitmapFont squrave;
    private Platform platform1;
    private Platform platform2;
    private Platform platform3;
    private Platform platform4;
    private Platform platform5;
    private Platform platform6;
    private Array<Platform> platformArray;
    private Boulder boulder1;
    private Boulder boulder2;
    private Boulder boulder3;
    private Boulder boulder4;
    private Boulder boulder5;
    private Boulder boulder6;
    private Array<Boulder> boulderArray;
    private Random boulderGenerator;
    private Boolean startScrollDown;
    private Boolean setCamY;
    private float timePassed;
    private boolean addToSize;

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

    public MenuState(GameStateManager gsm) {
        super(gsm);
        cam.setToOrtho(false, Yikes.WIDTH, Yikes.HEIGHT);
        bg = new Texture("bg.png");
        ground = new Texture("ground.png");
        wall = new Texture("wall.png");
        menuclick = Gdx.audio.newSound(Gdx.files.internal("menuclick.wav"));
        squrave = new BitmapFont(Gdx.files.internal("squrave.fnt"), false);
        Box2D.init();
        world = new World(new Vector2(0, GRAVITY), true);
        ballTexture = new Texture("ball.png");
        ball = new Ball(cam.position.x - ballTexture.getWidth() / 2, ground.getHeight(), world);
        shopBtn = new Texture("shopbtn.png");
        startScrollDown = false;
        setCamY = false;
        timePassed = 0;
        addToSize = true;
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
        if (Gdx.input.justTouched()) {
            menuclick.play(1f);
            Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            cam.unproject(mousePos);
            if (mousePos.x > cam.position.x + cam.viewportWidth/3 + cam.viewportWidth/20 - shopBtn.getWidth()
                    && mousePos.x < cam.position.x + cam.viewportWidth/3 + cam.viewportWidth/20
                    && mousePos.y > cam.position.y - cam.viewportHeight/5
                    && mousePos.y < cam.position.y - cam.viewportHeight/5 + shopBtn.getHeight()) {
                gsm.set(new ShopState(gsm));
            } else {
                startScrollDown = true;
            }
        }
    }

    @Override
    public void update(float dt) {
        if (!setCamY) {
            cam.position.y = 5000;
            setCamY = true;
        }
        handleInput();
        wallBody.setTransform(new Vector2(wallBody.getPosition().x, cam.position.y * PIXELS_TO_METERS), wallBody.getAngle());
        wallBody2.setTransform(new Vector2(wallBody2.getPosition().x, cam.position.y * PIXELS_TO_METERS), wallBody2.getAngle());
        for (Boulder boulder: boulderArray) {
            boulder.update(dt);
        }
        for (int i = 0; i < platformArray.size; i++) {
            Platform platform = platformArray.get(i);
            platform.update(dt);
            if (!startScrollDown) {
                if (platform.getPosition().y + platform.getTexture().getHeight() < cam.position.y - cam.viewportHeight / 2) {
                    platform.reposition(platform.getPosition().y + PLATFORM_INTERVALS * NUM_PLATFORMS);
                    if (boulderGenerator.nextBoolean()) {
                        boulderArray.get(i).reposition(platform.getPosition().x, platform.getPosition().y + platform.getTexture().getHeight());
                    }
                }
            } else {
                if (platform.getPosition().y > cam.position.y + cam.viewportHeight / 2 && platform.getPosition().y - PLATFORM_INTERVALS * NUM_PLATFORMS >= ground.getHeight() + PLATFORM_INTERVALS) {
                    platform.reposition(platform.getPosition().y - PLATFORM_INTERVALS * NUM_PLATFORMS);
                    if (boulderGenerator.nextBoolean() && platform.getPosition().y > ground.getHeight() + PLATFORM_INTERVALS * NUM_PLATFORMS) {
                        boulderArray.get(i).reposition(platform.getPosition().x, platform.getPosition().y + platform.getTexture().getHeight());
                    }
                }
            }
        }
        if (timePassed > 0.5f) {
            addToSize = false;
        }
        if (timePassed < 0) {
            addToSize = true;
        }
        if (addToSize) {
            timePassed += dt;
        } else {
            timePassed -= dt;
        }
        if (!startScrollDown) {
            cam.position.y += 24 * Ball.SCALING_FACTOR;
        } else {
            cam.position.y -= cam.position.y / 40;
            if (cam.position.y - cam.viewportHeight / 2 <= 0) {
                gsm.set(new PlayState(gsm));
            }
        }
        cam.update();
        world.step(TIME_STEP, 6, 2);
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(bg, 0, cam.position.y - cam.viewportHeight / 2);
        sb.draw(ground, 0, 0);
        sb.draw(wall, 0, cam.position.y - cam.viewportHeight / 2);
        ball.render(sb);
        sb.draw(wall, cam.position.x + cam.viewportWidth / 2 - wall.getWidth(), cam.position.y - cam.viewportHeight / 2);
        for (Platform platform : platformArray) {
            platform.render(sb);
        }
        for (Boulder boulder : boulderArray) {
            boulder.render(sb);
        }
        sb.draw(shopBtn, cam.position.x + cam.viewportWidth/3 + cam.viewportWidth/20 - shopBtn.getWidth(), cam.position.y - cam.viewportHeight/5);
        squrave.getData().setScale(0.55f, 0.55f);
        squrave.draw(sb, "SHOP", cam.position.x + cam.viewportWidth/3 + cam.viewportWidth/20 - shopBtn.getWidth()/2, cam.position.y - cam.viewportHeight/5 + shopBtn.getHeight()/2 + shopBtn.getHeight()/5, 0, Align.center, false);
        squrave.getData().setScale(1f, 1f);
        squrave.draw(sb, Yikes.TITLE, cam.position.x - 200, cam.position.y + cam.viewportHeight/3, 400, Align.center, true);
        squrave.getData().setScale(0.3f + timePassed/10, 0.3f + timePassed/10);
        squrave.draw(sb, "TAP TO PLAY!", cam.position.x - 125, cam.position.y + cam.viewportHeight/20, 250, Align.center, false);
        sb.end();
    }

    @Override
    public void dispose() {
        bg.dispose();
        ground.dispose();
        wall.dispose();
        ballTexture.dispose();
        ball.dispose();
        menuclick.dispose();
        shopBtn.dispose();
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

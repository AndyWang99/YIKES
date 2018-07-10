package com.sodirea.flickeringinthedark.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.utils.Array;
import com.sodirea.flickeringinthedark.FlickeringInTheDark;
import com.sodirea.flickeringinthedark.sprites.Ball;
import com.sodirea.flickeringinthedark.sprites.Platform;

public class PlayState extends State {

    public static final int PLATFORM_INTERVALS = 190;
    public static final int NUM_PLATFORMS = 6;
    private Texture bg;
    private Texture ground;
    private Texture wall1;
    private Texture wall2;
    private Texture ballTexture;
    private Ball ball;
    private Platform platform1;
    private Platform platform2;
    private Platform platform3;
    private Platform platform4;
    private Platform platform5;
    private Platform platform6;
    private Array<Platform> platformArray;
    private float totalTimePassed;
    private boolean isOverlapped;

    public PlayState(GameStateManager gsm) {
        super(gsm);
        cam.setToOrtho(false, FlickeringInTheDark.WIDTH, FlickeringInTheDark.HEIGHT);
        bg = new Texture("bg.png");
        ground = new Texture("ground.png");
        wall1 = new Texture("wall.png");
        wall2 = new Texture("wall.png");
        ballTexture = new Texture("ball.png");
        ball = new Ball(cam.position.x - ballTexture.getWidth() / 2, ground.getHeight());
        platform1 = new Platform(ground.getHeight() + PLATFORM_INTERVALS);
        platform2 = new Platform(ground.getHeight() + 2 * PLATFORM_INTERVALS);
        platform3 = new Platform(ground.getHeight() + 3 * PLATFORM_INTERVALS);
        platform4 = new Platform(ground.getHeight() + 4 * PLATFORM_INTERVALS);
        platform5 = new Platform(ground.getHeight() + 5 * PLATFORM_INTERVALS);
        platform6 = new Platform(ground.getHeight() + 6 * PLATFORM_INTERVALS);
        platformArray = new Array<Platform>();
        platformArray.add(platform1);
        platformArray.add(platform2);
        platformArray.add(platform3);
        platformArray.add(platform4);
        platformArray.add(platform5);
        platformArray.add(platform6);
        totalTimePassed = 0;
    }

    @Override
    protected void handleInput() {
        if (Gdx.input.justTouched()) {
            ball.jump();
        }
        float accelX = Gdx.input.getAccelerometerX();
        if (accelX > 0 && !isOverlapped) {
            ball.moveLeft(accelX * -25);
        }
        if (accelX < 0 && !isOverlapped) {
            ball.moveRight(accelX * -25);
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        ball.update(dt);
        for (Platform platform : platformArray) {
            platform.update(dt);
            if (Intersector.overlaps(ball.getBounds(), platform.getBounds1()) || Intersector.overlaps(ball.getBounds(), platform.getBounds2())) {
                if (ball.getPosition().y + 5 >= platform.getPosition().y + platform.getTexture().getHeight()) { // bottom of ball y position is higher than top of platform y position
                    ball.setNewMinPosition(platform.getPosition().y + platform.getTexture().getHeight());
                    platform.cleared();
                } else if (ball.getPosition().y + ball.getTexture().getHeight() >= platform.getPosition().y) { // top of ball comes in contact with bottom of platform
                    ball.resetVelocityY();
                    isOverlapped = true;
                    while (Intersector.overlaps(ball.getBounds(), platform.getBounds1()) || Intersector.overlaps(ball.getBounds(), platform.getBounds2())) {
                        // this is to simply wait until it is not overlapped with the current platform
                        if (ball.getPosition().y + ball.getTexture().getHeight() > platform.getPosition().y + 15 && ball.getPosition().y + ball.getTexture().getHeight() < platform.getPosition().y + platform.getTexture().getHeight()) { // the ball is in the hole between two platforms, so don't let the ball "noclip" through the platforms
                            ball.moveLeft(0);
                            if (ball.getPosition().x <= platform.getPosition().x) {
                                ball.setPosition(platform.getPosition().x, ball.getPosition().y);
                            } else if (ball.getPosition().x + ball.getTexture().getWidth() >= platform.getPosition().x + Platform.HOLE_WIDTH) {
                                ball.setPosition(platform.getPosition().x + Platform.HOLE_WIDTH - ball.getTexture().getWidth(), ball.getPosition().y);
                            }
                        }

                        if (totalTimePassed < 60) {
                            totalTimePassed += dt;
                        }
                        cam.position.y += 4 * Ball.SCALING_FACTOR * Math.pow(1.02, totalTimePassed);
                        cam.update();
                        if (cam.position.y - cam.viewportHeight / 2 > ball.getPosition().y + ball.getTexture().getHeight()) {
                            gsm.set(new PlayState(gsm));
                        }
                        handleInput();
                        ball.update(dt);
                        platform.update(dt);
                    }
                    isOverlapped = false;
                }
            }

            if (platform.getPosition().y + platform.getTexture().getHeight() < cam.position.y - cam.viewportHeight / 2) {
                platform.reposition();
            }
        }
        if (totalTimePassed < 60) {
            totalTimePassed += dt;
        }
        cam.position.y += 4 * Ball.SCALING_FACTOR * Math.pow(1.02, totalTimePassed);
        cam.update();
        if (cam.position.y - cam.viewportHeight / 2 > ball.getPosition().y + ball.getTexture().getHeight()) {
            gsm.set(new PlayState(gsm));
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(bg, 0, cam.position.y - cam.viewportHeight / 2);
        sb.draw(ground, 0, 0);
        sb.draw(wall1, 0, cam.position.y - cam.viewportHeight / 2);
        sb.draw(wall2, cam.position.x + cam.viewportWidth / 2 - wall2.getWidth(), cam.position.y - cam.viewportHeight / 2);
        if (!isOverlapped) {
            ball.render(sb);
        }
        for (Platform platform : platformArray) {
            platform.render(sb);
        }
        sb.end();
    }

    @Override
    public void dispose() {
        bg.dispose();
        ground.dispose();
        wall1.dispose();
        wall2.dispose();
        ballTexture.dispose();
        ball.dispose();
        platform1.dispose();
        platform2.dispose();
        platform3.dispose();
        platform4.dispose();
        platform5.dispose();
        platform6.dispose();
    }
}

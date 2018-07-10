package com.sodirea.flickeringinthedark.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sodirea.flickeringinthedark.FlickeringInTheDark;
import com.sodirea.flickeringinthedark.sprites.Ball;
import com.sodirea.flickeringinthedark.sprites.Platform;

public class PlayState extends State {

    private Texture bg;
    private Texture ground;
    private Texture wall1;
    private Texture wall2;
    private Texture ballTexture;
    private Ball ball;
    private Platform platform1;
    private float totalTimePassed;

    public PlayState(GameStateManager gsm) {
        super(gsm);
        cam.setToOrtho(false, FlickeringInTheDark.WIDTH, FlickeringInTheDark.HEIGHT);
        bg = new Texture("bg.png");
        ground = new Texture("ground.png");
        wall1 = new Texture("wall.png");
        wall2 = new Texture("wall.png");
        ballTexture = new Texture("ball.png");
        ball = new Ball(cam.position.x - ballTexture.getWidth() / 2, ground.getHeight());
        platform1 = new Platform(ground.getHeight() + ballTexture.getHeight() + 100);
        totalTimePassed = 0;
    }

    @Override
    protected void handleInput() {
        if (Gdx.input.justTouched()) {
            ball.jump();
        }
        float accelX = Gdx.input.getAccelerometerX();
        if (accelX > 0) {
            ball.moveLeft(accelX * -25);
        }
        if (accelX < 0) {
            ball.moveRight(accelX * -25);
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        platform1.update(dt);
        ball.update(dt);
        if (ball.boundsOverlapped(platform1.getBounds1()) || ball.boundsOverlapped(platform1.getBounds2())) {
            if (ball.getPosition().y + 5 >= platform1.getPosition().y + platform1.getTexture().getHeight()) { // bottom of ball y position is higher than top of platform y position
                ball.setNewMinPosition(platform1.getPosition().y + platform1.getTexture().getHeight());
                platform1.cleared();
            } else if (ball.getPosition().y + ball.getTexture().getHeight() >= platform1.getPosition().y) { // top of ball comes in contact with bottom of platform
                ball.resetVelocityY();
                ball.setPosition(ball.getPosition().x, platform1.getPosition().y - ball.getTexture().getHeight() - 1);
            }
        }
        if (totalTimePassed < 120) {
            totalTimePassed += dt;
        }
        //cam.position.y += 4 * Ball.SCALING_FACTOR * Math.pow(1.01, totalTimePassed);
        cam.update();
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(bg, 0, cam.position.y - cam.viewportHeight / 2);
        sb.draw(ground, 0, 0);
        sb.draw(wall1, 0, cam.position.y - cam.viewportHeight / 2);
        sb.draw(wall2, cam.position.x + cam.viewportWidth / 2 - wall2.getWidth(), cam.position.y - cam.viewportHeight / 2);
        ball.render(sb);
        platform1.render(sb);
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
    }
}

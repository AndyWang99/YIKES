package com.sodirea.flickeringinthedark.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sodirea.flickeringinthedark.FlickeringInTheDark;
import com.sodirea.flickeringinthedark.sprites.Ball;

public class PlayState extends State {

    private Texture bg;
    private Texture ground;
    private Texture wall1;
    private Texture wall2;
    private Ball ball;

    public PlayState(GameStateManager gsm) {
        super(gsm);
        cam.setToOrtho(false, FlickeringInTheDark.WIDTH, FlickeringInTheDark.HEIGHT);
        bg = new Texture("bg.png");
        ground = new Texture("ground.png");
        wall1 = new Texture("wall.png");
        wall2 = new Texture("wall.png");
        ball = new Ball(cam.position.x, ground.getHeight());
    }

    @Override
    protected void handleInput() {
        if (Gdx.input.justTouched()) {
            ball.jump();
        }
        float accelX = Gdx.input.getAccelerometerX();
        if (accelX > 1) {
            ball.moveLeft(accelX * -25);
        }
        if (accelX < -1) {
            ball.moveRight(accelX * -25);
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        ball.update(dt);
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(bg, 0, 0);
        sb.draw(ground, 0, 0);
        sb.draw(wall1, 0, 0);
        sb.draw(wall2, cam.position.x + cam.viewportWidth / 2 - wall2.getWidth(), 0);
        ball.render(sb);
        sb.end();
    }

    @Override
    public void dispose() {
        bg.dispose();
        ground.dispose();
        wall1.dispose();
        wall2.dispose();
        ball.dispose();
    }
}

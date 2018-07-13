package com.sodirea.flickeringinthedark.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sodirea.flickeringinthedark.FlickeringInTheDark;
import com.sodirea.flickeringinthedark.states.PlayState;

public class Ball {

    public static final int GRAVITY = -8;
    public static final float SCALING_FACTOR = 0.17f;
    private Texture ground;
    private Texture wall;
    private Texture ball;
    private Vector2 position;
    private Circle bounds;

    public Ball(float x, float y) {
        ground = new Texture("ground.png");
        wall = new Texture("wall.png");
        ball = new Texture("ball.png");
        position = new Vector2(x, y);
        bounds = new Circle(position.x + ball.getWidth() / 2, position.y + ball.getHeight() / 2, ball.getWidth() / 2);
    }

    public Vector2 getPosition() {
        return position;
    }

    public Texture getTexture(){
        return ball;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    public void update(float dt) {
        position.set(position.x, position.y);
        bounds.setPosition(position.x + ball.getWidth() / 2, position.y + ball.getHeight() / 2);
    }

    public void render(SpriteBatch sb) {
        sb.draw(ball, position.x, position.y);
    }

    public void dispose() {
        ground.dispose();
        wall.dispose();
        ball.dispose();
    }
}

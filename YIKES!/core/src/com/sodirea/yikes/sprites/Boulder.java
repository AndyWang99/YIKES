package com.sodirea.yikes.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.sodirea.yikes.Yikes;

import java.util.Random;

import static com.sodirea.yikes.states.PlayState.PIXELS_TO_METERS;

public class Boulder {

    private static final int MIN_VELOCITY = 20;
    private static final int MAX_ADDITIONAL_VELOCITY = 10;
    private Texture wall;
    private Texture boulder;
    private Vector2 position;
    private Circle bounds;
    private BodyDef boulderBodyDef;
    private Body boulderBody;
    private CircleShape boulderCircle;
    private FixtureDef boulderFixtureDef;
    private Fixture boulderFixture;

    public Boulder(float x, float y, World world) {
        wall = new Texture("wall.png");
        boulder = new Texture("boulder.png");
        position = new Vector2(x, y);
        bounds = new Circle(position.x + boulder.getWidth() / 2, position.y + boulder.getHeight() / 2, boulder.getWidth() / 2);
        boulderBodyDef = new BodyDef();
        boulderBodyDef.type = BodyDef.BodyType.DynamicBody;
        boulderBodyDef.position.set((position.x+boulder.getWidth()/2) * PIXELS_TO_METERS, (position.y+boulder.getHeight()/2) * PIXELS_TO_METERS); // convert pixel coordinates to physics boulder coodinates
        boulderBody = world.createBody(boulderBodyDef);
        boulderCircle = new CircleShape();
        boulderCircle.setRadius((boulder.getWidth()/2) * PIXELS_TO_METERS);
        boulderFixtureDef = new FixtureDef();
        boulderFixtureDef.shape = boulderCircle;
        boulderFixtureDef.density = 9999f; // giving it a very high density makes the boulder act like a kinematic body (in that the player's ball adds minimal momentum to the boulder on collisions) that is only affected by gravity
        boulderFixtureDef.friction = 0.0f;
        boulderFixture = boulderBody.createFixture(boulderFixtureDef);
        boulderBody.setUserData(this);
        boulderBody.setLinearVelocity(MIN_VELOCITY + new Random().nextInt(MAX_ADDITIONAL_VELOCITY), 0);
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
        boulderBody.setTransform(new Vector2((position.x+boulder.getWidth()/2) * PIXELS_TO_METERS, (position.y+boulder.getHeight()/2) * PIXELS_TO_METERS), 0);
        boulderBody.setLinearVelocity(MIN_VELOCITY + new Random().nextInt(MAX_ADDITIONAL_VELOCITY), 0);
        bounds.setPosition(position.x + boulder.getWidth() / 2, position.y + boulder.getHeight() / 2);
    }

    public void update(float dt) {
        position.set(boulderBody.getPosition().x/PIXELS_TO_METERS-boulder.getWidth()/2, boulderBody.getPosition().y/PIXELS_TO_METERS-boulder.getHeight()/2); // convert physics ball coordinates back to render/pixel coordinates
        if (position.x <= wall.getWidth()+3) {
            boulderBody.setLinearVelocity(-boulderBody.getLinearVelocity().x, boulderBody.getLinearVelocity().y);
        }
        if (position.x + boulder.getWidth() >= Yikes.WIDTH - wall.getWidth()-3) {
            boulderBody.setLinearVelocity(-boulderBody.getLinearVelocity().x, boulderBody.getLinearVelocity().y);
        }
        bounds.setPosition(position.x + boulder.getWidth() / 2, position.y + boulder.getHeight() / 2);
    }

    public void render(SpriteBatch sb) {
        sb.draw(boulder, position.x, position.y);
    }

    public void dispose() {
        wall.dispose();
        boulder.dispose();
        boulderCircle.dispose();
    }
}


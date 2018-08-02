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
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static com.sodirea.yikes.states.PlayState.PIXELS_TO_METERS;

public class Ball {

    public static final float SCALING_FACTOR = 0.17f;

    private Texture ball;
    private Vector2 position;
    private int numberOfFootContacts;
    private Circle bounds;

    private BodyDef ballBodyDef;
    private Body ballBody;
    private CircleShape ballCircle;
    private FixtureDef ballFixtureDef;
    private Fixture ballFixture;

    private BodyDef footBodyDef;
    private Body footBody;
    private PolygonShape footBox;
    private FixtureDef footFixtureDef;
    private Fixture footFixture;

    // creates a ball object, which consists of a physics body, a foot sensor, and a render body, at the specified coordinates
    public Ball(float x, float y, World world) {
        ball = new Texture("ball.png");
        position = new Vector2(x, y);
        numberOfFootContacts = 0;
        bounds = new Circle(position.x + ball.getWidth() / 2, position.y + ball.getHeight() / 2, ball.getWidth() / 2);

        // creating the ball's physics body
        ballBodyDef = new BodyDef();
        ballBodyDef.type = BodyDef.BodyType.DynamicBody;
        ballBodyDef.position.set((position.x+ball.getWidth()/2) * PIXELS_TO_METERS, (position.y+ball.getHeight()/2) * PIXELS_TO_METERS); // convert render coordinates to physics body coodinates
        ballBody = world.createBody(ballBodyDef);
        ballCircle = new CircleShape();
        ballCircle.setRadius((ball.getWidth()/2) * PIXELS_TO_METERS);
        ballFixtureDef = new FixtureDef();
        ballFixtureDef.shape = ballCircle;
        ballFixtureDef.density = 500f;
        ballFixtureDef.friction = 10f;
        ballFixture = ballBody.createFixture(ballFixtureDef);

        // adding a foot sensor for detecting if the ball could jump / is standing on something. NOTE: a separate body for the sensor is needed. if i only made a foot sensor fixture and attached it to ballBody, then the rotation of the ball would cause the foot sensor to also rotate, meaning the foot sensor would not always be beneath the ball
        footBodyDef = new BodyDef();
        footBodyDef.type = BodyDef.BodyType.DynamicBody;
        footBodyDef.position.set(ballBody.getPosition().x, ballBody.getPosition().y - ballCircle.getRadius() - ballCircle.getRadius() / 8 -  2*PIXELS_TO_METERS);
        footBody = world.createBody(footBodyDef);
        footBox = new PolygonShape();
        footBox.setAsBox(ballCircle.getRadius() / 4, ballCircle.getRadius() / 8);
        footFixtureDef = new FixtureDef();
        footFixtureDef.shape = footBox;
        footFixtureDef.density = 0;
        footFixtureDef.isSensor = true;
        footFixture = footBody.createFixture(footFixtureDef);
        footBody.setFixedRotation(true);
        footBody.setUserData(this);

    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(float x, float y) { // x, y are in rendering coordinates. changes the ballBody position, which then changes the rendering position through the update() method
        ballBody.setTransform(new Vector2((x+ball.getWidth()/2) * PIXELS_TO_METERS, (y+ball.getHeight()/2) * PIXELS_TO_METERS), ballBody.getAngle());
    }

    public Texture getTexture(){
        return ball;
    }

    public Vector2 getBodyLinearVelocity() {
        return ballBody.getLinearVelocity();
    }

    public void setBodyLinearVelocity(float x, float y) {
        ballBody.setLinearVelocity(x, y);
    }

    public void addNumberOfFootContacts() {
         numberOfFootContacts++;
    }

    public void lessNumberOfFootContacts() {
        numberOfFootContacts--;
    }

    public int getNumberOfFootContacts() {
        return numberOfFootContacts;
    }

    public void update(float dt) {
        position.set(ballBody.getPosition().x/PIXELS_TO_METERS-ball.getWidth()/2, ballBody.getPosition().y/PIXELS_TO_METERS-ball.getHeight()/2); // convert physics body coordinates back to render coordinates. this ensures that the rendering position is always in sync with the physics body's position
        footBody.setTransform(new Vector2(ballBody.getPosition().x, ballBody.getPosition().y - ballCircle.getRadius() - ballCircle.getRadius()/8 - 2*PIXELS_TO_METERS), 0); // update the foot sensor body's position to constantly be under the ball's body
        bounds.setPosition(position.x + ball.getWidth() / 2, position.y + ball.getHeight() / 2);
    }

    public void render(SpriteBatch sb) {
        sb.draw(ball, position.x, position.y);
    }

    public void dispose() {
        ball.dispose();
        ballCircle.dispose();
        footBox.dispose();
    }
}

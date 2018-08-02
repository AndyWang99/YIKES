package com.sodirea.yikes.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.utils.Array;
import com.sodirea.yikes.Yikes;
import com.sodirea.yikes.sprites.Ball;
import com.sodirea.yikes.sprites.Boulder;
import com.sodirea.yikes.sprites.Platform;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.sodirea.yikes.states.PlayState.GRAVITY;
import static com.sodirea.yikes.states.PlayState.NUM_PLATFORMS;
import static com.sodirea.yikes.states.PlayState.PIXELS_TO_METERS;
import static com.sodirea.yikes.states.PlayState.PLATFORM_INTERVALS;
import static com.sodirea.yikes.states.PlayState.TIME_STEP;

public class MultiplayerState extends State {

    private final float UPDATE_TIME = 1/20f;
    private float timer;
    private Socket socket;

    private Texture bg;
    private Texture ground;
    private Texture wall;
    private Texture ballTexture;
    private Sound jump;
    private Sound gameover;
    private float totalTimePassed;
    private boolean startCamera;
    private boolean resetState;
    private Vector2 lastSentPosition;
    private float displacementFromLastSentPosition;

    private Array<Platform> platformArray;
    private Array<Vector2> platformPositionArray;
    private Array<Integer> platformWidthArray;
    private boolean needsPlatforms;
    private int toRepositionIndex;
    private boolean giveServerPositionCoordinates;

    private Array<Boulder> boulderArray;
    private Array<Float> boulderVelocityArray;
    private Array<Vector2> boulderPositionArray;
    private Random boulderGenerator;
    private boolean needsBoulders;
    private boolean toRepositionBoulder;

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

    private Ball player;
    private boolean playerIsDead;
    private boolean playerConnected;
    private HashMap<String, Ball> otherPlayers;
    private HashMap<String, Vector2> otherPlayersPosition;
    private HashMap<String, Vector2> otherPlayersVelocity;

    public MultiplayerState(GameStateManager gsm) {
        super(gsm);
        bg = new Texture("bg.png");
        ground = new Texture("ground.png");
        wall = new Texture("wall.png");
        ballTexture = new Texture("ball.png");
        jump = Gdx.audio.newSound(Gdx.files.internal("jump.mp3"));
        gameover = Gdx.audio.newSound(Gdx.files.internal("gameover.wav"));

        resetState = false;
        displacementFromLastSentPosition = 0f;
        lastSentPosition = new Vector2(cam.position.x - ballTexture.getWidth() / 2, ground.getHeight());
        playerIsDead = false;
        totalTimePassed = 0;
        startCamera = false;

        platformArray = new Array<Platform>();
        platformPositionArray = new Array<Vector2>();
        platformWidthArray = new Array<Integer>();
        needsPlatforms = true;
        toRepositionIndex = -1; // if it is not -1, then a platform needs to be repositioned
        giveServerPositionCoordinates = false;

        boulderArray = new Array<Boulder>();
        boulderVelocityArray = new Array<Float>();
        boulderPositionArray = new Array<Vector2>();
        boulderGenerator = new Random();
        needsBoulders = true;
        toRepositionBoulder = false;
        cam.setToOrtho(false, Yikes.WIDTH, Yikes.HEIGHT);

        Box2D.init();
        world = new World(new Vector2(0, GRAVITY), true);
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                if (contact.getFixtureA().getBody() != null && contact.getFixtureB().getBody().getUserData() != null) {
                    // if the foot sensor fixture of the ball touches a platform, and the lowest point of the ball is higher than the highest point of the touched platform, then clear the platform
                    if (contact.getFixtureA().getBody().getUserData() == player || contact.getFixtureB().getBody().getUserData() == player) {
                        player.addNumberOfFootContacts(); // add to the total number of contact points
                        Platform platform = null;
                        if (contact.getFixtureA().getBody().getUserData() instanceof Platform) {
                            platform = (Platform) contact.getFixtureA().getBody().getUserData();
                        } else if (contact.getFixtureB().getBody().getUserData() instanceof Platform) {
                            platform = (Platform) contact.getFixtureB().getBody().getUserData();
                        }
                        if (platform != null && player.getPosition().y > platform.getPosition().y + platform.getTexture().getHeight() && !platform.getIsCleared()) {
                            if (!startCamera) {
                                startCamera = true;
                                socket.emit("startCamera"); // make camera start for every player
                                giveServerPositionCoordinates = true; // the first player to step on a platform will give the server coordinates to determine when to reposition platforms
                            }
                        }
                    }
                }
            }

            @Override
            public void endContact(Contact contact) {
                if (contact.getFixtureA().getBody() != null && contact.getFixtureB().getBody().getUserData() != null) {
                    if (contact.getFixtureA().getBody().getUserData() == player || contact.getFixtureB().getBody().getUserData() == player) {
                        player.lessNumberOfFootContacts();
                    }
                }
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });

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

        playerConnected = false;
        otherPlayers = new HashMap<String, Ball>();
        otherPlayersPosition = new HashMap<String, Vector2>();
        otherPlayersVelocity = new HashMap<String, Vector2>();

        connectSocket();
        configSocketEvents();
    }

    @Override
    protected void handleInput() {
        if (!playerIsDead) {
            // if there is more than one contact point, and the player touches the screen, then jump
            if (Gdx.input.justTouched() && player != null && player.getNumberOfFootContacts() > 0) {
                jump.play(1f);
                player.setBodyLinearVelocity(player.getBodyLinearVelocity().x, 50f);
            }
            // controlling the player's horizontal movement by tilting the screen
            float accelX = Gdx.input.getAccelerometerX();
            if (accelX > 0 && player != null) {
                player.setBodyLinearVelocity(-20f * accelX, player.getBodyLinearVelocity().y);
            }
            if (accelX < 0 && player != null) {
                player.setBodyLinearVelocity(-20f * accelX, player.getBodyLinearVelocity().y);
            }
        }
    }

    @Override
    public void update(float dt) {
        if (resetState) {
            gsm.set(new MenuState(gsm));
        }
        timer += dt;
        // if the timer exceeds UPDATE_TIME, then communicate with the server
        if (timer >= UPDATE_TIME && player != null) {
            timer = 0;
            // update the displacement variable by taking the client's current position and comparing it with the server's position of the player
            if (player != null) {
                displacementFromLastSentPosition = (float) Math.sqrt(Math.pow(player.getPosition().x - lastSentPosition.x, 2) + Math.pow(player.getPosition().y - lastSentPosition.y, 2));
            }
            // if the displacement exceeds 3f, then send the player's current position and velocity to the server
            if (displacementFromLastSentPosition >= 3f && player != null) {
                displacementFromLastSentPosition = 0;
                lastSentPosition.x = player.getPosition().x;
                lastSentPosition.y = player.getPosition().y;
                JSONObject data = new JSONObject();
                try {
                    data.put("x", player.getPosition().x);
                    data.put("y", player.getPosition().y);
                    data.put("velocityX", player.getBodyLinearVelocity().x);
                    data.put("velocityY", player.getBodyLinearVelocity().y);
                    socket.emit("playerUpdate", data);
                } catch (JSONException e) {
                    Gdx.app.log("SOCKET.IO", "Error sending update data");
                }
            }

            for (HashMap.Entry<String, Vector2> entry : otherPlayersPosition.entrySet()) { // updating the ball hashmap with the position and velocity hashmaps
                String id = entry.getKey();
                Vector2 position = otherPlayersPosition.get(id);
                Vector2 velocity = otherPlayersVelocity.get(id);
                if (!otherPlayers.containsKey(entry.getKey())) { // found an id in position array that isn't in the original array, so put that new id in
                    Ball otherPlayer = new Ball(position.x, position.y, world);
                    otherPlayer.setBodyLinearVelocity(velocity.x, velocity.y);
                    otherPlayers.put(id, otherPlayer);
                }
            }

            // adding platforms to a client with an empty platformArray by sending the positions and width of platforms form the server
            if (needsPlatforms) {
                for (int i = 0; i < platformPositionArray.size; i++) {
                    Vector2 position = platformPositionArray.get(i);
                    Integer width = platformWidthArray.get(i);
                    platformArray.add(new Platform(position.x, position.y, width, world));
                }
                if (platformArray.size != 0) { // confirm that platforms have been added
                    needsPlatforms = false;
                }
            }
            if (needsBoulders) {
                for (int i = 0; i < boulderPositionArray.size; i++) {
                    Vector2 position = boulderPositionArray.get(i);
                    Float velocity = boulderVelocityArray.get(i);
                    boulderArray.add(new Boulder(velocity, position.x, position.y, world));
                }
                if (boulderArray.size != 0) {
                    needsBoulders = false;
                }
            }
        }
        if (toRepositionIndex != -1) { // if it is not -1, then reposition the platform and boulder at the specified index
            platformArray.get(toRepositionIndex).reposition(platformPositionArray.get(toRepositionIndex).x, platformPositionArray.get(toRepositionIndex).y, platformWidthArray.get(toRepositionIndex));
            if (toRepositionBoulder) {
                boulderArray.get(toRepositionIndex).reposition(boulderPositionArray.get(toRepositionIndex).x, boulderPositionArray.get(toRepositionIndex).y, boulderVelocityArray.get(toRepositionIndex));
                toRepositionBoulder = false;
            }
            toRepositionIndex = -1;
        }

        handleInput();
        if (playerConnected && player == null) {
            player = new Ball(cam.position.x - ballTexture.getWidth() / 2, ground.getHeight(), world);
        }

        for (int i = 0; i < platformArray.size; i++) {
            Platform platform = platformArray.get(i);
            platform.update(dt);
            if (giveServerPositionCoordinates) { // if our representative needs to reposition a platform, all other players also reposition
                if (platform.getPosition().y + platform.getTexture().getHeight() < cam.position.y - cam.viewportHeight / 2) {
                    platform.reposition(platform.getPosition().y + PLATFORM_INTERVALS * NUM_PLATFORMS);
                    // serializing the repositioned platform's position and width to send to other clients
                    JSONObject repositionedPlatform = new JSONObject();
                    try {
                        repositionedPlatform.put("index", i);
                        repositionedPlatform.put("x", platform.getPosition().x);
                        repositionedPlatform.put("y", platform.getPosition().y);
                        repositionedPlatform.put("width", platform.getHoleWidth());
                        socket.emit("repositionPlatform", repositionedPlatform);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (boulderGenerator.nextBoolean()) {
                        boulderArray.get(i).reposition(platform.getPosition().x, platform.getPosition().y + platform.getTexture().getHeight());
                        JSONObject repositionedBoulder = new JSONObject();
                        try {
                            repositionedBoulder.put("index", i);
                            repositionedBoulder.put("x", boulderArray.get(i).getPosition().x);
                            repositionedBoulder.put("y", boulderArray.get(i).getPosition().y);
                            repositionedBoulder.put("velocity", boulderArray.get(i).getBodyLinearVelocityX());
                            socket.emit("repositionBoulder", repositionedBoulder);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        for (Boulder boulder: boulderArray) {
            boulder.update(dt);
        }

        if (player != null) {
            player.update(dt);
        }
        for (HashMap.Entry<String, Ball> entry : otherPlayers.entrySet()) {
            entry.getValue().update(dt);
        }

        // if startCamera is true, then start scrolling upwards while scaling the scrolling speed with time
        if (startCamera) {
            if (totalTimePassed < 60) {
                totalTimePassed += dt;
            }
            cam.position.y += 4 * Ball.SCALING_FACTOR * (Math.pow(1.02, totalTimePassed) + 2);
            cam.update();
        }

        wallBody.setTransform(new Vector2(wallBody.getPosition().x, cam.position.y * PIXELS_TO_METERS), wallBody.getAngle());
        wallBody2.setTransform(new Vector2(wallBody2.getPosition().x, cam.position.y * PIXELS_TO_METERS), wallBody2.getAngle());

        if (player != null && !playerIsDead && cam.position.y - cam.viewportHeight / 2 > player.getPosition().y + player.getTexture().getHeight()) {
            gameover.play(1f);
            playerIsDead = true;
            socket.emit("addToDeathCounter");
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
        if (player != null) {
            player.render(sb);
        }
        for (HashMap.Entry<String, Ball> entry : otherPlayers.entrySet()) {
            entry.getValue().render(sb);
        }
        for (Platform platform : platformArray) {
            platform.render(sb);
        }
        for (Boulder boulder : boulderArray) {
            boulder.render(sb);
        }
        sb.end();
    }

    @Override
    public void dispose() {
        bg.dispose();
        ground.dispose();
        wall.dispose();
        player.dispose();
        ballTexture.dispose();
        jump.dispose();
        gameover.dispose();
        for (HashMap.Entry<String, Ball> entry : otherPlayers.entrySet()) {
            entry.getValue().dispose();
        }
    }

    public void connectSocket() {
        try {
            socket = IO.socket("https://blooming-reef-86477.herokuapp.com");
            socket.connect();
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public void configSocketEvents() {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Gdx.app.log("SocketIO", "Connected");
                playerConnected = true;
            }
        }).on("socketID", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String id = data.getString("id");
                    Gdx.app.log("SocketIO", "My ID: " + id);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting ID");
                }
            }
        }).on("newPlayer", new Emitter.Listener() {
            @Override
            public void call(Object... args) { // when another client connects, put their position and velocity into the array
                JSONObject data = (JSONObject) args[0];
                try {
                    String id = data.getString("id");
                    Gdx.app.log("SocketIO", "New Player Connected: " + id);
                    otherPlayersPosition.put(id, new Vector2(200, 200));
                    otherPlayersVelocity.put(id, new Vector2(0, 0));
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting new Player ID");
                }
            }
        }).on("playerDisconnected", new Emitter.Listener() {
            @Override
            public void call(Object... args) { // when another client disconnects, remove their values from the arrays
                JSONObject data = (JSONObject) args[0];
                try {
                    String id = data.getString("id");
                    otherPlayers.remove(id);
                    otherPlayersPosition.remove(id);
                    otherPlayersVelocity.remove(id);
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting disconnected Player ID");
                }
            }
        }).on("getPlayers", new Emitter.Listener() {
            @Override
            public void call(Object... args) { // when the player connects to the server, get the positions and velocities of other players
                JSONArray objects = (JSONArray) args[0];
                try {
                    for (int i = 0; i < objects.length(); i++) {
                        Vector2 position = new Vector2();
                        position.x = ((Double) objects.getJSONObject(i).getDouble("x")).floatValue();
                        position.y = ((Double) objects.getJSONObject(i).getDouble("y")).floatValue();
                        otherPlayersPosition.put(objects.getJSONObject(i).getString("id"), position);
                        otherPlayersVelocity.put(objects.getJSONObject(i).getString("id"), new Vector2(0, 0));
                    }
                } catch(JSONException e) {

                }
            }
        }).on("playerUpdate", new Emitter.Listener() {
            @Override
            public void call(Object... args) { // when another player sends their position to the server, every other player receives the new position and updates their client accordingly
                JSONObject data = (JSONObject) args[0];
                try {
                    String id = data.getString("id");
                    Double x = data.getDouble("x");
                    Double y = data.getDouble("y");
                    Double velocityX = data.getDouble("velocityX");
                    Double velocityY = data.getDouble("velocityY");
                    if (otherPlayersPosition.get(id) != null) {
                        otherPlayersPosition.put(id, new Vector2(x.floatValue(), y.floatValue()));
                    }
                    if (otherPlayersVelocity.get(id) != null) {
                        otherPlayersVelocity.put(id, new Vector2(velocityX.floatValue(), velocityY.floatValue()));
                    }
                    Vector2 position = otherPlayersPosition.get(id);
                    Vector2 velocity = otherPlayersVelocity.get(id);
                    if (otherPlayers.containsKey(id)) { // if it already contains the id, then just update its ball's position
                        Ball otherPlayer = otherPlayers.get(id);
                        otherPlayer.setPosition(position.x, position.y);
                        otherPlayer.setBodyLinearVelocity(velocity.x, velocity.y);
                    }
                } catch (JSONException e) {
                    Gdx.app.log("SocketIO", "Error getting disconnected Player ID");
                }
            }
        }).on("getPlatforms", new Emitter.Listener() {
            @Override
            public void call(Object... args) { // when the player connects, get the positions and widths of each platform stored on the server
                JSONArray objects = (JSONArray) args[0];
                try {
                    for (int i = 0; i < objects.length(); i++) {
                        Vector2 position = new Vector2();
                        position.x = ((Double) objects.getJSONObject(i).getDouble("x")).floatValue();
                        position.y = ((Double) objects.getJSONObject(i).getDouble("y")).floatValue();
                        Integer width = new Integer(objects.getJSONObject(i).getInt("width"));
                        platformWidthArray.add(width);
                        platformPositionArray.add(position);
                    }
                } catch (JSONException e) {

                }
            }
        }).on("startCamera", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    startCamera = ((JSONObject) args[0]).getBoolean("start");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("repositionPlatform", new Emitter.Listener() {
            @Override
            public void call(Object... args) { // the server sends a repositioned platform's position and width to all clients for them to update
                JSONObject repositionedPlatform = (JSONObject) args[0];
                try {
                    int i = repositionedPlatform.getInt("index");
                    float x = ((Double) repositionedPlatform.getDouble("x")).floatValue();
                    float y = ((Double) repositionedPlatform.getDouble("y")).floatValue();
                    int width = repositionedPlatform.getInt("width");
                    platformPositionArray.set(i, new Vector2(x, y));
                    platformWidthArray.set(i, width);
                    toRepositionIndex = i;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("getBoulders", new Emitter.Listener() {
            @Override
            public void call(Object... args) { // when the player connects, get the positions and velocity of each boulder stored on the server
                JSONArray objects = (JSONArray) args[0];
                try {
                    for (int i = 0; i < objects.length(); i++) {
                        Vector2 position = new Vector2();
                        position.x = ((Double) objects.getJSONObject(i).getDouble("x")).floatValue();
                        position.y = ((Double) objects.getJSONObject(i).getDouble("y")).floatValue();
                        Float velocity = new Float(objects.getJSONObject(i).getInt("velocity"));
                        boulderVelocityArray.add(velocity);
                        boulderPositionArray.add(position);
                    }
                } catch (JSONException e) {

                }
            }
        }).on("repositionBoulder", new Emitter.Listener() {
            @Override
            public void call(Object... args) { // the server sends a repositioned boulder's position and velocity to all clients for them to update
                JSONObject repositionedBoulder = (JSONObject) args[0];
                try {
                    int i = repositionedBoulder.getInt("index");
                    float x = ((Double) repositionedBoulder.getDouble("x")).floatValue();
                    float y = ((Double) repositionedBoulder.getDouble("y")).floatValue();
                    float velocity = ((Double) repositionedBoulder.getDouble("velocity")).floatValue();
                    boulderPositionArray.set(i, new Vector2(x, y));
                    boulderVelocityArray.set(i, velocity);
                    toRepositionBoulder = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("resetState", new Emitter.Listener() {
            @Override
            public void call(Object... args) { // when all players are dead, bring them back to the menu state, and disconnect them from the server
                socket.disconnect();
                resetState = true;
            }
        });
    }
}

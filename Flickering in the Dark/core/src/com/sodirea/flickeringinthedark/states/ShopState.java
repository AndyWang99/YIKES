package com.sodirea.flickeringinthedark.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.sodirea.flickeringinthedark.FlickeringInTheDark;

public class ShopState extends State {

    private Image bg;
    private Array<ImageTextButton> unlocksArray;
    private ImageTextButton doubleJump;
    private Stage stage;
    private Preferences prefs;
    private BitmapFont squrave;
    private ScrollPane scrollPane;
    private Table btnTable;

    protected ShopState(GameStateManager gsm) {
        super(gsm);
        cam.setToOrtho(false, FlickeringInTheDark.WIDTH, FlickeringInTheDark.HEIGHT);
        bg = new Image(new TextureRegionDrawable(new TextureRegion(new Texture("bg.png"))));
        unlocksArray = new Array<ImageTextButton>();
        prefs = Gdx.app.getPreferences("Prefs");
        squrave = new BitmapFont(Gdx.files.internal("squrave.fnt"), false);
        stage = new Stage(new StretchViewport(cam.viewportWidth, cam.viewportHeight));
        Gdx.input.setInputProcessor(stage);
        btnTable = new Table();
        btnTable.align(Align.topLeft);
        scrollPane = new ScrollPane(btnTable);
        scrollPane.setBounds(cam.position.x - cam.viewportWidth / 2 + 50, cam.position.y - cam.viewportWidth / 2  - cam.viewportWidth / 5, cam.viewportWidth - 100, cam.viewportHeight/2+cam.viewportHeight/4);

        stage.addActor(bg);
        stage.addActor(scrollPane);

        squrave.getData().setScale(0.3f, 0.3f);
        Drawable btnBg = new TextureRegionDrawable(new TextureRegion(new Texture("unlockbtns.png")));
        ImageTextButton.ImageTextButtonStyle style = new ImageTextButton.ImageTextButtonStyle(btnBg, btnBg, btnBg, squrave);

        doubleJump = new ImageTextButton("DOUBLE JUMP", style);
        initUnlock(doubleJump, "DOUBLE JUMP");

        for (ImageTextButton btn : unlocksArray) {
            if (prefs.getInteger("highscore", 0) >= prefs.getInteger(btn.getName() + " Score Requirements")) {
                prefs.putBoolean(btn.getName(), true);
                prefs.flush();
                if (prefs.getBoolean(btn.getName() + " Toggle", false)) {
                    btn.setText(btn.getName() + " - ON");
                } else {
                    btn.setText(btn.getName() + " - OFF");
                }
            }
            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Actor target = event.getTarget();
                    Label label;
                    String key = target.getName();

                    if (target instanceof ImageTextButton) {
                        label = ((ImageTextButton) target).getLabel();
                    } else {
                        label = (Label) target;
                    }

                    if (prefs.getBoolean(key, false)) {
                        if (prefs.getBoolean(key + " Toggle", false)) {
                            prefs.putBoolean(key + " Toggle", false);
                            label.setText(key + " - OFF");
                        } else if (!prefs.getBoolean(key + " Toggle", false)) {
                            prefs.putBoolean(key + " Toggle", true);
                            label.setText(key + " - ON");
                        }
                        prefs.flush();
                    }
                }
            });
        }
    }

    @Override
    protected void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            cam.unproject(mousePos);
            if (mousePos.x < 100 && mousePos.y > stage.getHeight() - 100) {
                gsm.set(new MenuState(gsm));
            }
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        for (ImageTextButton btn : unlocksArray) {
            if (!prefs.getBoolean(btn.getName(), false)) {
                if (!btn.isPressed()) {
                    btn.setText(btn.getName() + " - LOCKED");
                } else {
                    btn.setText("GET OVER " + prefs.getInteger(btn.getName() + " Score Requirements") + " SCORE");
                }
            }
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setProjectionMatrix(cam.combined);
        stage.draw();
        sb.begin();
        squrave.getData().setScale(1f, 1f);
        squrave.draw(sb, "SHOP", cam.position.x, cam.position.y + cam.viewportHeight/2 - cam.viewportHeight/25, 0, Align.center, false);
        squrave.getData().setScale(0.3f, 0.3f);
        squrave.draw(sb, "BACK", cam.position.x - cam.viewportWidth/2, cam.position.y + cam.viewportHeight/2 - cam.viewportHeight/25, 0, Align.left, false);
        sb.end();
    }

    @Override
    public void dispose() {
        stage.dispose();
        squrave.dispose();
    }

    public void initUnlock(ImageTextButton btn, String key) {
        btn.setName(key);
        btn.getLabel().setName(key);
        if (!prefs.getBoolean(key, false)) {
            btn.setText(btn.getName() + " - LOCKED");
        } else {
            if (prefs.getBoolean(key + " Toggle", false)) {
                btn.setText(btn.getName() + " - ON");
            } else {
                btn.setText(btn.getName() + " - OFF");
            }
        }
        unlocksArray.add(btn);
        btnTable.add(btn);
    }
}

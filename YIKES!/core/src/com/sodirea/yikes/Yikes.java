package com.sodirea.yikes;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sodirea.yikes.states.GameStateManager;
import com.sodirea.yikes.states.MenuState;
import com.sodirea.yikes.states.MultiplayerState;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Yikes extends ApplicationAdapter {

	public static final int WIDTH = 480;
	public static final int HEIGHT = 800;
	public static final String TITLE = "YIKES!";
	private SpriteBatch sb;
	private GameStateManager gsm;
	private Preferences prefs;

	@Override
	public void create () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		sb = new SpriteBatch();
		gsm = new GameStateManager();
		//gsm.push(new MenuState(gsm));
		gsm.push(new MultiplayerState(gsm));

		prefs = Gdx.app.getPreferences("Prefs");
		prefs.putInteger("DOUBLE JUMP Score Requirements", 50);
		prefs.flush();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		gsm.update(Gdx.graphics.getDeltaTime());
		gsm.render(sb);
	}
	
	@Override
	public void dispose () {
		sb.dispose();
	}
}

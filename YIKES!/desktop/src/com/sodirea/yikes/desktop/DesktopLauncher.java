package com.sodirea.yikes.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.sodirea.yikes.Yikes;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new Yikes(), config);
		config.width = Yikes.WIDTH;
		config.height = Yikes.HEIGHT;
		config.title = Yikes.TITLE;
	}
}

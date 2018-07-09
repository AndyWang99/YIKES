package com.sodirea.flickeringinthedark.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.sodirea.flickeringinthedark.FlickeringInTheDark;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new FlickeringInTheDark(), config);
		config.width = FlickeringInTheDark.WIDTH;
		config.height = FlickeringInTheDark.HEIGHT;
		config.title = FlickeringInTheDark.TITLE;
	}
}

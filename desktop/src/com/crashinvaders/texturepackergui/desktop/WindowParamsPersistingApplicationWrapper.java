package com.crashinvaders.texturepackergui.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglPreferences;
import com.badlogic.gdx.files.FileHandle;
import com.crashinvaders.texturepackergui.App;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.opengl.Display;

/**
 * Saves and loads window position and size across application shutdowns
 */
class WindowParamsPersistingApplicationWrapper extends ApplicationListenerWrapper {
    public WindowParamsPersistingApplicationWrapper(App app, LwjglApplicationConfiguration configuration) {
        super(app);
        loadWindowParams(configuration);
    }

    @Override
    public void dispose() {
        saveWindowParams();
        super.dispose();
    }

    private void loadWindowParams(LwjglApplicationConfiguration configuration) {
        FileHandle file = new FileHandle(LwjglFiles.externalPath + configuration.preferencesDirectory + "/window_params.xml");
        if (!file.exists()) return;

        Preferences prefs = new LwjglPreferences(file);

        configuration.x = prefs.getInteger("x", configuration.x);
        configuration.y = prefs.getInteger("y", configuration.y);
        configuration.width = prefs.getInteger("width", configuration.width);
        configuration.height = prefs.getInteger("height", configuration.height);
    }

    private void saveWindowParams() {
        int width = Display.getWidth();
        int height = Display.getHeight();
        int x = Display.getX();
        int y = Display.getY();

        //FIXME by some reason actual window position shifted by 6 pixels on Windows (by 12 at y when maximized)
        if (LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_WINDOWS) {
            x += 6;
            y += 6;
        }

        Preferences prefs = Gdx.app.getPreferences("window_params.xml");
        prefs.putInteger("x", x);
        prefs.putInteger("y", y);
        prefs.putInteger("width", width);
        prefs.putInteger("height", height);
        prefs.flush();
    }
}

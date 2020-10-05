package com.crashinvaders.texturepackergui.desktoplwjgl3.launchers.glsurface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglPreferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.desktoplwjgl3.ApplicationListenerWrapper;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.opengl.Display;

import java.awt.*;

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

        DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        int screenWidth = displayMode.getWidth();
        int screenHeight = displayMode.getHeight();

        Preferences prefs = new LwjglPreferences(file);
        configuration.width = MathUtils.clamp(prefs.getInteger("width", configuration.width), 320, screenWidth);
        configuration.height = MathUtils.clamp(prefs.getInteger("height", configuration.height), 320, screenHeight);
        configuration.x = MathUtils.clamp(prefs.getInteger("x", configuration.x), 0, screenWidth - configuration.width);
        configuration.y = MathUtils.clamp(prefs.getInteger("y", configuration.y), 0, screenHeight - configuration.height);
    }

    private void saveWindowParams() {
        int width = Display.getWidth();
        int height = Display.getHeight();
        int x = Display.getX();
        int y = Display.getY();

        //FIXME For some reason actual window position shifted by 6 pixels on Windows (by 12 at y when maximized).
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

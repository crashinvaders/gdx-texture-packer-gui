package com.crashinvaders.texturepackergui.desktoplwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.crashinvaders.texturepackergui.App;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.nio.IntBuffer;

/**
 * Saves/loads window position and size on startup/shutdown respectively.
 */
class WindowParamsPersistingApplicationWrapper extends ApplicationListenerWrapper {
    private static final IntBuffer tmpBuffer0 = BufferUtils.createIntBuffer(1);
    private static final IntBuffer tmpBuffer1 = BufferUtils.createIntBuffer(1);

    public WindowParamsPersistingApplicationWrapper(App app, Lwjgl3ApplicationConfigurationX configuration) {
        super(app);
        loadWindowParams(configuration);
    }

    @Override
    public void dispose() {
        saveWindowParams();
        super.dispose();
    }

    //NOTE Do not use any GDX related instances inside this method, as the GDX application is not initialized yet.
    private void loadWindowParams(Lwjgl3ApplicationConfigurationX configuration) {
        FileHandle file = new FileHandle(Lwjgl3Files.externalPath + configuration.getPreferencesDirectory() + "/window_params.xml");
        if (!file.exists()) return;

        DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        int screenWidth = displayMode.getWidth();
        int screenHeight = displayMode.getHeight();

        Preferences prefs = new Lwjgl3Preferences(file);
        configuration.setWindowedMode(
                MathUtils.clamp(prefs.getInteger("width", configuration.getWindowWidth()), 320, screenWidth),
                MathUtils.clamp(prefs.getInteger("height", configuration.getWindowHeight()), 320, screenHeight));
        configuration.setWindowPosition(
                MathUtils.clamp(prefs.getInteger("x", configuration.getWindowX()), 0, screenWidth - configuration.getWindowWidth()),
                MathUtils.clamp(prefs.getInteger("y", configuration.getWindowY()), 0, screenHeight - configuration.getWindowHeight()));
    }

    private void saveWindowParams() {
        Lwjgl3Window window = ((Lwjgl3Graphics) Gdx.graphics).getWindow();
        int x = window.getPositionX();
        int y = window.getPositionY();
        int width = getWindowWidth(window);
        int height = getWindowHeight(window);

        Preferences prefs = Gdx.app.getPreferences("window_params.xml");
        prefs.putInteger("x", x);
        prefs.putInteger("y", y);
        prefs.putInteger("width", width);
        prefs.putInteger("height", height);
        prefs.flush();
    }

    private static int getWindowWidth(Lwjgl3Window window) {
        GLFW.glfwGetWindowSize(window.getWindowHandle(), tmpBuffer0, tmpBuffer1);
        return tmpBuffer0.get(0);
    }

    private static int getWindowHeight(Lwjgl3Window window) {
        GLFW.glfwGetWindowSize(window.getWindowHandle(), tmpBuffer0, tmpBuffer1);
        return tmpBuffer1.get(0);
    }
}

package com.crashinvaders.texturepackergui.desktoplwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.DragDropManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;

import java.awt.*;
import java.nio.IntBuffer;

/**
 * Handles any specific desktop oriented functionality for the {@link App}.
 * <ol>
 * <li>Saves/loads window position and size on startup/shutdown respectively.</li>
 * <li>Set up file drag-n-drop handling.</li>
 * </ol>
 */
class Lwjgl3AppWrapper extends ApplicationListenerWrapper {
    private static final IntBuffer tmpBuffer0 = BufferUtils.createIntBuffer(1);
    private static final IntBuffer tmpBuffer1 = BufferUtils.createIntBuffer(1);

    public Lwjgl3AppWrapper(App app, Lwjgl3ApplicationConfigurationX configuration) {
        super(app);
        loadWindowParams(configuration);
    }

    @Override
    public void create() {
        super.create();

        // Register drag-n-drop handler.
        long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        GLFW.glfwSetDropCallback(windowHandle, (window, count, names) -> {
            Array<FileHandle> files = new Array<>(count);
            for (int i = 0; i < count; i++) {
                FileHandle fileHandle = new FileHandle(GLFWDropCallback.getName(names, i));
                files.add(fileHandle);
            }

            final int mouseX = Gdx.input.getX();
            final int mouseY = Gdx.input.getY();

            //TODO Implement another drag-n-drop overlay that doesn't need mouse dragging events.
            DragDropManager dragDropManager = App.inst().getDragDropManager();
            dragDropManager.onDragStarted(mouseX, mouseY);
            new Timer().scheduleTask(new Timer.Task() {
                @Override
                public void run() {
                    dragDropManager.handleFileDrop(mouseX, mouseY, files);
                    dragDropManager.onDragFinished();
                }
            }, 0.5f);
        });
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

package com.crashinvaders.texturepackergui.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.DragDropManager;
import com.crashinvaders.texturepackergui.controllers.GlobalActions;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;

import java.nio.IntBuffer;

/**
 * Handles any desktop specific functionality for the {@link App}.
 * <ol>
 * <li>Saves/loads window position and size on startup/shutdown respectively.</li>
 * <li>Set up file drag-n-drop handling.</li>
 * <li>Check if there are any unsaved project changes on window close event.</li>
 * <li>Some extra info logging.</li>
 * </ol>
 */
class Lwjgl3AppWrapper extends ApplicationListenerWrapper {
    private static final IntBuffer tmpBuffer0 = BufferUtils.createIntBuffer(1);
    private static final IntBuffer tmpBuffer1 = BufferUtils.createIntBuffer(1);
    private static final IntBuffer tmpBuffer2 = BufferUtils.createIntBuffer(1);
    private static final IntBuffer tmpBuffer3 = BufferUtils.createIntBuffer(1);

    public Lwjgl3AppWrapper(App app, Lwjgl3ApplicationConfigurationX configuration) {
        super(app);
        loadWindowParams(configuration);
    }

    @Override
    public void create() {
        LoggerUtils.printGpuInfo();

        super.create();

        Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
        Lwjgl3Window window = graphics.getWindow();

        // Validate and fix window dimensions (in case it's outside the display).
        if (!graphics.isFullscreen())
        {
            long monitorHandle = getCurrentMonitorHandle(graphics);
            IntRectangle monitorArea = getMonitorWorkarea(monitorHandle);

            if (monitorArea.width != 0 && monitorArea.height != 0) {
                final int minWidth = Math.min(320, monitorArea.width);
                final int minHeight = Math.min(320, monitorArea.height);

                int windowX = MathUtils.clamp(window.getPositionX(), monitorArea.x, monitorArea.x + monitorArea.width - minWidth);
                int windowY = MathUtils.clamp(window.getPositionY(), monitorArea.y, monitorArea.y + monitorArea.height - minHeight);
                int windowWidth = MathUtils.clamp(getWindowWidth(window), minWidth, monitorArea.width);
                int windowHeight = MathUtils.clamp(getWindowHeight(window), minHeight, monitorArea.height);

                long windowHandle = window.getWindowHandle();
                GLFW.glfwSetWindowPos(windowHandle, windowX, windowY);
                GLFW.glfwSetWindowSize(windowHandle, windowWidth, windowHeight);
                GLFW.glfwSetWindowSizeLimits(windowHandle, minWidth, minHeight, GLFW.GLFW_DONT_CARE, GLFW.GLFW_DONT_CARE);
            }
        }

        // Check for unsaved changes on window close attempt.
        window.setWindowListener(new Lwjgl3WindowAdapter() {
            boolean safeToCloseWindow = false;
            boolean closeHandling = false;

            @Override
            public boolean closeRequested() {
                if (safeToCloseWindow) return true;
                if (closeHandling) return false;

                closeHandling = true;

                GlobalActions globalActions = (GlobalActions) App.inst().getContext().getComponent(GlobalActions.class);
                globalActions.commonDialogs.checkUnsavedChanges(
                        () -> {
                            safeToCloseWindow = true;
                            closeHandling = false;
                            Gdx.app.exit();

                        }, () -> closeHandling = false);

                // Never close the window instantly.
                // Only if there are no unsaved changes or the user has confirmed it explicitly.
                return false;
            }
        });

        // Register drag-n-drop handler.
        GLFW.glfwSetDropCallback(window.getWindowHandle(), (wnd, count, names) -> {
            Array<FileHandle> files = new Array<>(count);
            for (int i = 0; i < count; i++) {
                FileHandle fileHandle = new FileHandle(GLFWDropCallback.getName(names, i));
                files.add(fileHandle);
            }

            final int mouseX = Gdx.input.getX();
            final int mouseY = Gdx.input.getY();

            Gdx.app.postRunnable(() -> {
                DragDropManager dragDropManager = App.inst().getDragDropManager();
                dragDropManager.handleFileDrop(mouseX, mouseY, files);
            });
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

        Preferences prefs = new Lwjgl3Preferences(file);
        configuration.setWindowedMode(
                Math.max(prefs.getInteger("width", configuration.getWindowWidth()), 320),
                Math.max(prefs.getInteger("height", configuration.getWindowHeight()), 320));
        configuration.setWindowPosition(
                prefs.getInteger("x", configuration.getWindowX()),
                prefs.getInteger("y", configuration.getWindowY()));
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

    private static IntRectangle getMonitorWorkarea(long monitorHandle) {
        GLFW.glfwGetMonitorWorkarea(monitorHandle, tmpBuffer0, tmpBuffer1, tmpBuffer2, tmpBuffer3);
        return new IntRectangle(tmpBuffer0.get(0), tmpBuffer1.get(0), tmpBuffer2.get(0), tmpBuffer3.get(0));
    }

    /**
     * This is a copy of {@link Lwjgl3Graphics#getMonitor()},
     * but instead of a wrapper class it only returns the monitor's native handle.
     */
    private static long getCurrentMonitorHandle(Lwjgl3Graphics graphics) {
        Lwjgl3Window window = graphics.getWindow();
        Graphics.Monitor[] monitors = graphics.getMonitors();
        int resultIdx = 0;

        GLFW.glfwGetWindowPos(window.getWindowHandle(), tmpBuffer0, tmpBuffer1);
        int windowX = tmpBuffer0.get(0);
        int windowY = tmpBuffer1.get(0);
        GLFW.glfwGetWindowSize(window.getWindowHandle(), tmpBuffer0, tmpBuffer1);
        int windowWidth = tmpBuffer0.get(0);
        int windowHeight = tmpBuffer1.get(0);
        int overlap;
        int bestOverlap = 0;

        for (int i = 0; i < monitors.length; i++) {
            Graphics.Monitor monitor = monitors[i];
            Graphics.DisplayMode mode = graphics.getDisplayMode(monitor);

            overlap = Math.max(0,
                    Math.min(windowX + windowWidth, monitor.virtualX + mode.width)
                            - Math.max(windowX, monitor.virtualX))
                    * Math.max(0, Math.min(windowY + windowHeight, monitor.virtualY + mode.height)
                    - Math.max(windowY, monitor.virtualY));

            if (bestOverlap < overlap) {
                bestOverlap = overlap;
                resultIdx = i;
            }
        }
        return GLFW.glfwGetMonitors().get(resultIdx);
    }

    public static class IntRectangle {
        public int x;
        public int y;
        public int width;
        public int height;

        public IntRectangle() {

        }

        public IntRectangle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}

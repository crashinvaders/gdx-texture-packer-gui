package com.crashinvaders.texturepackergui.desktop.launchers.awt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;

import javax.swing.*;
import java.awt.*;

class FramePropertiesPersister {
    public static final String PREF_NAME = "window_params.xml";

    public static void saveFrameProperties(JFrame frame) {
        int extendedState = frame.getExtendedState();
        frame.setExtendedState(Frame.NORMAL);

        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
        prefs.putInteger("x", frame.getX());
        prefs.putInteger("y", frame.getY());
        prefs.putInteger("width", frame.getWidth());
        prefs.putInteger("height", frame.getHeight());
        prefs.putBoolean("maximized", extendedState == Frame.MAXIMIZED_BOTH);
        prefs.flush();

        frame.setExtendedState(extendedState);
    }

    public static void loadFrameProperties(JFrame frame) {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);

        DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        int screenWidth = displayMode.getWidth();
        int screenHeight = displayMode.getHeight();

        int width = MathUtils.clamp(prefs.getInteger("width", frame.getSize().width), 320, screenWidth);
        int height = MathUtils.clamp(prefs.getInteger("height", frame.getSize().height), 320, screenHeight);
        int x = MathUtils.clamp(prefs.getInteger("x", frame.getLocation().x), 0, screenWidth - width);
        int y = MathUtils.clamp(prefs.getInteger("y", frame.getLocation().y), 0, screenHeight - height);
        int extendedState = prefs.getBoolean("maximized", false) ? Frame.MAXIMIZED_BOTH : frame.getExtendedState();

        frame.setSize(width, height);
        frame.setLocation(x, y);
        frame.setExtendedState(extendedState);
    }
}

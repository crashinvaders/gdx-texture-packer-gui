package com.crashinvaders.texturepackergui.desktop.launchers.awt;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.backends.lwjgl.LwjglPreferences;
import com.crashinvaders.texturepackergui.App;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This is improved version of original {@link LwjglCanvas}.
 * It supports shared preference params from {@link LwjglApplicationConfiguration}.
 */
class CustomLwjglCanvas extends LwjglCanvas {
    private final Map<String, Preferences> preferences = new HashMap<>();
    private String prefersDir;
    private Files.FileType prefsFileType;

    public CustomLwjglCanvas(App app, LwjglCanvasConfiguration config) {
        super(app, config);

        this.prefersDir = config.preferencesDirectory;
        this.prefsFileType = config.preferencesFileType;
    }

    @Override
    public Preferences getPreferences(String name) {
        if (preferences.containsKey(name)) {
            return preferences.get(name);
        } else {
            Preferences prefs = new LwjglPreferences(new LwjglFileHandle(new File(prefersDir, name), prefsFileType));
            preferences.put(name, prefs);
            return prefs;
        }
    }
}

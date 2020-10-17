package com.badlogic.gdx.backends.lwjgl3;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.utils.Null;

/**
 * An utility extension to Lwjgl3ApplicationConfiguration.
 * Provides some extra public getters.
 */
public class Lwjgl3ApplicationConfigurationX extends Lwjgl3ApplicationConfiguration {

    public int getWindowX() {
        return windowX;
    }

    public int getWindowY() {
        return windowY;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public String getPreferencesDirectory() {
        return preferencesDirectory;
    }

    public @Null Files.FileType getWindowIconFileType() {
        return windowIconFileType;
    }

    public @Null String[] getWindowIconPaths() {
        return windowIconPaths;
    }
}

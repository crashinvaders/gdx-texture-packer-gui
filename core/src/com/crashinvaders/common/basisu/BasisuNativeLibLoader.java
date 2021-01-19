package com.crashinvaders.common.basisu;

import com.badlogic.gdx.utils.SharedLibraryLoader;

public class BasisuNativeLibLoader {

    private static boolean nativeLibLoaded = false;

    /**
     * Ensures that the basisu-wrapper native library is loaded and initialized.
     */
    public static synchronized void loadIfNeeded() {
        if (nativeLibLoaded) return;

        new SharedLibraryLoader().load("basisu-wrapper");
        nativeLibLoaded = true;
    }
}
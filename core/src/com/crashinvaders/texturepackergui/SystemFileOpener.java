package com.crashinvaders.texturepackergui;

import com.badlogic.gdx.files.FileHandle;

public interface SystemFileOpener {

//    /**
//     * Determines whether the feature is supported by the target platform.
//     */
//    boolean isSupported();

    /**
     * Opens the file using OS default action.
     * @return true if the operation was successful.
     */
    boolean openFile(FileHandle fileHandle);
}

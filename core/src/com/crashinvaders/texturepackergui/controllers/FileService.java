package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.files.FileHandle;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;

public interface FileService {
    void pickDirectory(FileHandle initialFolder, FileChooserAdapter callback);
}

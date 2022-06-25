package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;

public class DefaultFileService implements FileService {

    @Inject InterfaceService interfaceService;

    public void initDependencies(InterfaceService interfaceService) {
        this.interfaceService = interfaceService;
    }

    @Override
    public void pickDirectory(FileHandle initialFolder, FileChooserAdapter callback) {
        String initialPath = initialFolder.path();

        FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);
        fileChooser.setDirectory(initialPath);
        fileChooser.setListener(callback);

        getStage().addActor(fileChooser.fadeIn());
    }

    private Stage getStage() {
        return interfaceService.getCurrentController().getStage();
    }

}

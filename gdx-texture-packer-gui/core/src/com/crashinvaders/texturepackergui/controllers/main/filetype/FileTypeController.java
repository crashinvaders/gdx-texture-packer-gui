package com.crashinvaders.texturepackergui.controllers.main.filetype;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.github.czyzby.lml.parser.action.ActionContainer;

public interface FileTypeController extends ActionContainer {
    void onViewCreated(Stage stage);
    void activate();
    void deactivate();
}

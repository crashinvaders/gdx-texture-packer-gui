package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;

@ViewDialog("lml/ninepatcheditor/dialogNinePatchEditor.lml")
public class NinePatchEditorDialog implements ActionContainer {

    @Inject InterfaceService interfaceService;

    @LmlActor("canvasStack") Stack canvasStack;
    CompositionHolder compositionHolder;

    private final ZoomModel zoomModel = new ZoomModel();

    @LmlAfter void initView() {
        Skin skin = interfaceService.getSkin();
        zoomModel.reset();

        Image imgBackground = new Image(skin.getTiledDrawable("custom/transparent-light"));
        canvasStack.addActor(imgBackground);

        Pixmap pixmap = new Pixmap(Gdx.files.absolute("D:/chest0.png"));
        SourceImage sourceImage = new SourceImage(pixmap);

        compositionHolder = new CompositionHolder(skin, sourceImage, zoomModel);
        canvasStack.addActor(compositionHolder);
    }

    @LmlAction("editPatchGrid") void editPatchGrid() {
        compositionHolder.editPatchGrid();
    }

    @LmlAction("editContentGrid") void editContentGrid() {
        compositionHolder.editContentGird();
    }
}

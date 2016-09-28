package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;

@ViewDialog(id = "dialog_pack_scales", value = "lml/packdialogs/dialogPackScales.lml")
public class ScalesDialogController implements ActionContainer {

    @ViewStage Stage stage;

    @LmlAfter void initView() {

    }
}

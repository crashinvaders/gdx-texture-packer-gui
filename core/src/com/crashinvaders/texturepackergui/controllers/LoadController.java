package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.crashinvaders.texturepackergui.controllers.main.MainController;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.asset.AssetService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.SkinService;
import com.github.czyzby.autumn.mvc.component.ui.controller.impl.StandardViewRenderer;
import com.github.czyzby.autumn.mvc.stereotype.View;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlBefore;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisProgressBar;

@View(value = "lml/load.lml")
public class LoadController extends StandardViewRenderer implements ActionContainer {
//    private static final String ATLAS_PATH = "atlases/uiatlas.atlas";

    @Inject AssetService assetService;
    @Inject SkinService skinService;
    @Inject InterfaceService interfaceService;

    @LmlActor("loadingBar") VisProgressBar loadingBar;

    @LmlBefore
    void initialize() {
//        assetService.load(ATLAS_PATH, TextureAtlas.class);
    }

    @Override
    public void render(final Stage stage, final float delta) {
        assetService.update();
        loadingBar.setValue(assetService.getLoadingProgress());
        super.render(stage, delta);
    }

    @LmlAction("onLoadComplete")
    public void onLoadComplete() {
//        Skin skin = skinService.getSkin();
//        skin.addRegions(assetService.get("atlases/uiatlas.atlas", TextureAtlas.class));

        interfaceService.show(MainController.class);
    }
}

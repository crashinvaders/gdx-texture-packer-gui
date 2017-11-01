package com.crashinvaders.texturepackergui.controllers.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.crashinvaders.texturepackergui.views.BusyBar;
import com.crashinvaders.texturepackergui.App;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.controller.impl.StandardViewShower;
import com.github.czyzby.autumn.mvc.stereotype.View;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@View(id = ModuleInstallationController.VIEW_ID, value = "lml/moduleInstallation.lml", first = false)
public class ModuleInstallationController extends StandardViewShower implements ActionContainer {
    public static final String VIEW_ID = "ModuleInstallation";
    public static final String TAG = ModuleInstallationController.class.getSimpleName();

    @Inject InterfaceService interfaceService;

    @ViewStage Stage stage;

    @LmlActor("contentTable") WidgetGroup contentTable;

    @Initiate void init() {
        System.out.println("ModuleInstallationController.init");
    }

    @LmlAfter() void initView() {
        System.out.println("ModuleInstallationController.initView");
    }

    @LmlAction("onCancelClicked") void onCancelClicked() {
//        Gdx.app.postRunnable(new Runnable() {
//            @Override
//            public void run() {
//                App.inst().restart();
//            }
//        });

        try {
            File tmpFile = File.createTempFile("module-extension", null);
            System.out.println(tmpFile.getAbsolutePath());
            FileUtils.copyURLToFile(new URL("https://crashinvaders.github.io/gdx-texture-packer-gui/modules/font-cjk0.zip"), tmpFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @LmlAction("createBusyBar") BusyBar createBusyBar() {
        BusyBar.Style style = new BusyBar.Style();
        style.pattern = (TextureRegionDrawable) interfaceService.getSkin().getDrawable("custom/busy-bar-pattern");
        return new BusyBar(style);
    }

    @Override
    public void show(Stage stage, Action action) {
        super.show(stage, action);
        System.out.println("ModuleInstallationController.show");

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                contentTable.setOrigin(Align.center);
                contentTable.setTransform(true);
                contentTable.addAction(Actions.sequence(
                        Actions.scaleTo(2f, 2f),
                        Actions.scaleTo(1f, 1f, 0.5f, Interpolation.pow5Out)
                ));
            }
        });
    }

    @Override
    public void hide(Stage stage, Action action) {
        super.hide(stage, action);
        System.out.println("ModuleInstallationController.hide");
    }
}

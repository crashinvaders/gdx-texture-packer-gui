package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.crashinvaders.common.async.JobTask;
import com.crashinvaders.common.async.JobTaskQueue;
import com.crashinvaders.common.scene2d.InjectActor;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.crashinvaders.texturepackergui.services.GlobalActions;
import com.crashinvaders.texturepackergui.services.extensionmodules.ExtensionModuleController;
import com.crashinvaders.texturepackergui.services.extensionmodules.ExtensionModuleManagerService;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewDialogShower;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.annotation.LmlBefore;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;

@ViewDialog(id = ExtensionModuleRequiredDialogController.VIEW_ID, value = "lml/dialogExtensionModuleRequired.lml")
public class ExtensionModuleRequiredDialogController implements ActionContainer {
    public static final String VIEW_ID = "ExtensionModuleRequiredDialog";
    public static final String TAG = ExtensionModuleRequiredDialogController.class.getSimpleName();

    @Inject InterfaceService interfaceService;
    @Inject ExtensionModuleManagerService extensionModuleManager;
    @Inject GlobalActions globalActions;

    private String moduleName;

    public void showDialog(String moduleId) {
        ExtensionModuleController moduleController = extensionModuleManager.findModuleController(moduleId);
        if (moduleController == null) {
            Gdx.app.error(TAG, "Can't find extension module controller id: " + moduleId);
            return;
        }
        moduleName = moduleController.getName();
        interfaceService.showDialog(ExtensionModuleRequiredDialogController.class);
    }

    public void hideDialog() {
        interfaceService.destroyDialog(ExtensionModuleRequiredDialogController.class);
    }

    @LmlBefore() void initView() {
        if (moduleName == null) {
            throw new IllegalStateException("Module name wasn't set. You should display this dialog by calling ExtensionModuleRequiredDialogController#showDialog(String).");
        }
        interfaceService.getParser().getData().addArgument("moduleName", moduleName);
    }

    @LmlAction("onOkClicked") void onOkClicked() {
        hideDialog();
        globalActions.showExtensionModulesDialog();
    }
}

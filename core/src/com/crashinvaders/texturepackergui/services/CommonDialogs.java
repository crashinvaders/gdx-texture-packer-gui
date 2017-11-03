package com.crashinvaders.texturepackergui.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.controllers.ExtensionModuleRequiredDialogController;
import com.crashinvaders.texturepackergui.services.extensionmodules.ExtensionModuleController;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.crashinvaders.texturepackergui.services.model.ModelUtils;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.utils.WidgetUtils;
import com.crashinvaders.texturepackergui.views.ButtonBarBuilder;
import com.crashinvaders.texturepackergui.views.ContentDialog;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.lml.scene2d.ui.reflected.ButtonTable;
import com.kotcrab.vis.ui.widget.ButtonBar;
import com.kotcrab.vis.ui.widget.VisTextField;

@Component
public class CommonDialogs {

    @Inject InterfaceService interfaceService;
    @Inject LocaleService localeService;
    @Inject EventDispatcher eventDispatcher;
    @Inject ModelService modelService;
    @Inject ModelUtils modelUtils;
    @Inject ExtensionModuleRequiredDialogController emRequiredDialog;

    public void newPack() {
        final ContentDialog dialog = WidgetUtils.showContentDialog(
                interfaceService,
                getString("newPack"),
                Gdx.files.internal("lml/packdialogs/dialogCreate.lml")
        );
        final PackModel selectedPack = getSelectedPack();
        final VisTextField edtName = dialog.findActor("edtName");
        final ButtonTable btPlacing = dialog.findActor("btPlacing");

        final Runnable okRunnable = new Runnable() {
            @Override
            public void run() {
                dialog.fadeOut();

                PackModel pack = new PackModel();
                pack.setName(edtName.getText());
                getProject().addPack(pack);
                getProject().setSelectedPack(pack);

                switch (btPlacing.getButtonGroup().getChecked().getName()) {
                    case "rbAbove":
                        if (selectedPack != null) modelUtils.movePackPrevTo(selectedPack, pack);
                        break;
                    case "rbBelow":
                        if (selectedPack != null) modelUtils.movePackNextTo(selectedPack, pack);
                        break;
                    case "rbTop":
                        modelUtils.movePackTop(pack);
                        break;
                    case "rbBottom":
                        modelUtils.movePackBottom(pack);
                        break;
                }
            }
        };

        dialog.closeOnEscape();
        dialog.addCloseButton();
        edtName.focusField();
        edtName.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch (keycode) {
                    case Input.Keys.ENTER: {
                        okRunnable.run();
                        return true;
                    }
                }
                return super.keyDown(event, keycode);
            }
        });

        dialog.setupButtons(new ButtonBarBuilder()
                .button(ButtonBar.ButtonType.OK, new ChangeListener() {
                    @Override
                    public void changed (ChangeEvent event, Actor actor) {
                        okRunnable.run();
                    }
                }).button(ButtonBar.ButtonType.CANCEL, new ChangeListener() {
                    @Override
                    public void changed (ChangeEvent event, Actor actor) {
                        dialog.fadeOut();
                    }
                }).prepare());
    }

    public void copyPack(final PackModel sourcePack) {
        if (sourcePack == null) {
            throw new IllegalArgumentException("sourcePack cannot be null");
        }

        final ContentDialog dialog = WidgetUtils.showContentDialog(
                interfaceService,
                getString("makeCopy"),
                Gdx.files.internal("lml/packdialogs/dialogCreate.lml")
        );
        final PackModel selectedPack = getSelectedPack();
        final VisTextField edtName = dialog.findActor("edtName");
        final ButtonTable btPlacing = dialog.findActor("btPlacing");

        final Runnable okRunnable = new Runnable() {
            @Override
            public void run() {
                dialog.fadeOut();

                PackModel pack = new PackModel(sourcePack);
                pack.setName(edtName.getText());
                getProject().addPack(pack);
                getProject().setSelectedPack(pack);

                switch (btPlacing.getButtonGroup().getChecked().getName()) {
                    case "rbAbove":
                        if (selectedPack != null) modelUtils.movePackPrevTo(selectedPack, pack);
                        break;
                    case "rbBelow":
                        if (selectedPack != null) modelUtils.movePackNextTo(selectedPack, pack);
                        break;
                    case "rbTop":
                        modelUtils.movePackTop(pack);
                        break;
                    case "rbBottom":
                        modelUtils.movePackBottom(pack);
                        break;
                }
            }
        };

        dialog.closeOnEscape();
        dialog.addCloseButton();
        edtName.focusField();
        edtName.setText(sourcePack.getName());
        edtName.selectAll();
        edtName.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch (keycode) {
                    case Input.Keys.ENTER: {
                        okRunnable.run();
                        return true;
                    }
                }
                return super.keyDown(event, keycode);
            }
        });

        dialog.setupButtons(new ButtonBarBuilder()
                .button(ButtonBar.ButtonType.OK, new ChangeListener() {
                    @Override
                    public void changed (ChangeEvent event, Actor actor) {
                        okRunnable.run();
                    }
                }).button(ButtonBar.ButtonType.CANCEL, new ChangeListener() {
                    @Override
                    public void changed (ChangeEvent event, Actor actor) {
                        dialog.fadeOut();
                    }
                }).prepare());
    }

    /**
     * Checks if particular extension module is installed and if not, shows dialog.
     * @return true if module is installed.
     */
    public boolean checkExtensionModuleActivated(Class<? extends ExtensionModuleController> moduleControllerClass) {
        ExtensionModuleController moduleController = (ExtensionModuleController) App.inst().getContext().getComponent(moduleControllerClass);
//        if (moduleController.getStatus() == ExtensionModuleController.Status.INSTALLED) {
        if (moduleController.isActivated()) {
            return true;
        } else {
            emRequiredDialog.showDialog(moduleController.getModuleId());
            return false;
        }
    }

    /** @return localized string */
    private String getString(String key) {
        return localeService.getI18nBundle().get(key);
    }
    /** @return localized string */
    private String getString(String key, Object... args) {
        return localeService.getI18nBundle().format(key, args);
    }

    private PackModel getSelectedPack() {
        return getProject().getSelectedPack();
    }

    private ProjectModel getProject() {
        return modelService.getProject();
    }

    private Stage getStage() {
        return interfaceService.getCurrentController().getStage();
    }
}

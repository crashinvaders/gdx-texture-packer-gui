package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.crashinvaders.common.scene2d.InjectActor;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.crashinvaders.texturepackergui.events.ExtensionModuleStatusChangedEvent;
import com.crashinvaders.texturepackergui.services.extensionmodules.ExtensionModuleController;
import com.crashinvaders.texturepackergui.services.extensionmodules.ExtensionModuleManagerService;
import com.crashinvaders.texturepackergui.services.extensionmodules.ExtensionModuleRepositoryService;
import com.crashinvaders.texturepackergui.utils.LmlAutumnUtils;
import com.crashinvaders.texturepackergui.utils.Scene2dUtils;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.component.ui.controller.ViewDialogShower;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

@ViewDialog(id = ExtensionModulesDialogController.VIEW_ID, value = "lml/extensionmodules/dialogExtensionModules.lml")
public class ExtensionModulesDialogController implements ActionContainer, ViewDialogShower {
    public static final String VIEW_ID = "ExtensionModulesDialog";
    public static final String ACTION_CONTAINER_VIEW_HOLDER = "ModuleItemViewHolder";

    @Inject InterfaceService interfaceService;
    @Inject LocaleService localeService;
    @Inject ExtensionModuleManagerService moduleManager;
    @Inject ExtensionModuleRepositoryService moduleRepository;

    @LmlActor("tableModules") Table tableModules;

    private final ArrayMap<String, ModuleViewHolder> moduleViewHolders = new ArrayMap<>();

    @Override
    public void doBeforeShow(Window dialog) {
        moduleRepository.requestRefreshRepositoryIfNeeded();
    }

    @LmlAfter void initView() {
        moduleViewHolders.clear();

        Array<ExtensionModuleController> moduleControllers = moduleManager.getModuleControllers();
        for (ExtensionModuleController moduleController : moduleControllers) {
            ModuleViewHolder viewHolder = new ModuleViewHolder(moduleController);
            moduleViewHolders.put(moduleController.getModuleId(), viewHolder);
            tableModules.row();
            tableModules.add(viewHolder.root);
        }
    }

    @OnEvent(ExtensionModuleStatusChangedEvent.class) void onEvent(ExtensionModuleStatusChangedEvent event) {
        ExtensionModuleController moduleController = event.getModuleController();
        ModuleViewHolder moduleViewHolder = moduleViewHolders.get(moduleController.getModuleId());
        if (moduleViewHolder != null) {
            moduleViewHolder.updateFromController();
        }
    }

    private class ModuleViewHolder implements ActionContainer {
        final ExtensionModuleController moduleController;
        final Group root;

        @InjectActor VisTable frameTable;
        @InjectActor VisLabel lblName;
        @InjectActor VisLabel lblStatus;
        @InjectActor VisLabel lblDescription;
        @InjectActor ShrinkContainer scBtnInstall;
        @InjectActor ShrinkContainer scBtnUpdate;
        @InjectActor ShrinkContainer scBtnUninstall;

        public ModuleViewHolder(ExtensionModuleController moduleController) {
            this.moduleController = moduleController;
            root = LmlAutumnUtils.parseLml(interfaceService, ACTION_CONTAINER_VIEW_HOLDER, this, Gdx.files.internal("lml/extensionmodules/extensionModuleItem.lml"));
            Scene2dUtils.injectActorFields(this, root);
            updateFromController();
        }

        public void updateFromController() {
            scBtnInstall.setVisible(moduleController.getStatus() == ExtensionModuleController.Status.NOT_INSTALLED);
            scBtnUninstall.setVisible(moduleController.getStatus() != ExtensionModuleController.Status.NOT_INSTALLED);
            scBtnUpdate.setVisible(moduleController.getStatus() == ExtensionModuleController.Status.UPDATE_REQUIRED);

            lblName.setText(moduleController.getName());
            lblDescription.setText(moduleController.getDescription());

            Skin skin = interfaceService.getSkin();
            switch (moduleController.getStatus()) {
                case NOT_INSTALLED:
                    frameTable.background(skin.getDrawable("custom/em-item-frame"));
                    lblStatus.setText(localeService.getI18nBundle().get("emRepoStatusNotInstalled"));
                    lblStatus.setColor(skin.getColor("text-grey"));
                    break;
                case INSTALLED:
                    frameTable.background(skin.getDrawable("custom/em-item-frame-installed"));
                    lblStatus.setText(localeService.getI18nBundle().get("emRepoStatusInstalled"));
                    lblStatus.setColor(skin.getColor("text-green"));
                    break;
                case UPDATE_REQUIRED:
                    frameTable.background(skin.getDrawable("custom/em-item-frame-outofdate"));
                    lblStatus.setText(localeService.getI18nBundle().get("emRepoStatusUpdateRequired"));
                    lblStatus.setColor(skin.getColor("text-yellow"));
                    break;
            }
        }

        @LmlAction("onInstallClicked") void onInstallClicked() {
            moduleManager.installModule(moduleController.getModuleId());
        }

        @LmlAction("onUpdateClicked") void onUpdateClicked() {
            moduleManager.updateModule(moduleController.getModuleId());
        }

        @LmlAction("onUninstallClicked") void onUninstallClicked() {
            moduleManager.uninstallModule(moduleController.getModuleId());
        }
    }
}

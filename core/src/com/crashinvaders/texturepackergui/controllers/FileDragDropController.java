package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.DragDropManager;
import com.crashinvaders.texturepackergui.controllers.model.InputFile;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.utils.CommonUtils;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Destroy;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.kiwi.util.common.Strings;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.action.ActionContainer;

@Component
public class FileDragDropController implements DragDropManager.Listener, ActionContainer {
    private static final String TAG = FileDragDropController.class.getSimpleName();
    private static final Vector2 tmpVec2 = new Vector2();

    @Inject InterfaceService interfaceService;
    @Inject LocaleService localeService;
    @Inject GlobalActions globalActions;
    @Inject ModelService modelService;

    @LmlActor("dragndropOverlay") Group overlayRoot;

    private Stage stage;

    private boolean hintVisible = false;

    @Initiate void initialize() {
        interfaceService.getParser().getData().addActionContainer(TAG, this);
        App.inst().getDragDropManager().addListener(this);
    }

    @Destroy void destroy() {
        interfaceService.getParser().getData().removeActionContainer(TAG);
        App.inst().getDragDropManager().removeListener(this);
    }

    public void onViewCreated(Stage stage) {
        this.stage = stage;
    }

    //TODO replace with LML code when the issue gets resolved https://github.com/czyzby/gdx-lml/issues/36
    @LmlAction("getProjectFileHint") String getProjectFileHint() {
        return localeService.getI18nBundle().format("dndProjectFileHint",
                "."+AppConstants.PROJECT_FILE_EXT);
    }

    //TODO replace with LML code when the issue gets resolved https://github.com/czyzby/gdx-lml/issues/36
    @LmlAction("getImageFilesHint") String getImageFilesHint() {
        return localeService.getI18nBundle().format("dndImageFilesHint",
                "."+ Strings.join(" .", (Object[]) AppConstants.IMAGE_FILE_EXT));
    }

    @LmlAction("showDragndropHint") void showDragndropHint() {
        if (hintVisible) return;
        hintVisible = true;

        overlayRoot.clearActions();
        overlayRoot.addAction(Actions.sequence(
                Actions.scaleTo(1.5f, 1.5f),
                Actions.visible(true),
                Actions.touchable(Touchable.enabled),
                Actions.parallel(
                        Actions.fadeIn(0.5f, Interpolation.pow5Out),
                        Actions.scaleTo(1f, 1f, 0.5f, Interpolation.pow5Out)
                )
        ));
    }

    @LmlAction("hideDragndropHint") void hideDragndropHint() {
        if (!hintVisible) return;
        hintVisible = false;

        overlayRoot.clearActions();
        overlayRoot.addAction(Actions.sequence(
                Actions.touchable(Touchable.disabled),
                Actions.fadeOut(0.35f),
                Actions.visible(false)
        ));
    }

    @Override
    public void handleFileDrop(int screenX, int screenY, Array<FileHandle> files) {
        // Reset visual hint.
        hideDragndropHint();

        // Look for a project file. If found, load first and return.
        for (FileHandle file : files) {
            if (AppConstants.PROJECT_FILE_EXT.equals(file.extension())) {
                globalActions.loadProject(file);
                return;
            }
        }

        // If there is no selected pack, select first or create a new one.
        ProjectModel project = modelService.getProject();
        PackModel pack = project.getSelectedPack();
        boolean wasPackCreated = false;
        if (pack == null) {
            if (project.getPacks().size > 0) {
                pack = project.getPacks().first();
            } else {
                pack = new PackModel();
                project.addPack(pack);
                project.setSelectedPack(pack);
                wasPackCreated = true;
            }
            project.setSelectedPack(pack);
        }

        boolean anyFilesAdded = false;
        for (FileHandle file : files) {
            if (file.isDirectory() || CommonUtils.contains(AppConstants.IMAGE_FILE_EXT, file.extension(), false)) {
                InputFile inputFile = new InputFile(file, InputFile.Type.Input);

                // If there is any record with same file handle, we will simply re-add it (just to highlight).
                int existingEntryIdx = pack.getInputFiles().indexOf(inputFile, false);
                if (existingEntryIdx != -1) {
                    inputFile = pack.getInputFiles().get(existingEntryIdx);
                    pack.removeInputFile(inputFile);
                }

                pack.addInputFile(inputFile);
                anyFilesAdded = true;
            }
        }

        // In case no supported files were recognized - show drag-n-drop hint.
        if (!anyFilesAdded) {
            showDragndropHint();
            // Roll back the pack creation...
            if (wasPackCreated) {
                project.removePack(pack);
            }
        }
    }
}
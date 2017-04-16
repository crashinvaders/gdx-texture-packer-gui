package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.DragDropManager;
import com.crashinvaders.texturepackergui.services.GlobalActions;
import com.crashinvaders.texturepackergui.services.model.InputFile;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
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
    @LmlActor("btnDragndropInclude") Button btnInclude;
    @LmlActor("btnDragndropExclude") Button btnExclude;

    private Stage stage;

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

    //TODO replace with LML code when issues get resolved https://github.com/czyzby/gdx-lml/issues/36
    @LmlAction("getProjectFileHint") String getProjectFileHint() {
        return localeService.getI18nBundle().format("dndProjectFileHint",
                "."+AppConstants.PROJECT_FILE_EXT);
    }

    //TODO replace with LML code when issues get resolved https://github.com/czyzby/gdx-lml/issues/36
    @LmlAction("getImageFilesHint") String getImageFilesHint() {
        return localeService.getI18nBundle().format("dndImageFilesHint",
                "."+ Strings.join(" .", AppConstants.IMAGE_FILE_EXT));
    }

    @Override
    public void onDragStarted(int screenX, int screenY) {
        overlayRoot.clearActions();
        overlayRoot.addAction(Actions.sequence(
                Actions.scaleTo(1.5f, 1.5f),
                Actions.visible(true),
                Actions.parallel(
                        Actions.fadeIn(0.5f, Interpolation.pow5Out),
                        Actions.scaleTo(1f, 1f, 0.5f, Interpolation.pow5Out)
                )
        ));
    }

    @Override
    public void onDragFinished() {
        overlayRoot.clearActions();
        overlayRoot.addAction(Actions.sequence(
                Actions.fadeOut(0.35f),
                Actions.visible(false)
        ));

    }

    @Override
    public void onDragMoved(int screenX, int screenY) {
        stage.mouseMoved(screenX, screenY);
    }

    @Override
    public void handleFileDrop(int screenX, int screenY, Array<FileHandle> files) {
        // Look for a project file. If found, load first and return.
        for (FileHandle file : files) {
            if (AppConstants.PROJECT_FILE_EXT.equals(file.extension())) {
                globalActions.loadProject(file);
                return;
            }
        }

        stage.screenToStageCoordinates(tmpVec2.set(screenX, screenY));
        Actor hit = stage.hit(tmpVec2.x, tmpVec2.y, true);
        boolean include = hit == btnInclude;
        boolean exclude = hit == btnExclude;

        // If there is no selected pack, we will either select first or create new one
        ProjectModel project = modelService.getProject();
        PackModel pack = project.getSelectedPack();
        if (pack == null) {
            if (project.getPacks().size > 0) {
                pack = project.getPacks().first();
            } else {
                pack = new PackModel();
                project.addPack(pack);
                project.setSelectedPack(pack);
            }
            project.setSelectedPack(pack);
        }

        // Look for a project file. If found, load first and return.
        for (FileHandle file : files) {
            if (file.isDirectory()) {
                if (include) { pack.addInputFile(file, InputFile.Type.Input); }
            } else {
                if (CommonUtils.contains(AppConstants.IMAGE_FILE_EXT, file.extension(), false)) {
                    InputFile inputFile = null;

                    if (include) { inputFile = new InputFile(file, InputFile.Type.Input); }
                    if (exclude) { inputFile = new InputFile(file, InputFile.Type.Ignore); }

                    if (inputFile == null) {
                        Gdx.app.error(TAG, "Can't add input file: " + file);
                        continue;
                    }

                    // If there is any record with same file handle, we will replace it
                    if (pack.getInputFiles().contains(inputFile, false)) {
                        pack.removeInputFile(inputFile);
                    }

                    pack.addInputFile(inputFile);
                }
            }
        }
    }
}

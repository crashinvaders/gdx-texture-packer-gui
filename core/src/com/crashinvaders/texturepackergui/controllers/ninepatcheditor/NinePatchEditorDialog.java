package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.crashinvaders.texturepackergui.controllers.ErrorDialogController;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisDialog;

@ViewDialog("lml/ninepatcheditor/dialogNinePatchEditor.lml")
public class NinePatchEditorDialog implements ActionContainer {
    private static final String TAG = NinePatchEditorDialog.class.getSimpleName();

    @Inject InterfaceService interfaceService;
    @Inject ErrorDialogController errorDialogController;

    @LmlActor("dialog") VisDialog dialog;
    @LmlActor("canvasStack") Stack canvasStack;
    CompositionHolder compositionHolder;

    private NinePatchEditorModel model;

    @LmlAfter void initView() {
        if (model == null) {
            showErrorAndHide(new IllegalStateException("Model is not initialized. Have you properly called setImageFile() before showing dialog?"));
            return;
        }

        Skin skin = interfaceService.getSkin();

        Image imgBackground = new Image(skin.getTiledDrawable("custom/transparent-light"));
        canvasStack.addActor(imgBackground);

        SourceImage sourceImage = new SourceImage(model.imagePixmap);

        compositionHolder = new CompositionHolder(skin, sourceImage, model);
        canvasStack.addActor(compositionHolder);
    }

    @LmlAction("editPatchGrid") void editPatchGrid() {
        compositionHolder.editPatchGrid();
    }

    @LmlAction("editContentGrid") void editContentGrid() {
        compositionHolder.editContentGird();
    }

    public void setImageFile(FileHandle imageFile) {
        if (model != null) {
            model.dispose();
            model = null;
        }

        if (imageFile.exists());

        try {
            model = new NinePatchEditorModel(imageFile);
        } catch (Exception e) {
            showErrorAndHide(e);
        }
    }

    private void showErrorAndHide(final Exception exception) {
        Gdx.app.error(TAG, "", exception);
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.hide();
                }
                // Show error dialog
                errorDialogController.setError(exception);
                interfaceService.showDialog(errorDialogController.getClass());
            }
        });
    }
}

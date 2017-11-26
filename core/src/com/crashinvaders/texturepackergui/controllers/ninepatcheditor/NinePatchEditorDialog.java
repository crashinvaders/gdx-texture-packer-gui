package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.crashinvaders.common.scene2d.ScalarScalableWrapper;
import com.crashinvaders.common.scene2d.TransformScalableWrapper;
import com.crashinvaders.texturepackergui.controllers.ErrorDialogController;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.github.czyzby.autumn.annotation.Destroy;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisSlider;

@ViewDialog("lml/ninepatcheditor/dialogNinePatchEditor.lml")
public class NinePatchEditorDialog implements ActionContainer {
    private static final String TAG = NinePatchEditorDialog.class.getSimpleName();
    private static final float PREVIEW_UPDATE_THRESHOLD = 1f;

    @Inject InterfaceService interfaceService;
    @Inject ErrorDialogController errorDialogController;
    @Inject ModelService modelService;

    @LmlActor("dialog") VisDialog dialog;
    @LmlActor("chbMatchContent") VisCheckBox chbMatchContent;
    @LmlActor("compositionHolder") CompositionHolder compositionHolder;
    @LmlActor("imgPreviewBackground") Image imgPreviewBackground;
    @LmlActor("previewTransform") TransformScalableWrapper previewTransform;
    @LmlActor("previewScalar") ScalarScalableWrapper previewScalar;
    @LmlActor("previewImage") Image previewImage;
    @LmlActor("sbPreviewScaleX") Slider sbPreviewScaleX;
    @LmlActor("sbPreviewScaleY") Slider sbPreviewScaleY;
    @LmlActor("lblPreviewScaleX") Label lblPreviewScaleX;
    @LmlActor("lblPreviewScaleY") Label lblPreviewScaleY;

    private NinePatchEditorModel model;
    private ResultListener resultListener;

    @LmlAfter void initView() {
        if (model == null) {
            showErrorAndHide(new IllegalStateException("Model is not initialized. Have you properly called setImageFile() before showing dialog?"));
            return;
        }

        if (model.patchValues.equals(model.contentValues)) {
            chbMatchContent.setChecked(true);
        }
        model.patchValues.addListener(new GridValues.ChangeListener() {
            @Override
            public void onValuesChanged(GridValues values) {
                if (chbMatchContent.isChecked()) {
                    model.contentValues.set(model.patchValues);
                }
                updatePreviewNinePatch();
            }
        });
        model.contentValues.addListener(new GridValues.ChangeListener() {
            @Override
            public void onValuesChanged(GridValues values) {
                if (chbMatchContent.isChecked() && !model.contentValues.equals(model.patchValues)) {
                    chbMatchContent.setChecked(false);
                }
            }
        });

        //TODO remove
        updatePreviewNinePatch();
//        previewScalar.setScale(0.5f, 2f);
//        previewScalar.addAction(Actions.repeat(-1, Actions.sequence(
//                Actions.scaleTo(2f, 0.5f, 2f, Interpolation.pow4),
//                Actions.scaleTo(0.5f, 2f, 2f, Interpolation.pow4)
//        )));
//        previewTransform.addAction(Actions.repeat(-1, Actions.sequence(
//                Actions.scaleTo(3f, 3f, 3f, Interpolation.pow4),
//                Actions.scaleTo(1f, 1f, 3f, Interpolation.pow4)
//        )));
        imgPreviewBackground.setColor(modelService.getProject().getPreviewBackgroundColor());

        model.zoomModel.addListener(new ZoomModel.ChangeListener() {
            @Override
            public void onZoomIndexChanged(int zoomIndex, float scale) {
                previewTransform.setScale(scale);
            }
        });
        sbPreviewScaleX.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = sbPreviewScaleX.getValue();
                previewScalar.setScaleX(value);
                lblPreviewScaleX.setText(String.format("%.0f%%", value*100f));
            }
        });
        sbPreviewScaleY.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = sbPreviewScaleY.getValue();
                previewScalar.setScaleY(value);
                lblPreviewScaleY.setText(String.format("%.0f%%", value*100f));
            }
        });
    }

    @Destroy void destroy() {
        if (model != null) {
            model.dispose();
            model = null;
        }
    }

    @LmlAction("createCompositionHolder") CompositionHolder createCompositionHolder() {
        return new CompositionHolder(interfaceService.getSkin(), new SourceImage(model.pixmap), model);
    }

    @LmlAction("editPatchGrid") void editPatchGrid() {
        compositionHolder.activatePatchGrid();
    }

    @LmlAction("editContentGrid") void editContentGrid() {
        compositionHolder.activateContentGird();
    }

    @LmlAction("hide") void hide() {
        if (dialog != null) {
            dialog.hide();
        }
    }

    @LmlAction("confirmResult") void confirmResult() {
        if (resultListener != null) {
            resultListener.onResult(model);
        }

        model.dispose();
        model = null;
        hide();
    }

    @LmlAction("onMatchContentChanged") void onMatchContentChanged() {
        boolean checked = chbMatchContent.isChecked();

        if (checked) {
            model.contentValues.set(model.patchValues);
        }
    }

    public NinePatchEditorModel getModel() {
        return model;
    }

    public void setImageFile(FileHandle imageFile) {
        if (model != null) {
            model.dispose();
            model = null;
        }

        try {
            model = new NinePatchEditorModel(imageFile);
        } catch (Exception e) {
            showErrorAndHide(e);
        }
    }

    public void setResultListener(ResultListener resultListener) {
        this.resultListener = resultListener;
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

    private void updatePreviewNinePatch() {
        System.out.println("NinePatchEditorDialog.updatePreviewNinePatch");
        int[] patches = model.readPatchValues();
        NinePatch ninePatch = new NinePatch(model.texture, patches[0], patches[1], patches[2], patches[3]);
        previewImage.setDrawable(new NinePatchDrawable(ninePatch));
    }

    public interface ResultListener {
        /** Called only if user confirmed changes (closed dialog through "OK" button) */
        void onResult(NinePatchEditorModel model);
    }

//    private class PreviewUpdateAction extends Action {
//        private final float threshold;
//        private float counter = 0f;
//        private boolean triggered = false;
//
//        public PreviewUpdateAction(float threshold) {
//            this.threshold = threshold;
//        }
//
//        @Override
//        public void restart() {
//            super.restart();
//            counter = 0f;
//            triggered = false;
//        }
//
//        @Override
//        public boolean act(float delta) {
//            if (triggered) return true;
//
//            counter += delta;
//            triggered = counter > threshold;
//            if (triggered) {
//                updatePreviewNinePatch();
//            }
//            return triggered;
//        }
//    }
}

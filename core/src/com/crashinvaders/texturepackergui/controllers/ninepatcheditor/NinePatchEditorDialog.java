package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.crashinvaders.common.scene2d.ScalarScalableWrapper;
import com.crashinvaders.common.scene2d.TransformScalableWrapper;
import com.crashinvaders.texturepackergui.controllers.ErrorDialogController;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
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
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

@ViewDialog("lml/ninepatcheditor/dialogNinePatchEditor.lml")
public class NinePatchEditorDialog implements ActionContainer {
    private static final String TAG = NinePatchEditorDialog.class.getSimpleName();

    @Inject InterfaceService interfaceService;
    @Inject ErrorDialogController errorDialogController;
    @Inject ModelService modelService;

    @LmlActor("dialog") VisDialog dialog;
    @LmlActor("chbMatchContent") VisCheckBox chbMatchContent;
    @LmlActor("compositionHolder") CompositionHolder compositionHolder;
    @LmlActor("imgPreviewBackground") Image imgPreviewBackground;
    @LmlActor("spPreview") ScrollPane spPreview;
    @LmlActor("previewTransform") TransformScalableWrapper previewTransform;
    @LmlActor("previewScalar") ScalarScalableWrapper previewScalar;
    @LmlActor("previewImage") Image previewImage;
    @LmlActor("sbPreviewScaleX") Slider sbPreviewScaleX;
    @LmlActor("sbPreviewScaleY") Slider sbPreviewScaleY;
    @LmlActor("lblPreviewScaleX") Label lblPreviewScaleX;
    @LmlActor("lblPreviewScaleY") Label lblPreviewScaleY;
    @LmlActor("spnValueLeft") Spinner spnValueLeft;
    @LmlActor("spnValueRight") Spinner spnValueRight;
    @LmlActor("spnValueBottom") Spinner spnValueBottom;
    @LmlActor("spnValueTop") Spinner spnValueTop;

    private NinePatchEditorModel model;
    private ResultListener resultListener;

    private boolean contentEditMode = false;

    @LmlAfter void initView() {
        if (model == null) {
            showErrorAndHide(new IllegalStateException("Model is not initialized. Have you properly called setImageFile() before showing dialog?"));
            return;
        }

        contentEditMode = false;

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

        updatePreviewNinePatch();
        updatePadValuesFromModel();
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
        spPreview.addListener(new InputListener() {
            @Override
            public boolean scrolled(InputEvent event, float x, float y, int amount) {
                int zoomIndex = model.zoomModel.getIndex();
                model.zoomModel.setIndex(zoomIndex - amount);
                return true;
            }
        });

        model.contentValues.addListener(new GridValues.ChangeListener() {
            @Override
            public void onValuesChanged(GridValues values) {
                if (contentEditMode) {
                    updatePadValuesFromModel();
                }
            }
        });
        model.patchValues.addListener(new GridValues.ChangeListener() {
            @Override
            public void onValuesChanged(GridValues values) {
                if (!contentEditMode) {
                    updatePadValuesFromModel();
                }
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
        contentEditMode = false;
        updatePadValuesFromModel();
    }

    @LmlAction("editContentGrid") void editContentGrid() {
        compositionHolder.activateContentGird();
        contentEditMode = true;
        updatePadValuesFromModel();
    }

    @LmlAction("hide") void hide() {
        if (model != null) {
            model.dispose();
            model = null;
        }

        if (dialog != null) {
            dialog.hide();
        }
    }

    @LmlAction("confirmResult") void confirmResult() {
        if (resultListener != null) {
            resultListener.onResult(model);
        }
        hide();
    }

    @LmlAction("onMatchContentChanged") void onMatchContentChanged() {
        boolean checked = chbMatchContent.isChecked();

        if (checked) {
            model.contentValues.set(model.patchValues);
        }
    }

    @LmlAction("updatePadValuesFromView") void updatePadValuesFromView() {
        GridValues gridValues = getActiveGridValues();
        gridValues.set(
                ((IntSpinnerModel) spnValueLeft.getModel()).getValue(),
                ((IntSpinnerModel) spnValueRight.getModel()).getValue(),
                ((IntSpinnerModel) spnValueBottom.getModel()).getValue(),
                ((IntSpinnerModel) spnValueTop.getModel()).getValue());
    }

    @LmlAction("updateLeftPadFromView") void updateLeftPadFromView(Spinner spinner) {
        GridValues gridValues = getActiveGridValues();
        IntSpinnerModel spinnerModel = (IntSpinnerModel) spinner.getModel();

        int maxValue = model.texture.getWidth() - gridValues.right.get() - 1;
        if (spinnerModel.getValue() > maxValue) {
            spinnerModel.setValue(maxValue);
            return;
        }
        gridValues.left.set(spinnerModel.getValue());
    }
    @LmlAction("updateRightPadFromView") void updateRightPadFromView(Spinner spinner) {
        GridValues gridValues = getActiveGridValues();
        IntSpinnerModel spinnerModel = (IntSpinnerModel) spinner.getModel();

        int maxValue = model.texture.getWidth() - gridValues.left.get() - 1;
        if (spinnerModel.getValue() > maxValue) {
            spinnerModel.setValue(maxValue);
            return;
        }
        gridValues.right.set(spinnerModel.getValue());
    }
    @LmlAction("updateBottomPadFromView") void updateBottomPadFromView(Spinner spinner) {
        GridValues gridValues = getActiveGridValues();
        IntSpinnerModel spinnerModel = (IntSpinnerModel) spinner.getModel();

        int maxValue = model.texture.getHeight() - gridValues.top.get() - 1;
        if (spinnerModel.getValue() > maxValue) {
            spinnerModel.setValue(maxValue);
            return;
        }
        gridValues.bottom.set(spinnerModel.getValue());
    }
    @LmlAction("updateTopPadFromView") void updateTopPadFromView(Spinner spinner) {
        GridValues gridValues = getActiveGridValues();
        IntSpinnerModel spinnerModel = (IntSpinnerModel) spinner.getModel();

        int maxValue = model.texture.getHeight() - gridValues.bottom.get() - 1;
        if (spinnerModel.getValue() > maxValue) {
            spinnerModel.setValue(maxValue);
            return;
        }
        gridValues.top.set(spinnerModel.getValue());
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

    private void updatePreviewNinePatch() {
        int[] patches = model.readPatchValues();
        NinePatch ninePatch = new NinePatch(model.texture, patches[0], patches[1], patches[2], patches[3]);
        previewImage.setDrawable(new NinePatchDrawable(ninePatch));
    }

    private GridValues getActiveGridValues() {
        GridValues gridValues;
        if (contentEditMode) {
            gridValues = model.contentValues;
        } else {
            gridValues = model.patchValues;
        }
        return gridValues;
    }

    private void updatePadValuesFromModel() {
        GridValues gridValues = getActiveGridValues();
        ((IntSpinnerModel) spnValueLeft.getModel()).setValue(gridValues.left.get());
        ((IntSpinnerModel) spnValueRight.getModel()).setValue(gridValues.right.get());
        ((IntSpinnerModel) spnValueBottom.getModel()).setValue(gridValues.bottom.get());
        ((IntSpinnerModel) spnValueTop.getModel()).setValue(gridValues.top.get());
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

    public interface ResultListener {
        /** Called only if user confirmed changes (closed dialog through "OK" button) */
        void onResult(NinePatchEditorModel model);
    }
}

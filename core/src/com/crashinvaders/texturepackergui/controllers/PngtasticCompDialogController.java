package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.crashinvaders.texturepackergui.services.model.PngCompressionType;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.services.model.compression.PngCompressionModel;
import com.crashinvaders.texturepackergui.services.model.compression.PngtasticCompressionModel;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

@ViewDialog(id = "dialog_comp_pngtastic", value = "lml/compression/dialogPngtastic.lml")
public class PngtasticCompDialogController implements ActionContainer {
    private static final String TAG = PngtasticCompDialogController.class.getSimpleName();

    @Inject ModelService modelService;

    @LmlActor("spnLevel") Spinner spnLevel;
    @LmlActor("chbRemoveGamma") VisCheckBox chbRemoveGamma;

    private PngtasticCompressionModel compressionModel;

    @LmlAfter
    public void initialize() {
        compressionModel = obtainCompressionModel();
        if (compressionModel == null) return;

        updateValuesFromModel();
    }

    @LmlAction("onLevelValueChanged") void onLevelValueChanged() {
        int level = ((IntSpinnerModel) spnLevel.getModel()).getValue();
        compressionModel.setLevel(level);
    }

    @LmlAction("onRemoveGammaChanged") void onRemoveGammaChanged() {
        boolean removeGamma = chbRemoveGamma.isChecked();
        compressionModel.setRemoveGamma(removeGamma);
    }

    private void updateValuesFromModel() {
        ((IntSpinnerModel) spnLevel.getModel()).setValue(compressionModel.getLevel());
        chbRemoveGamma.setChecked(compressionModel.isRemoveGamma());
    }

    private PngtasticCompressionModel obtainCompressionModel() {
        ProjectModel project = modelService.getProject();
        PngCompressionModel pngCompression = project.getPngCompression();

        if (pngCompression == null || pngCompression.getType() != PngCompressionType.PNGTASTIC) {
            Gdx.app.error(TAG, "Dialog was created while model holds different compression type");
            return null;
        }
        return (PngtasticCompressionModel) project.getPngCompression();
    }
}

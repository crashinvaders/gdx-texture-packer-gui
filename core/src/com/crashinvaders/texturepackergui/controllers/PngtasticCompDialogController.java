package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.crashinvaders.texturepackergui.services.model.PngCompressionType;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.services.model.compression.PngCompressionModel;
import com.crashinvaders.texturepackergui.services.model.compression.PngtasticCompressionModel;
import com.crashinvaders.texturepackergui.services.model.filetype.FileTypeModel;
import com.crashinvaders.texturepackergui.services.model.filetype.KtxFileTypeModel;
import com.crashinvaders.texturepackergui.services.model.filetype.PngFileTypeModel;
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
        FileTypeModel fileType = project.getFileType();

        if (!(fileType instanceof PngFileTypeModel)) {
            Gdx.app.error(TAG, "Project isn't set to PNG file type");
            return null;
        }

        PngCompressionModel compression = ((PngFileTypeModel) project.getFileType()).getCompression();
        if (!(compression instanceof PngtasticCompressionModel)) {
            Gdx.app.error(TAG, "Project isn't set to Pngtastic compression");
            return null;
        }

        return (PngtasticCompressionModel) compression;
    }
}

package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.crashinvaders.texturepackergui.services.model.PngCompressionType;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.services.model.compression.PngCompressionModel;
import com.crashinvaders.texturepackergui.services.model.compression.ZopfliCompressionModel;
import com.crashinvaders.texturepackergui.services.model.filetype.FileTypeModel;
import com.crashinvaders.texturepackergui.services.model.filetype.PngFileTypeModel;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

@ViewDialog(id = "dialog_comp_zopfli", value = "lml/compression/dialogZopfli.lml")
public class ZopfliCompDialogController implements ActionContainer {
    private static final String TAG = ZopfliCompDialogController.class.getSimpleName();

    @Inject ModelService modelService;

    @LmlActor("spnLevel") Spinner spnLevel;
    @LmlActor("spnIterations") Spinner spnIterations;

    private ZopfliCompressionModel compressionModel;

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

    @LmlAction("onIterationsValueChanged") void onIterationsValueChanged() {
        int level = ((IntSpinnerModel) spnIterations.getModel()).getValue();
        compressionModel.setIterations(level);
    }

    private void updateValuesFromModel() {
        ((IntSpinnerModel) spnLevel.getModel()).setValue(compressionModel.getLevel());
        ((IntSpinnerModel) spnIterations.getModel()).setValue(compressionModel.getIterations());
    }

    private ZopfliCompressionModel obtainCompressionModel() {
        ProjectModel project = modelService.getProject();
        FileTypeModel fileType = project.getFileType();

        if (!(fileType instanceof PngFileTypeModel)) {
            Gdx.app.error(TAG, "Project isn't set to PNG file type");
            return null;
        }

        PngCompressionModel compression = ((PngFileTypeModel) project.getFileType()).getCompression();
        if (!(compression instanceof ZopfliCompressionModel)) {
            Gdx.app.error(TAG, "Project isn't set to Pngtastic compression");
            return null;
        }

        return (ZopfliCompressionModel) compression;
    }
}

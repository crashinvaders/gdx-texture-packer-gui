package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.controllers.model.compression.Png8CompressionModel;
import com.crashinvaders.texturepackergui.controllers.model.compression.PngCompressionModel;
import com.crashinvaders.texturepackergui.controllers.model.filetype.FileTypeModel;
import com.crashinvaders.texturepackergui.controllers.model.filetype.PngFileTypeModel;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

@ViewDialog(id = "dialog_comp_png8", value = "lml/compression/dialogPng8.lml")
public class Png8CompDialogController implements ActionContainer {
    private static final String TAG = Png8CompDialogController.class.getSimpleName();

    @Inject ModelService modelService;

    @LmlActor("spnLevel") Spinner spnLevel;
    @LmlActor("spnThreshold") Spinner spnThreshold;
    @LmlActor("chbDithering") VisCheckBox chbDithering;

    private Png8CompressionModel compressionModel;

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

    @LmlAction("onThresholdValueChanged") void onThresholdValueChanged() {
        int threshold = ((IntSpinnerModel) spnThreshold.getModel()).getValue();
        compressionModel.setThreshold(threshold);
    }

    @LmlAction("onDitheringChanged") void onDitheringChanged() {
        boolean dithering = chbDithering.isChecked();
        compressionModel.setDithering(dithering);
    }

    private void updateValuesFromModel() {
        ((IntSpinnerModel) spnLevel.getModel()).setValue(compressionModel.getLevel());
        ((IntSpinnerModel) spnThreshold.getModel()).setValue(compressionModel.getThreshold());
        chbDithering.setChecked(compressionModel.isDithering());
    }

    private Png8CompressionModel obtainCompressionModel() {
        ProjectModel project = modelService.getProject();
        FileTypeModel fileType = project.getFileType();

        if (!(fileType instanceof PngFileTypeModel)) {
            Gdx.app.error(TAG, "Project isn't set to PNG file type");
            return null;
        }

        PngCompressionModel compression = ((PngFileTypeModel) project.getFileType()).getCompression();
        if (!(compression instanceof Png8CompressionModel)) {
            Gdx.app.error(TAG, "Project isn't set to PNG8 (Palette) compression");
            return null;
        }

        return (Png8CompressionModel) compression;
    }
}

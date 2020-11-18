package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.controllers.model.compression.Png8CompressionModel;
import com.crashinvaders.texturepackergui.controllers.model.compression.PngCompressionModel;
import com.crashinvaders.texturepackergui.controllers.model.filetype.FileTypeModel;
import com.crashinvaders.texturepackergui.controllers.model.filetype.PngFileTypeModel;
import com.crashinvaders.texturepackergui.views.seekbar.IntSeekBarModel;
import com.crashinvaders.texturepackergui.views.seekbar.SeekBar;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.Dithered.DitherAlgorithm;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisSelectBox;

@ViewDialog(id = "dialog_comp_te_png8", value = "lml/compression/dialogTePng8.lml")
public class TePng8CompDialogController implements ActionContainer {
    private static final String TAG = TePng8CompDialogController.class.getSimpleName();

    @Inject ModelService modelService;

    @LmlActor SeekBar sbCompLevel;
    @LmlActor SeekBar sbColorThreshold;
    @LmlActor VisSelectBox<DitherAlgorithm> cboDitherAlgorithm;

    private Png8CompressionModel compressionModel;

    private boolean ignoreChangeEvents = false;

    @LmlAfter
    public void initialize() {
        compressionModel = obtainCompressionModel();
        if (compressionModel == null) return;

        updateValuesFromModel();
    }

    @LmlAction void onCompLevelChanged() {
        if (ignoreChangeEvents) return;

        int level = ((IntSeekBarModel) sbCompLevel.getModel()).getValue();
        compressionModel.setLevel(level);
    }

    @LmlAction void onColorThresholdChanged() {
        if (ignoreChangeEvents) return;

        int threshold = ((IntSeekBarModel) sbColorThreshold.getModel()).getValue();
        compressionModel.setThreshold(threshold);
    }

    @LmlAction void onDitherAlgorithmChanged() {
        if (ignoreChangeEvents) return;

        DitherAlgorithm algorithm = cboDitherAlgorithm.getSelected();
        compressionModel.setDitherAlgorithm(algorithm);
    }

    private void updateValuesFromModel() {
        ignoreChangeEvents = true;

        ((IntSeekBarModel) sbCompLevel.getModel()).setValue(compressionModel.getLevel());
        ((IntSeekBarModel) sbColorThreshold.getModel()).setValue(compressionModel.getThreshold());

        cboDitherAlgorithm.setItems(DitherAlgorithm.values());
        cboDitherAlgorithm.setSelected(compressionModel.getDitherAlgorithm());

        ignoreChangeEvents = false;
    }

    private Png8CompressionModel obtainCompressionModel() {
        ProjectModel project = modelService.getProject();
        FileTypeModel fileType = project.getFileType();

        if (!(fileType instanceof PngFileTypeModel)) {
            Gdx.app.error(TAG, "Project isn't set to PNG file type.");
            return null;
        }

        PngCompressionModel compression = ((PngFileTypeModel) project.getFileType()).getCompression();
        if (!(compression instanceof Png8CompressionModel)) {
            Gdx.app.error(TAG, "Project isn't set to anim8-gdx (PNG8) compression.");
            return null;
        }

        return (Png8CompressionModel) compression;
    }
}

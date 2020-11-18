package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.controllers.model.compression.PngCompressionModel;
import com.crashinvaders.texturepackergui.controllers.model.compression.PngquantCompressionModel;
import com.crashinvaders.texturepackergui.controllers.model.filetype.FileTypeModel;
import com.crashinvaders.texturepackergui.controllers.model.filetype.PngFileTypeModel;
import com.crashinvaders.texturepackergui.views.seekbar.FloatSeekBarModel;
import com.crashinvaders.texturepackergui.views.seekbar.IntSeekBarModel;
import com.crashinvaders.texturepackergui.views.seekbar.SeekBar;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisCheckBox;

@ViewDialog(id = "dialog_comp_pngquant", value = "lml/compression/dialogPngquant.lml")
public class PngquantCompDialogController implements ActionContainer {
    private static final String TAG = PngquantCompDialogController.class.getSimpleName();

    @Inject ModelService modelService;

    @LmlActor SeekBar sbMaxColors;
    @LmlActor SeekBar sbMinQuality;
    @LmlActor SeekBar sbMaxQuality;
    @LmlActor SeekBar sbCompSpeed;
    @LmlActor SeekBar sbDithering;
    @LmlActor SeekBar sbCompLevel;

    private PngquantCompressionModel compressionModel;

    private boolean ignoreChangeEvents = false;

    @LmlAfter
    public void initialize() {
        compressionModel = obtainCompressionModel();
        if (compressionModel == null) return;

        updateValuesFromModel();
    }

    @LmlAction void onCompSpeedChanged() {
        if (ignoreChangeEvents) return;

        int speed = ((IntSeekBarModel) sbCompSpeed.getModel()).getValue();
        compressionModel.setSpeed(speed);
    }

    @LmlAction void onMinQualityChanged() {
        if (ignoreChangeEvents) return;

        int minQuality = ((IntSeekBarModel) sbMinQuality.getModel()).getValue();
        compressionModel.setMinQuality(minQuality);

        if (compressionModel.getMaxQuality() < minQuality) {
            ((IntSeekBarModel) sbMaxQuality.getModel()).setValue(minQuality);
        }
    }

    @LmlAction void onMaxQualityChanged() {
        if (ignoreChangeEvents) return;

        int maxQuality = ((IntSeekBarModel) sbMaxQuality.getModel()).getValue();
        compressionModel.setMaxQuality(maxQuality);

        if (compressionModel.getMinQuality() > maxQuality) {
            ((IntSeekBarModel) sbMinQuality.getModel()).setValue(maxQuality);
        }
    }

    @LmlAction void onMaxColorsChanged() {
        if (ignoreChangeEvents) return;

        int maxColors = ((IntSeekBarModel) sbMaxColors.getModel()).getValue();
        compressionModel.setMaxColors(maxColors);
    }

    @LmlAction void onCompLevelChanged() {
        if (ignoreChangeEvents) return;

        int level = ((IntSeekBarModel) sbCompLevel.getModel()).getValue();
        compressionModel.setDeflateLevel(level);
    }

    @LmlAction void onDitheringChanged() {
        if (ignoreChangeEvents) return;

        float ditheringLevel = ((FloatSeekBarModel) sbDithering.getModel()).getValue();
        compressionModel.setDitheringLevel(ditheringLevel);
    }

    private void updateValuesFromModel() {
        ignoreChangeEvents = true;
        ((IntSeekBarModel) sbCompSpeed.getModel()).setValue(compressionModel.getSpeed());
        ((IntSeekBarModel) sbMaxColors.getModel()).setValue(compressionModel.getMaxColors());
        ((IntSeekBarModel) sbMinQuality.getModel()).setValue(compressionModel.getMinQuality());
        ((IntSeekBarModel) sbMaxQuality.getModel()).setValue(compressionModel.getMaxQuality());
        ((IntSeekBarModel) sbCompLevel.getModel()).setValue(compressionModel.getDeflateLevel());
        ((FloatSeekBarModel) sbDithering.getModel()).setValue(compressionModel.getDitheringLevel());
        ignoreChangeEvents = false;
    }

    private PngquantCompressionModel obtainCompressionModel() {
        ProjectModel project = modelService.getProject();
        FileTypeModel fileType = project.getFileType();

        if (!(fileType instanceof PngFileTypeModel)) {
            Gdx.app.error(TAG, "Project isn't set to PNG file type");
            return null;
        }

        PngCompressionModel compression = ((PngFileTypeModel) project.getFileType()).getCompression();
        if (!(compression instanceof PngquantCompressionModel)) {
            Gdx.app.error(TAG, "Project isn't set to PNG8 (Pngquant) compression");
            return null;
        }

        return (PngquantCompressionModel) compression;
    }
}

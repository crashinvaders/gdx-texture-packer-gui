package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.controllers.model.compression.PngCompressionModel;
import com.crashinvaders.texturepackergui.controllers.model.compression.PngtasticCompressionModel;
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
import com.kotcrab.vis.ui.widget.VisCheckBox;

@ViewDialog(id = "dialog_comp_pngtastic", value = "lml/compression/dialogPngtastic.lml")
public class PngtasticCompDialogController implements ActionContainer {
    private static final String TAG = PngtasticCompDialogController.class.getSimpleName();

    @Inject ModelService modelService;

    @LmlActor SeekBar sbCompLevel;
    @LmlActor VisCheckBox chbRemoveGamma;

    private PngtasticCompressionModel compressionModel;

    @LmlAfter
    public void initialize() {
        compressionModel = obtainCompressionModel();
        if (compressionModel == null) return;

        updateValuesFromModel();
    }

    @LmlAction void onCompLevelChanged() {
        int level = ((IntSeekBarModel) sbCompLevel.getModel()).getValue();
        compressionModel.setLevel(level);
    }

    @LmlAction void onRemoveGammaChanged() {
        boolean removeGamma = chbRemoveGamma.isChecked();
        compressionModel.setRemoveGamma(removeGamma);
    }

    private void updateValuesFromModel() {
        ((IntSeekBarModel) sbCompLevel.getModel()).setValue(compressionModel.getLevel());
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

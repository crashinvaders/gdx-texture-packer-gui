package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.crashinvaders.texturepackergui.services.model.EtcCompressionType;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.services.model.compression.EtcCompressionModel;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisSelectBox;

//TODO remove
@ViewDialog(id = "dialog_comp_etc", value = "lml/compression/dialogEtc.lml")
public class EtcCompDialogController implements ActionContainer {
    private static final String TAG = EtcCompDialogController.class.getSimpleName();
    
    @Inject ModelService modelService;

    @LmlActor("cboEtc1Compression") SelectBox<String> cboEtc1Compression;
    @LmlActor("cboEtc2Compression") SelectBox<String> cboEtc2Compression;
    @LmlActor("chbUseCompression") VisCheckBox chbUseCompression;

    private EtcCompressionModel compressionModel;
    
	String[] etc1Options = { "None", "-etc1", "-etc1a" };
	String[] etc2Attr = { "None", "-RGB8", "-SRGB8", "-RGBA8", "-RGB8A1", "-SRGB8A1", "-R11" };

    @LmlAfter
    public void initialize() {
        compressionModel = null;
        cboEtc1Compression.setItems();
        cboEtc1Compression.setItems(etc1Options);
        cboEtc2Compression.setItems(etc2Attr);

        compressionModel = obtainCompressionModel();
        if (compressionModel == null) return;

        updateValuesFromModel();
    }

    @LmlAction("onEtc1CboChanged") void onEtc1CboChanged(VisSelectBox<String> selectBox) {
        if (compressionModel == null) { return; }

    	String value = selectBox.getSelected();
    	if(value.equalsIgnoreCase("None"))
    		compressionModel.setEtc1Comp(null);
    	else compressionModel.setEtc1Comp(value);
    }
    
    @LmlAction("onEtc2CboChanged") void onEtc2CboChanged(VisSelectBox<String> selectBox) {
        if (compressionModel == null) { return; }

    	String value = (String) selectBox.getSelected();
    	if(value.equalsIgnoreCase("None"))
    		compressionModel.setEtc2Comp(null);
    	else compressionModel.setEtc2Comp(value);
    }

    @LmlAction("onUseCompressionChanged") void onUseCompressionChanged() {
        if (compressionModel == null) { return; }

        boolean useCompression = chbUseCompression.isChecked();
        compressionModel.setCompressed(useCompression);
    }

	private void updateValuesFromModel() {
		cboEtc1Compression.setSelected(compressionModel.getEtc1Comp());
		cboEtc2Compression.setSelected(compressionModel.getEtc2Comp());
		chbUseCompression.setChecked(compressionModel.isCompressed());
	}

    private EtcCompressionModel obtainCompressionModel() {
//        ProjectModel project = modelService.getProject();
//        EtcCompressionModel etcCompression = project.getEtcCompression();
//
//        if (etcCompression == null || etcCompression.getType() != EtcCompressionType.KTX) {
//            Gdx.app.error(TAG, "Dialog was created while model holds different compression type");
//            return null;
//        }
//        return project.getEtcCompression();

        return null;
    }
}

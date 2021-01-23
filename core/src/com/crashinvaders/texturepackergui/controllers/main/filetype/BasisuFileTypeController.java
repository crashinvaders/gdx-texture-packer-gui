package com.crashinvaders.texturepackergui.controllers.main.filetype;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.filetype.BasisuFileTypeModel;
import com.crashinvaders.texturepackergui.events.FileTypePropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.ProjectInitializedEvent;
import com.crashinvaders.texturepackergui.views.seekbar.IntSeekBarModel;
import com.crashinvaders.texturepackergui.views.seekbar.SeekBar;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewActionContainer;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.VisSelectBox;

@Component
@ViewActionContainer("ftcBasisu")
public class BasisuFileTypeController implements FileTypeController {

    @Inject InterfaceService interfaceService;
    @Inject ModelService modelService;

    @LmlActor("ftcBasisu") ShrinkContainer container;
    @LmlActor("cboBasisInterFormat") VisSelectBox<IntermediateFormat> cboInterFormat;
    @LmlActor("sbBasisQualityLevel") SeekBar sbQualityLevel;
    @LmlActor("sbBasisCompLevel") SeekBar sbCompLevel;

    private BasisuFileTypeModel model;
    private boolean ignoreViewChangeEvents = false;

    @Override
    public void onViewCreated(Stage stage) {
        ignoreViewChangeEvents = true;
        cboInterFormat.setItems(IntermediateFormat.values());
        ignoreViewChangeEvents = false;
    }

    @Override
    public void activate() {
        model = modelService.getProject().getFileType();
        container.setVisible(true);

        updateInterFormat();
        updateQualityLevel();
        updateCompressionLevel();
    }

    @Override
    public void deactivate() {
        model = null;
        container.setVisible(false);
    }

    @OnEvent(ProjectInitializedEvent.class) void onEvent(ProjectInitializedEvent event) {
        updateInterFormat();
        updateQualityLevel();
        updateCompressionLevel();
    }

    @OnEvent(FileTypePropertyChangedEvent.class) void onEvent(FileTypePropertyChangedEvent event) {
        switch (event.getProperty()) {
            case BASIS_UASTC:
                updateInterFormat();
                break;
            case BASIS_QUALITY_LEVEL:
                updateQualityLevel();
                break;
            case BASIS_COMPRESSION_LEVEL:
                updateCompressionLevel();
                break;
        }
    }

    @LmlAction("onInterFormatChanged") void onInterFormatChanged() {
        if (model == null) return;
        if (ignoreViewChangeEvents) return;

        IntermediateFormat format = cboInterFormat.getSelected();
        model.setUastc(format == IntermediateFormat.UASTC);
    }

    @LmlAction("onQualityLevelChanged") void onQualityLevelChanged() {
        if (model == null) return;
        if (ignoreViewChangeEvents) return;

        int qualityLevel = ((IntSeekBarModel) sbQualityLevel.getModel()).getValue();
        model.setQualityLevel(qualityLevel);
    }

    @LmlAction("onCompLevelChanged") void onCompLevelChanged() {
        if (model == null) return;
        if (ignoreViewChangeEvents) return;

        int compLevel = ((IntSeekBarModel) sbCompLevel.getModel()).getValue();
        model.setCompressionLevel(compLevel);
    }

    @LmlAction("showBasisuInfo") void showBasisuInfo() {
        //TODO Show a popup with general information.
    }

    private void updateInterFormat() {
        if (model == null) return;

        IntermediateFormat format = model.isUastc() ? IntermediateFormat.UASTC : IntermediateFormat.ETC1S;
        cboInterFormat.setSelected(format);
    }

    private void updateQualityLevel() {
        if (model == null) return;

        int qualityLevel = model.getQualityLevel();
        ((IntSeekBarModel) sbQualityLevel.getModel()).setValue(qualityLevel);
    }

    private void updateCompressionLevel() {
        if (model == null) return;

        int compLevel = model.getCompressionLevel();
        ((IntSeekBarModel) sbCompLevel.getModel()).setValue(compLevel);
    }

    public enum IntermediateFormat {
        ETC1S, UASTC
    }
}

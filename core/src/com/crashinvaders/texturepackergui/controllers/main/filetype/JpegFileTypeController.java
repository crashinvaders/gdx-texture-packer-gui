package com.crashinvaders.texturepackergui.controllers.main.filetype;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.crashinvaders.texturepackergui.events.FileTypePropertyChangedEvent;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.crashinvaders.texturepackergui.services.model.filetype.JpegFileTypeModel;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewActionContainer;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.spinner.FloatSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

import java.math.BigDecimal;

@Component
@ViewActionContainer("ftcJpeg")
public class JpegFileTypeController implements FileTypeController {

    @Inject InterfaceService interfaceService;
    @Inject ModelService modelService;

    @LmlActor("ftcJpeg") ShrinkContainer container;
    @LmlActor("cboJpegEncoding") VisSelectBox<Pixmap.Format> cboEncoding;
    @LmlActor("spnJpegQuality1") Spinner spnQuality;

    private JpegFileTypeModel model;

    @Override
    public void onViewCreated(Stage stage) {
        cboEncoding.setItems(TexturePacker.availableEncodings);
    }

    @Override
    public void activate() {
        model = ((JpegFileTypeModel) modelService.getProject().getFileType());
        container.setVisible(true);

        updateEncoding();
        updateQuality();
    }

    @Override
    public void deactivate() {
        model = null;
        container.setVisible(false);
    }

    @OnEvent(FileTypePropertyChangedEvent.class) void onEvent(FileTypePropertyChangedEvent event) {
        switch (event.getProperty()) {
            case JPEG_ENCODING:
                updateEncoding();
                break;
            case JPEG_QUALITY:
                updateQuality();
                break;
        }
    }

    @LmlAction("onEncodingChanged") void onEncodingChanged() {
        if (model == null) return;

        Pixmap.Format encoding = cboEncoding.getSelected();
        model.setEncoding(encoding);
    }

    @LmlAction("onQualityChanged") void onQualityChanged() {
        if (model == null) return;

        float quality = ((FloatSpinnerModel) spnQuality.getModel()).getValue().floatValue();
        model.setQuality(quality);
    }

    private void updateEncoding() {
        if (model == null) return;

        Pixmap.Format encoding = model.getEncoding();
        cboEncoding.setSelected(encoding);
    }

    private void updateQuality() {
        if (model == null) return;

        float quality = model.getQuality();
        ((FloatSpinnerModel) spnQuality.getModel()).setValue(BigDecimal.valueOf(quality), false);
    }
}

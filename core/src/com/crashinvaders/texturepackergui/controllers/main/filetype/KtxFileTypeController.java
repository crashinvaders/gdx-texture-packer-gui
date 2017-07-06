package com.crashinvaders.texturepackergui.controllers.main.filetype;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.crashinvaders.texturepackergui.events.FileTypePropertyChangedEvent;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.crashinvaders.texturepackergui.services.model.filetype.KtxFileTypeModel;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewActionContainer;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisSelectBox;

@Component
@ViewActionContainer("ftcKtx")
public class KtxFileTypeController implements FileTypeController {

    @Inject InterfaceService interfaceService;
    @Inject ModelService modelService;

    @LmlActor("ftcKtx") ShrinkContainer container;
    @LmlActor("cboKtxFormat") VisSelectBox<KtxFileTypeModel.Format> cboFormat;
    @LmlActor("cboKtxEncoding") VisSelectBox cboEncoding;
    @LmlActor("chbKtxZipping") VisCheckBox chbZipping;

    private KtxFileTypeModel model;
    private boolean muteEncodingChangeEvent;

    @Override
    public void onViewCreated(Stage stage) {
        cboFormat.setItems(KtxFileTypeModel.Format.values());
    }

    @Override
    public void activate() {
        model = ((KtxFileTypeModel) modelService.getProject().getFileType());
        container.setVisible(true);

        updateFormat();
        updateEncoding();
        updateZipping();
    }

    @Override
    public void deactivate() {
        model = null;
        container.setVisible(false);
    }

    @OnEvent(FileTypePropertyChangedEvent.class) void onEvent(FileTypePropertyChangedEvent event) {
        switch (event.getProperty()) {
            case KTX_FORMAT:
                updateFormat();
                break;
            case KTX_ENCODING:
                updateEncoding();
                break;
            case KTX_ZIPPING:
                updateZipping();
                break;
        }
    }

    @LmlAction("onFormatChanged") void onFormatChanged() {
        if (model == null) return;

        KtxFileTypeModel.Format format = cboFormat.getSelected();
        model.setFormat(format);

        // Update encoding spinner values
        {
            muteEncodingChangeEvent = true;
            switch (format) {
                case ETC1:
                    cboEncoding.setItems(KtxFileTypeModel.EncodingETC1.values());
                    break;
                case ETC2:
                    cboEncoding.setItems(KtxFileTypeModel.EncodingETC2.values());
                    break;
            }
            updateEncoding();
            muteEncodingChangeEvent = false;
        }
    }

    @LmlAction("onEncodingChanged") void onEncodingChanged() {
        if (model == null) return;
        if (muteEncodingChangeEvent) return;

        Object encoding = cboEncoding.getSelected();
        switch (model.getFormat()) {
            case ETC1:
                model.setEncodingEtc1((KtxFileTypeModel.EncodingETC1) encoding);
                break;
            case ETC2:
                model.setEncodingEtc2((KtxFileTypeModel.EncodingETC2) encoding);
                break;
        }
    }

    @LmlAction("onZippingChanged") void onZippingChanged() {
        if (model == null) return;

        boolean zipping = chbZipping.isChecked();
        model.setZipping(zipping);
    }

    private void updateFormat() {
        if (model == null) return;

        KtxFileTypeModel.Format format = model.getFormat();
        cboFormat.setSelected(format);
    }

    private void updateEncoding() {
        if (model == null) return;

        switch (model.getFormat()) {
            case ETC1:
                cboEncoding.setSelected(model.getEncodingEtc1());
                break;
            case ETC2:
                cboEncoding.setSelected(model.getEncodingEtc2());
                break;
        }
    }

    private void updateZipping() {
        if (model == null) return;

        boolean zipping = model.isZipping();
        chbZipping.setChecked(zipping);
    }
}

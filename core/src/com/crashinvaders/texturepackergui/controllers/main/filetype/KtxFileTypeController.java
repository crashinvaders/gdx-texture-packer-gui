package com.crashinvaders.texturepackergui.controllers.main.filetype;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.crashinvaders.texturepackergui.events.FileTypePropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.ProjectInitializedEvent;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.filetype.KtxFileTypeModel;
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
    @LmlActor("cboKtxEncoding") VisSelectBox<Object> cboEncoding;
    @LmlActor("chbKtxZipping") VisCheckBox chbZipping;

    private KtxFileTypeModel model;
    private boolean ignoreViewChangeEvents = false;
    private boolean ignoreEncodingChangeEvent = false;

    @Override
    public void onViewCreated(Stage stage) {
        ignoreViewChangeEvents = true;
        cboFormat.setItems(KtxFileTypeModel.Format.values());
        ignoreViewChangeEvents = false;
    }

    @Override
    public void activate() {
        model = modelService.getProject().getFileType();
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

    @OnEvent(ProjectInitializedEvent.class) void onEvent(ProjectInitializedEvent event) {
        updateFormat();
        updateEncoding();
        updateZipping();
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
        if (ignoreViewChangeEvents) return;

        KtxFileTypeModel.Format format = cboFormat.getSelected();
        model.setFormat(format);
    }

    @LmlAction("onEncodingChanged") void onEncodingChanged() {
        if (model == null) return;
        if (ignoreViewChangeEvents) return;
        if (ignoreEncodingChangeEvent) return;

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

        ignoreEncodingChangeEvent = true;
        switch (format) {
            case ETC1:
                cboEncoding.setItems((Object[])KtxFileTypeModel.EncodingETC1.values());
                cboEncoding.setSelected(model.getEncodingEtc1());
                break;
            case ETC2:
                cboEncoding.setItems((Object[])KtxFileTypeModel.EncodingETC2.values());
                cboEncoding.setSelected(model.getEncodingEtc2());
                break;
        }
        ignoreEncodingChangeEvent = false;
    }

    private void updateEncoding() {
        if (model == null) return;

        ignoreEncodingChangeEvent = true;
        switch (model.getFormat()) {
            case ETC1:
                cboEncoding.setSelected(model.getEncodingEtc1());
                break;
            case ETC2:
                cboEncoding.setSelected(model.getEncodingEtc2());
                break;
        }
        ignoreEncodingChangeEvent = false;
    }

    private void updateZipping() {
        if (model == null) return;

        boolean zipping = model.isZipping();
        chbZipping.setChecked(zipping);
    }
}

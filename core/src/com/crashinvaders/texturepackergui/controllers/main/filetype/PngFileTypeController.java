package com.crashinvaders.texturepackergui.controllers.main.filetype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.crashinvaders.texturepackergui.controllers.Png8CompDialogController;
import com.crashinvaders.texturepackergui.controllers.PngtasticCompDialogController;
import com.crashinvaders.texturepackergui.controllers.TinifyCompDialogController;
import com.crashinvaders.texturepackergui.controllers.ZopfliCompDialogController;
import com.crashinvaders.texturepackergui.controllers.main.WidgetData;
import com.crashinvaders.texturepackergui.controllers.model.compression.*;
import com.crashinvaders.texturepackergui.events.FileTypePropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.ProjectInitializedEvent;
import com.crashinvaders.texturepackergui.controllers.model.ModelService;
import com.crashinvaders.texturepackergui.controllers.model.PngCompressionType;
import com.crashinvaders.texturepackergui.controllers.model.filetype.PngFileTypeModel;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewActionContainer;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.VisSelectBox;

@Component
@ViewActionContainer("ftcPng")
public class PngFileTypeController implements FileTypeController {
    private static final String TAG = PngFileTypeController.class.getSimpleName();

    @Inject InterfaceService interfaceService;
    @Inject ModelService modelService;

    @LmlActor("ftcPng") ShrinkContainer container;
    @LmlActor("cboPngEncoding") VisSelectBox<Pixmap.Format> cboEncoding;
    @LmlActor("cboPngCompression") VisSelectBox<WidgetData.PngCompression> cboCompression;
    @LmlActor("containerPngCompSettings") Actor containerPngCompSettings;

    private PngFileTypeModel model;

    @Override
    public void onViewCreated(Stage stage) {
        cboEncoding.setItems(TexturePacker.availableEncodings);
        cboCompression.setItems(WidgetData.PngCompression.values());
    }

    @Override
    public void activate() {
        model = modelService.getProject().getFileType();
        container.setVisible(true);

        updateEncoding();
        updateCompression();
    }

    @Override
    public void deactivate() {
        model = null;
        container.setVisible(false);
    }

    @OnEvent(ProjectInitializedEvent.class) void onEvent(ProjectInitializedEvent event) {
        updateEncoding();
        updateCompression();
    }

    @OnEvent(FileTypePropertyChangedEvent.class) void onEvent(FileTypePropertyChangedEvent event) {
        switch (event.getProperty()) {
            case PNG_ENCODING:
                updateEncoding();
                break;
            case PNG_COMPRESSION:
                updateCompression();
                break;
        }
    }

    @LmlAction("onEncodingChanged") void onEncodingChanged() {
        if (model == null) return;

        Pixmap.Format encoding = cboEncoding.getSelected();
        model.setEncoding(encoding);
    }

    @LmlAction("onCompressionChanged") void onCompressionChanged() {
        if (model == null) return;

        PngCompressionType compType = cboCompression.getSelected().type;

        if (compType == null) {
            model.setCompression(null);
            return;
        }

        if (model.getCompression() == null || compType != model.getCompression().getType()) {
            switch (compType) {
                case PNGTASTIC:
                    model.setCompression(new PngtasticCompressionModel());
                    break;
                case ZOPFLI:
                    model.setCompression(new ZopfliCompressionModel());
                    break;
                case TINY_PNG:
                    model.setCompression(new TinyPngCompressionModel());
                    break;
                case PALETTE:
                    model.setCompression(new Png8CompressionModel());
                    break;
                default:
                    Gdx.app.error(TAG, "Unexpected compression type: " + compType);
                    model.setCompression(null);
            }
        }
    }

    @LmlAction("showPngCompSettings") public void showPngCompSettings() {
        if (model == null) return;

        PngCompressionModel compression = model.getCompression();
        if (compression == null) return;

        switch (compression.getType()) {
            case PNGTASTIC:
                interfaceService.showDialog(PngtasticCompDialogController.class);
                break;
            case ZOPFLI:
                interfaceService.showDialog(ZopfliCompDialogController.class);
                break;
            case TINY_PNG:
                interfaceService.showDialog(TinifyCompDialogController.class);
                break;
            case PALETTE:
                interfaceService.showDialog(Png8CompDialogController.class);
                break;
            default:
                Gdx.app.error(TAG, "Unexpected PngCompressionType: " + compression.getType(), new IllegalStateException());
        }
    }

    private void updateEncoding() {
        if (model == null) return;

        cboEncoding.setSelected(model.getEncoding());
    }

    private void updateCompression() {
        if (model == null) return;

		PngCompressionModel compModel = model.getCompression();
		WidgetData.PngCompression compValue = WidgetData.PngCompression.valueOf(compModel == null ? null : compModel.getType());
        cboCompression.setSelected(compValue);
        containerPngCompSettings.setVisible(compValue.hasSettings);
    }
}

package com.crashinvaders.texturepackergui.controllers.main.inputfiles;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.crashinvaders.texturepackergui.config.attributes.KeyboardFocusChangedLmlAttribute;
import com.crashinvaders.texturepackergui.config.filechooser.AppIconProvider;
import com.crashinvaders.texturepackergui.events.InputFilePropertyChangedEvent;
import com.crashinvaders.texturepackergui.services.model.InputFile;
import com.crashinvaders.texturepackergui.services.model.ModelService;
import com.crashinvaders.texturepackergui.services.model.ModelUtils;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.utils.FileUtils;
import com.crashinvaders.texturepackergui.utils.Scene2dUtils;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.Tooltip;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;

@ViewDialog(id = "InputFilePropertiesDialogController", value = "lml/dialogInputFileProperties.lml")
public class InputFilePropertiesDialogController implements ActionContainer {
    private static final String TAG = InputFilePropertiesDialogController.class.getSimpleName();

    @Inject InterfaceService interfaceService;
    @Inject LocaleService localeService;
    @Inject ModelService modelService;
    @Inject ModelUtils modelUtils;

    @ViewStage Stage stage;

    @LmlActor("window") VisDialog dialog;
    @LmlActor("lblFilePath") Label lblFilePath;
    @LmlActor("edtFilePrefix") VisTextField edtFilePrefix;
    @LmlActor("edtRegionName") VisTextField edtRegionName;
    @LmlActor("shrinkInputDir") ShrinkContainer shrinkInputDir;
    @LmlActor("shrinkInputFile") ShrinkContainer shrinkInputFile;
    private Tooltip tooltip;

    private InputFile inputFile;

    @LmlAfter void init() {
        tooltip = new Tooltip();
        tooltip.setAppearDelayTime(0.25f);

        mapDataFromModel();
    }

    @OnEvent(InputFilePropertyChangedEvent.class) void onEvent(InputFilePropertyChangedEvent event) {
        if (event.getInputFile() != inputFile) return;

        mapDataFromModel();
    }

    @LmlAction("showFilePicker") void showFilePicker() {
        final FileChooser fileChooser = new FileChooser(inputFile.getFileHandle().parent(), FileChooser.Mode.OPEN);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
        if (inputFile.isDirectory()) {
            fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);
        }  else {
            fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
            fileChooser.setFileTypeFilter(new FileUtils.FileTypeFilterBuilder(true)
                    .rule("Image files", "png", "jpg", "jpeg").get()); //TODO localize
        }
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> files) {
                FileHandle file = files.first();
                if (file.equals(inputFile.getFileHandle())) return;

                InputFile newInputFile = modelUtils.changeInputFileHandle(modelService.getProject().getSelectedPack(), InputFilePropertiesDialogController.this.inputFile, file);

                if (newInputFile != null) {
                    InputFilePropertiesDialogController.this.inputFile = newInputFile;
                    mapDataFromModel();
                }
            }
        });
        stage.addActor(fileChooser.fadeIn());

        if (FileUtils.fileExists(inputFile.getFileHandle())) { fileChooser.setSelectedFiles(inputFile.getFileHandle()); }
    }

    @LmlAction("close") public void close() {
        dialog.hide();
    }

    @LmlAction("onFilePrefixFocusChanged")
    void onFilePrefixFocusChanged(KeyboardFocusChangedLmlAttribute.Params params) {
        if (!params.focused) {
            inputFile.setDirFilePrefix(edtFilePrefix.getText().trim());
        }
    }

    @LmlAction("onRegionNameFocusChanged")
    void onRegionNameFocusChanged(KeyboardFocusChangedLmlAttribute.Params params) {
        if (!params.focused) {
            inputFile.setRegionName(edtRegionName.getText().trim());
        }
    }

    public void show(InputFile inputFile) {
        setInputFile(inputFile);
        interfaceService.showDialog(this.getClass());
    }

    public void setInputFile(InputFile inputFile) {
        this.inputFile = inputFile;
    }

    private void mapDataFromModel() {
        dialog.pack();

        boolean fileShortened = false;
        String origFilePath = inputFile.getFileHandle().path();
        String filePath = origFilePath;

        filePath = Scene2dUtils.ellipsisFilePath(filePath, lblFilePath.getStyle().font, lblFilePath.getWidth());
        fileShortened = !origFilePath.equals(filePath);
        filePath = Scene2dUtils.colorizeFilePath(filePath, inputFile.getFileHandle().isDirectory(), "light-grey", "white");

        lblFilePath.setText(filePath);

        // Show tooltip only if displayed file name was shortened
        tooltip.setTarget(fileShortened ? lblFilePath : null);
        tooltip.setText(Scene2dUtils.colorizeFilePath(origFilePath, inputFile.getFileHandle().isDirectory(), "light-grey", "white"));

        shrinkInputDir.setVisible(false);
        shrinkInputFile.setVisible(false);

        // Input directory data
        if (inputFile.isDirectory() && inputFile.getType() == InputFile.Type.Input) {
            dialog.getTitleLabel().setText(localeService.getI18nBundle().get("dInputFileTitleDir"));
            shrinkInputDir.setVisible(true);
            edtFilePrefix.setText(inputFile.getDirFilePrefix());
        }

        // Input file data
        if (!inputFile.isDirectory() && inputFile.getType() == InputFile.Type.Input) {
            dialog.getTitleLabel().setText(localeService.getI18nBundle().get("dInputFileTitleFile"));
            shrinkInputFile.setVisible(true);
            edtRegionName.setMessageText(inputFile.getFileHandle().nameWithoutExtension());
            edtRegionName.setText(inputFile.getRegionName());
        }

        // Ignored file data
        if (inputFile.getType() == InputFile.Type.Ignore) {
            dialog.getTitleLabel().setText(localeService.getI18nBundle().get("dInputFileTitleFileIgnore"));
        }

    }
}

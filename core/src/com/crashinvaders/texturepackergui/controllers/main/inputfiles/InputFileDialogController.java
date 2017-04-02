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
import com.crashinvaders.texturepackergui.utils.FileUtils;
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
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;

@ViewDialog(id = "InputFileDialogController", value = "lml/dialogInputFile.lml")
public class InputFileDialogController implements ActionContainer {
    private static final String TAG = InputFileDialogController.class.getSimpleName();

    @Inject InterfaceService interfaceService;
    @Inject LocaleService localeService;
    @Inject ModelService modelService;

    @ViewStage Stage stage;

    @LmlActor("window") VisDialog dialog;
    @LmlActor("lblFilePath") Label lblFilePath;
    @LmlActor("edtFilePrefix") VisTextField edtFilePrefix;
    @LmlActor("edtRegionName") VisTextField edtRegionName;
    @LmlActor("shrinkInputDir") ShrinkContainer shrinkInputDir;
    @LmlActor("shrinkInputFile") ShrinkContainer shrinkInputFile;

    private InputFile inputFile;

    @LmlAfter void init() {
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
                inputFile.setFileHandle(files.first());
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
        lblFilePath.setText(inputFile.getFileHandle().path());

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

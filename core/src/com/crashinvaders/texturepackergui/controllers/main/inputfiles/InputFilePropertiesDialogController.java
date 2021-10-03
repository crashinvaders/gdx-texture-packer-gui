package com.crashinvaders.texturepackergui.controllers.main.inputfiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.common.scene2d.Scene2dUtils;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.crashinvaders.texturepackergui.controllers.ErrorDialogController;
import com.crashinvaders.texturepackergui.controllers.model.*;
import com.crashinvaders.texturepackergui.controllers.ninepatcheditor.NinePatchEditorDialog;
import com.crashinvaders.texturepackergui.controllers.ninepatcheditor.NinePatchEditorModel;
import com.crashinvaders.texturepackergui.events.InputFilePropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.PackPropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.ShowToastEvent;
import com.crashinvaders.texturepackergui.utils.AppIconProvider;
import com.crashinvaders.texturepackergui.utils.FileUtils;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.Tooltip;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;

@ViewDialog(id = "InputFilePropertiesDialogController", value = "lml/dialogInputFileProperties.lml")
public class InputFilePropertiesDialogController implements ActionContainer {
    private static final String TAG = InputFilePropertiesDialogController.class.getSimpleName();

    @Inject InterfaceService interfaceService;
    @Inject LocaleService localeService;
    @Inject EventDispatcher eventDispatcher;
    @Inject ModelService modelService;
    @Inject ModelUtils modelUtils;
    @Inject NinePatchEditorDialog ninePatchEditorDialog;
    @Inject ErrorDialogController errorDialogController;

    @ViewStage Stage stage;

    @LmlActor("window") VisDialog dialog;
    @LmlActor("lblFilePath") Label lblFilePath;
    @LmlActor("shrinkInputDir") ShrinkContainer shrinkInputDir;
    @LmlActor("shrinkInputFile") ShrinkContainer shrinkInputFile;
    // Directory properties
    @LmlActor("edtFilePrefix") VisTextField edtFilePrefix;
    @LmlActor("flattenPathContainer") Group flattenPathContainer;
    @LmlActor("chbRecursive") VisCheckBox chbRecursive;
    @LmlActor("chbFlattenPaths") VisCheckBox chbFlattenPaths;
    // File properties
    @LmlActor("edtRegionName") VisTextField edtRegionName;
    @LmlActor("chbNinePatch") VisCheckBox chbNinePatch;
    @LmlActor("btnEditNinePatch") Button btnEditNinePatch;

    private Tooltip tooltip;
    private InputFile inputFile;

    private Color colorTextValid;
    private Color colorTextInvalid;

    private boolean ignoreInputFileUpdateEvents = false;

    @LmlAfter void init() {
        tooltip = new Tooltip();
        tooltip.setAppearDelayTime(0.25f);

        colorTextValid = interfaceService.getSkin().getColor("white");
        colorTextInvalid = interfaceService.getSkin().getColor("orange");

        mapDataFromModel();
    }

    @OnEvent(InputFilePropertyChangedEvent.class) void onEvent(InputFilePropertyChangedEvent event) {
        if (!isShown() || event.getInputFile() != inputFile || ignoreInputFileUpdateEvents) return;

        mapDataFromModel();
    }

    @OnEvent(PackPropertyChangedEvent.class) void onEvent(PackPropertyChangedEvent event) {
        if (!isShown() || inputFile == null) return;

        switch (event.getProperty()) {
            case KEEP_FILE_EXTENSIONS:
                mapDataFromModel();
                break;
        }
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

    @LmlAction("onFilePrefixTextChanged") void onFilePrefixFocusChanged() {
        inputFile.setDirFilePrefix(edtFilePrefix.getText().trim());
    }

    @LmlAction("onRecursiveChecked") void onRecursiveChecked() {
        boolean checked = chbRecursive.isChecked();
        inputFile.setRecursive(checked);
        flattenPathContainer.setVisible(checked);
    }

    @LmlAction("onFlattenPathsChecked") void onFlattenPathsChecked() {
        boolean checked = chbFlattenPaths.isChecked();
        inputFile.setFlattenPaths(checked);
    }

    @LmlAction("onRegionNameTextChanged") void onRegionNameTextChanged() {
        inputFile.setRegionName(edtRegionName.getText().trim());
    }

    @LmlAction("onNinePatchChecked") void onNinePatchChecked() {
        boolean checked = chbNinePatch.isChecked();
        btnEditNinePatch.setVisible(checked);

        // Apply model logic only for a file based nine patch
        if (!inputFile.isFileBasedNinePatch()) {
            inputFile.setProgrammaticNinePatch(checked);
        }

        // Open 9patch editor immediately
        if (checked) {
            navigateToNinePatchEditor();
        }
    }

    @LmlAction("navigateToNinePatchEditor") void navigateToNinePatchEditor() {
        if (!checkFileExists(inputFile, true)) return;

        ninePatchEditorDialog.setImageFile(inputFile.getFileHandle());

        if (inputFile.isProgrammaticNinePatch()) {
            ninePatchEditorDialog.getModel().loadFromInputFile(inputFile);
            ninePatchEditorDialog.setResultListener(new NinePatchEditorDialog.ResultListener() {
                @Override
                public void onResult(NinePatchEditorModel model) {
                    // Update input file model
                    model.saveToInputFile(inputFile);
                }
            });
        }

        if (inputFile.isFileBasedNinePatch()) {
            ninePatchEditorDialog.setResultListener(new NinePatchEditorDialog.ResultListener() {
                @Override
                public void onResult(NinePatchEditorModel model) {
                    // Rewrite image file
                    Pixmap pixmap = model.prepareNinePatchPixmap();
                    try {
                        PixmapIO.writePNG(inputFile.getFileHandle(), pixmap);
                    } catch (Exception e) {
                        errorDialogController.setError(e);
                        interfaceService.showDialog(errorDialogController.getClass());
                    } finally {
                        pixmap.dispose();
                    }
                }
            });
        }

        interfaceService.showDialog(ninePatchEditorDialog.getClass());
    }

    public void show(InputFile inputFile) {
        if (!checkFileExists(inputFile, true)) {
            Gdx.app.error(TAG, "Input file doesn't exist: " + inputFile.getFileHandle().path());
        }

        setInputFile(inputFile);
        interfaceService.showDialog(this.getClass());
    }

    public void setInputFile(InputFile inputFile) {
        this.inputFile = inputFile;
    }

    private void mapDataFromModel() {
        final PackModel packModel = modelService.getProject().getSelectedPack();

        dialog.pack();

        boolean fileShortened = false;
        String origFilePath = inputFile.getFileHandle().path();
        String filePath = origFilePath;

        filePath = Scene2dUtils.ellipsisFilePath(filePath, lblFilePath.getStyle().font, lblFilePath.getWidth());
        fileShortened = !origFilePath.equals(filePath);
        filePath = Scene2dUtils.colorizeFilePath(filePath, inputFile.getFileHandle().isDirectory(), "light-grey", "white");

        lblFilePath.setText(filePath);
        lblFilePath.setColor(inputFile.getFileHandle().exists() ? colorTextValid : colorTextInvalid);

        // Show tooltip only if displayed file name was shortened
        tooltip.setTarget(fileShortened ? lblFilePath : null);
        tooltip.setText(Scene2dUtils.colorizeFilePath(origFilePath, inputFile.getFileHandle().isDirectory(), "light-grey", "white"));
        tooltip.setTouchable(Touchable.disabled);

        shrinkInputDir.setVisible(false);
        shrinkInputFile.setVisible(false);

        // Input directory data
        if (inputFile.isDirectory() && inputFile.getType() == InputFile.Type.Input) {
            dialog.getTitleLabel().setText(localeService.getI18nBundle().get("dInputFileTitleDir"));
            shrinkInputDir.setVisible(true);
            edtFilePrefix.setText(inputFile.getDirFilePrefix());

            chbRecursive.setChecked(inputFile.isRecursive());
            chbFlattenPaths.setChecked(inputFile.isFlattenPaths());
        }

        // Input file data
        if (!inputFile.isDirectory() && inputFile.getType() == InputFile.Type.Input) {
            dialog.getTitleLabel().setText(localeService.getI18nBundle().get("dInputFileTitleFile"));
            shrinkInputFile.setVisible(true);

            String defaultRegionName = InputFile.evalDefaultRegionName(
                    inputFile.getFileHandle(),
                    packModel.isKeepInputFileExtensions());

            edtRegionName.setMessageText(defaultRegionName);
            edtRegionName.setText(inputFile.getRegionName());

            chbNinePatch.setProgrammaticChangeEvents(false);
            if (inputFile.isFileBasedNinePatch()) {
                chbNinePatch.setDisabled(true);
                chbNinePatch.setChecked(true);
                btnEditNinePatch.setVisible(true);
            } else {
                chbNinePatch.setDisabled(false);
                chbNinePatch.setChecked(inputFile.isProgrammaticNinePatch());
                btnEditNinePatch.setVisible(inputFile.isProgrammaticNinePatch());
            }
            chbNinePatch.setProgrammaticChangeEvents(true);
        }

        // Ignored file data
        if (inputFile.getType() == InputFile.Type.Ignore) {
            dialog.getTitleLabel().setText(localeService.getI18nBundle().get("dInputFileTitleFileIgnore"));
        }
    }

    private boolean checkFileExists(InputFile inputFile, boolean showErrorToast) {
        boolean exists = inputFile.getFileHandle().exists();
        if (!exists && showErrorToast) {
            eventDispatcher.postEvent(new ShowToastEvent()
                    .message(localeService.getI18nBundle().format("toastInputFileDoesNotExist", inputFile.getFileHandle().path())));
        }
        return exists;
    }

    private boolean isShown() {
        return stage != null;
    }
}

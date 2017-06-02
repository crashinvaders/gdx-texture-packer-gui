package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.config.filechooser.AppIconProvider;
import com.crashinvaders.texturepackergui.controllers.ErrorDialogController;
import com.crashinvaders.texturepackergui.utils.FileUtils;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;

@Component
public class NinePatchToolController {
    private static final String TAG = NinePatchToolController.class.getSimpleName();

    @Inject InterfaceService interfaceService;
    @Inject ErrorDialogController errorDialogController;
    @Inject NinePatchEditorDialog ninePatchEditorDialog;

    private NinePatchEditorModel model;
    private FileHandle sourceFile;

    public void initiateFromFilePicker() {
        FileHandle rootDir = null;
        if (sourceFile != null) {
            rootDir = sourceFile.parent();
        }

        final FileChooser fileChooser = new FileChooser(rootDir, FileChooser.Mode.OPEN);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setFileTypeFilter(new FileUtils.FileTypeFilterBuilder(true)
                .rule("PNG or 9-Patch", "png", "9.png").get());
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> files) {
                FileHandle file = files.first();
                initiateWithSourceFile(file);
            }
        });
        Stage stage = interfaceService.getCurrentController().getStage();
        stage.addActor(fileChooser.fadeIn());

        if (sourceFile != null && FileUtils.fileExists(sourceFile)) {
            fileChooser.setSelectedFiles(sourceFile);
        }
    }

    public void initiateWithSourceFile(final FileHandle sourceFile) {
        this.sourceFile = sourceFile;
        try {
            model = new NinePatchEditorModel(sourceFile);
        } catch (Exception e) {
            showError(e);
            return;
        }

        ninePatchEditorDialog.setImageFile(sourceFile);
        ninePatchEditorDialog.setResultListener(new NinePatchEditorDialog.ResultListener() {
            @Override
            public void onResult(NinePatchEditorModel model) {
                saveAsNinePatch(sourceFile, model);
            }
        });
        interfaceService.showDialog(ninePatchEditorDialog.getClass());
    }

    private void saveAsNinePatch(final FileHandle sourceFile, final NinePatchEditorModel model) {
        // Model will be disposed at the moment of the file chooser's callback, so we cache the result.
        final Pixmap ninePatchPixmap = model.prepareNinePatchPixmap();

        final FileChooser fileChooser = new FileChooser(sourceFile.parent(), FileChooser.Mode.SAVE);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setFileTypeFilter(new FileUtils.FileTypeFilterBuilder(true)
                .rule("PNG or 9-Patch", "png", "9.png").get());
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> files) {
                FileHandle file = files.first();

                if (file.name().endsWith(".9")) {
                    String path = file.path() + ".png";
                    file = Gdx.files.getFileHandle(path, file.type());
                }
                if (!file.name().endsWith(".9.png")) {
                    String path = file.pathWithoutExtension() + ".9.png";
                    file = Gdx.files.getFileHandle(path, file.type());
                }

                PixmapIO.writePNG(file, ninePatchPixmap);
                ninePatchPixmap.dispose();
            }

            @Override
            public void canceled() {
                ninePatchPixmap.dispose();
            }
        });
        Stage stage = interfaceService.getCurrentController().getStage();
        stage.addActor(fileChooser.fadeIn());

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                fileChooser.setSelectedFiles(sourceFile);
            }
        });
    }

    private void showError(final Exception exception) {
        Gdx.app.error(TAG, "", exception);
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                // Show error dialog
                errorDialogController.setError(exception);
                interfaceService.showDialog(errorDialogController.getClass());
            }
        });
    }
}

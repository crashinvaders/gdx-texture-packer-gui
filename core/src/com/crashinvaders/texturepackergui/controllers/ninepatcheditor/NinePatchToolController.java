package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.controllers.ErrorDialogController;
import com.crashinvaders.texturepackergui.controllers.FileDialogService;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;

@Component
public class NinePatchToolController {
    private static final String TAG = NinePatchToolController.class.getSimpleName();

    public static final FileDialogService.FileFilter[] fileDialogFilterImages =
            FileDialogService.FileFilter.createSingle("PNG or 9-Patch", "png", "9.png");

    @Inject InterfaceService interfaceService;
    @Inject ErrorDialogController errorDialogController;
    @Inject NinePatchEditorDialog ninePatchEditorDialog;
    @Inject FileDialogService fileDialogService;

    private NinePatchEditorModel model;
    private FileHandle sourceFile;

    public void initiateFromFilePicker() {
        FileHandle initialSelection = null;
        if (sourceFile != null && sourceFile.exists()) {
            initialSelection = sourceFile;
        }

        fileDialogService.openFile(null, initialSelection, fileDialogFilterImages,
                new FileDialogService.CallbackAdapter() {
                    @Override
                    public void selected(Array<FileHandle> files) {
                        FileHandle file = files.first();
                        initiateWithSourceFile(file);
                    }
                });
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
        ninePatchEditorDialog.setResultListener(model ->
                saveAsNinePatch(sourceFile, model));
        interfaceService.showDialog(ninePatchEditorDialog.getClass());
    }

    private void saveAsNinePatch(final FileHandle sourceFile, final NinePatchEditorModel model) {
        // Model will be disposed at the moment of the file chooser's callback, so we cache the result.
        final Pixmap ninePatchPixmap = model.prepareNinePatchPixmap();

        FileHandle initialSelection = sourceFile;
        // If the ".png" file was used to create 9patch,
        // lets propose to output to a ".9.png" file with the same name.
        if (!initialSelection.name().endsWith(".9.png") &&
                initialSelection.name().endsWith(".png")) {
            String absolutePath = initialSelection.file().getAbsolutePath();
            absolutePath = absolutePath.substring(0, absolutePath.length() - 4); // Chop out the ".png" extension.
            absolutePath += ".9.png";
            initialSelection = Gdx.files.absolute(absolutePath);
        }

        fileDialogService.saveFile(null, initialSelection, fileDialogFilterImages,
                new FileDialogService.Callback() {
                    @Override
                    public void selected(Array<FileHandle> files) {
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

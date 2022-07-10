package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.crashinvaders.texturepackergui.utils.AppIconProvider;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserListener;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;

/**
 * File selection dialogs using VisUI {@link FileChooser}.
 */
public class DefaultFileDialogService implements FileDialogService {

    @Inject InterfaceService interfaceService;

    @Override
    public void pickDirectory(String dialogTitle, @Null FileHandle initialFile, Callback callback) {
        FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
        fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.getTitleLabel().setText(dialogTitle);
        fileChooser.setListener(new FileChooserListenerWrapper(callback));

        if (initialFile != null) {
            fileChooser.setDirectory(initialFile);
        }

        showDialog(fileChooser);
    }

    @Override
    public void openFile(String dialogTitle, @Null FileHandle initialFile, @Null FileFilter[] fileFilters, Callback callback) {
        FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.getTitleLabel().setText(dialogTitle);
        fileChooser.setFileTypeFilter(prepareFileFilter(fileFilters));
        fileChooser.setListener(new FileChooserListenerWrapper(callback));

        if (initialFile != null) {
            fileChooser.setDirectory(initialFile);
        }

        showDialog(fileChooser);
    }

    @Override
    public void openMultipleFiles(String dialogTitle, @Null FileHandle initialFile, @Null FileFilter[] fileFilters, Callback callback) {
        FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.getTitleLabel().setText(dialogTitle);
        fileChooser.setFileTypeFilter(prepareFileFilter(fileFilters));
        fileChooser.setListener(new FileChooserListenerWrapper(callback));

        if (initialFile != null) {
            fileChooser.setDirectory(initialFile);
        }

        showDialog(fileChooser);
    }

    @Override
    public void saveFile(String dialogTitle, @Null FileHandle initialFile, @Null FileFilter[] fileFilters, Callback callback) {
        FileChooser fileChooser = new FileChooser(FileChooser.Mode.SAVE);
        fileChooser.setIconProvider(new AppIconProvider(fileChooser));
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.getTitleLabel().setText(dialogTitle);
        fileChooser.setFileTypeFilter(prepareFileFilter(fileFilters));
        fileChooser.setListener(new FileChooserListenerWrapper(callback));

        if (initialFile != null) {
            fileChooser.setDirectory(initialFile);
        }

        showDialog(fileChooser);
    }

    private void showDialog(FileChooser fileChooser) {
        getStage().addActor(fileChooser.fadeIn());
    }

    private static @Null FileTypeFilter prepareFileFilter(@Null FileFilter[] filters) {
        if (filters == null)
            return null;

        FileTypeFilter result = new FileTypeFilter(true);
        for (FileFilter filter : filters) {
            result.addRule(filter.description, filter.extensions);
        }
        return result;
    }

    private Stage getStage() {
        return interfaceService.getCurrentController().getStage();
    }

    private static class FileChooserListenerWrapper implements FileChooserListener {
        private final FileDialogService.Callback callback;

        private FileChooserListenerWrapper(Callback callback) {
            this.callback = callback;
        }

        @Override
        public void selected(Array<FileHandle> files) {
            callback.selected(files);
        }

        @Override
        public void canceled() {
            callback.canceled();
        }
    }
}

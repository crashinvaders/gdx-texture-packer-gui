package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public interface FileDialogService {
    void pickDirectory(@Null String dialogTitle, @Null FileHandle initialFile, Callback callback);

    void openFile(@Null String dialogTitle, @Null FileHandle initialFile, @Null FileFilter[] fileFilters, Callback callback);

    void openMultipleFiles(@Null String dialogTitle, @Null FileHandle initialFile, @Null FileFilter[] fileFilters, Callback callback);

    void saveFile(@Null String dialogTitle, @Null FileHandle initialFile, @Null FileFilter[] fileFilters, Callback callback);

    interface Callback {
        void selected (Array<FileHandle> files);
        void canceled ();
    }

    class CallbackAdapter implements Callback {
        @Override
        public void selected(Array<FileHandle> files) { }
        @Override
        public void canceled() { }
    }

    final class FileFilter {
        public static final FileFilter[] none = new FileFilter[0];

        public final String description;
        public final String[] extensions;

        public FileFilter(String filterName, String... extensions) {
            this.description = filterName;
            this.extensions = extensions;
        }
    }
}

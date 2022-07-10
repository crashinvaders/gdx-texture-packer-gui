package com.crashinvaders.texturepackergui.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.StringBuilder;
import com.crashinvaders.texturepackergui.controllers.DefaultFileDialogService;
import com.crashinvaders.texturepackergui.controllers.FileDialogService;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.kiwi.util.common.Strings;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NFDPathSet;
import org.lwjgl.util.nfd.NativeFileDialog;

import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * File selection dialogs using LWJGL Native File Dialog (NFD) library.
 */
public class LwjglFileDialogService implements FileDialogService {

    @Inject DefaultFileDialogService defaultFileService;

    @Override
    public void pickDirectory(@Null String dialogTitle, @Null FileHandle initialFile, Callback callback) {
        @Null String initialPath = prepareInitialPath(initialFile);

        PointerBuffer pathPointer = MemoryUtil.memAllocPointer(1);

        try {
            int status = NativeFileDialog.NFD_PickFolder(initialPath, pathPointer);

            if (status == NativeFileDialog.NFD_CANCEL) {
                callback.canceled();
                return;
            }

            // Unexpected error.
            if (status != NativeFileDialog.NFD_OKAY) {
                String errorText = NativeFileDialog.NFD_GetError();
                throw new RuntimeException("Native file dialog error: " + errorText);
            }

            String selectedFolderPath = pathPointer.getStringUTF8(0);
            NativeFileDialog.nNFD_Free(pathPointer.get(0));

            Array<FileHandle> array = new Array<>();
            array.add(Gdx.files.absolute(selectedFolderPath));

            callback.selected(array);
        } catch (Throwable e) {
            defaultFileService.pickDirectory(dialogTitle, initialFile, callback);
        } finally {
            memFree(pathPointer);
        }
    }

    @Override
    public void openFile(@Null String dialogTitle, @Null FileHandle initialFile, @Null FileFilter[] fileFilters, Callback callback) {
        @Null String initialPath = prepareInitialPath(initialFile);
        @Null String filterList = prepareFilterList(fileFilters);

        PointerBuffer pathPointer = MemoryUtil.memAllocPointer(1);

        try {
            int status = NativeFileDialog.NFD_OpenDialog(filterList, initialPath, pathPointer);

            if (status == NativeFileDialog.NFD_CANCEL) {
                callback.canceled();
                return;
            }

            // Unexpected error.
            if (status != NativeFileDialog.NFD_OKAY) {
                String errorText = NativeFileDialog.NFD_GetError();
                throw new RuntimeException("Native file dialog error: " + errorText);
            }

            String selectedFilePath = pathPointer.getStringUTF8(0);
            NativeFileDialog.nNFD_Free(pathPointer.get(0));

            Array<FileHandle> array = new Array<>();
            array.add(Gdx.files.absolute(selectedFilePath));

            callback.selected(array);
        } catch (Throwable e) {
            defaultFileService.openFile(dialogTitle, initialFile, fileFilters, callback);
        } finally {
            memFree(pathPointer);
        }
    }

    @Override
    public void openMultipleFiles(@Null String dialogTitle, @Null FileHandle initialFile, @Null FileFilter[] fileFilters, Callback callback) {
        @Null String initialPath = prepareInitialPath(initialFile);
        @Null String filterList = prepareFilterList(fileFilters);

        NFDPathSet pathSet = NFDPathSet.create();

        try {
            int status = NativeFileDialog.NFD_OpenDialogMultiple(filterList, initialPath, pathSet);

            if (status == NativeFileDialog.NFD_CANCEL) {
                callback.canceled();
                return;
            }

            // Unexpected error.
            if (status != NativeFileDialog.NFD_OKAY) {
                String errorText = NativeFileDialog.NFD_GetError();
                throw new RuntimeException("Native file dialog error: " + errorText);
            }

            long selectedAmount = NativeFileDialog.NFD_PathSet_GetCount(pathSet);
            // If nothing is selected, consider the selection operation is canceled.
            if (selectedAmount == 0) {
                callback.canceled();
                return;
            }

            Array<FileHandle> array = new Array<>();
            for (long i = 0; i < selectedAmount; i++) {
                String selectedPath = NativeFileDialog.NFD_PathSet_GetPath(pathSet, i);
                array.add(Gdx.files.absolute(selectedPath));
            }

            callback.selected(array);
        } catch (Throwable e) {
            defaultFileService.openMultipleFiles(dialogTitle, initialFile, fileFilters, callback);
        } finally {
            NativeFileDialog.NFD_PathSet_Free(pathSet);
        }
    }

    @Override
    public void saveFile(@Null String dialogTitle, @Null FileHandle initialFile, @Null FileFilter[] fileFilters, Callback callback) {
        @Null String initialPath = prepareInitialPath(initialFile);
        @Null String filterList = prepareFilterList(fileFilters);

        PointerBuffer pathPointer = MemoryUtil.memAllocPointer(1);

        try {
            int status = NativeFileDialog.NFD_SaveDialog(filterList, initialPath, pathPointer);

            if (status == NativeFileDialog.NFD_CANCEL) {
                callback.canceled();
                return;
            }

            // Unexpected error.
            if (status != NativeFileDialog.NFD_OKAY) {
                String errorText = NativeFileDialog.NFD_GetError();
                throw new RuntimeException("Native file dialog error: " + errorText);
            }

            String selectedFilePath = pathPointer.getStringUTF8(0);
            NativeFileDialog.nNFD_Free(pathPointer.get(0));

            Array<FileHandle> array = new Array<>();
            array.add(Gdx.files.absolute(selectedFilePath));

            callback.selected(array);
        } catch (Throwable e) {
            defaultFileService.saveFile(dialogTitle, initialFile, fileFilters, callback);
        } finally {
            memFree(pathPointer);
        }
    }

    private static @Null String prepareInitialPath(@Null FileHandle fileHandle) {
        if (fileHandle == null)
            return null;

        return fileHandle.file().getAbsolutePath();
    }

    private static @Null String prepareFilterList(@Null FileFilter[] filters) {
        if (filters == null)
            return null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filters.length; i++) {
            FileFilter filter = filters[i];
            if (sb.length > 0) {
                sb.append(";");
            }
            sb.append(Strings.join(",", (Object[]) filter.extensions));
        }

        if (sb.length == 0)
            return null;

        return sb.toString();
    }
}

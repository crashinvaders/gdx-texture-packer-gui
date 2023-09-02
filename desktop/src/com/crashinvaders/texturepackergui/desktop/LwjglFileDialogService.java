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
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NFDFilterItem;
import org.lwjgl.util.nfd.NFDPathSetEnum;
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
            int status = NativeFileDialog.NFD_PickFolder(pathPointer, initialPath);

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
            NativeFileDialog.NFD_FreePath(pathPointer.get(0));

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
        try (MemoryStack stack = MemoryStack.stackPush()) {
            @Null String initialPath = prepareInitialPath(initialFile);
            @Null NFDFilterItem.Buffer filterList = prepareFilterList(stack, fileFilters);

            PointerBuffer pp = stack.mallocPointer(1);

            int status = NativeFileDialog.NFD_OpenDialog(pp, filterList, initialPath);

            if (status == NativeFileDialog.NFD_CANCEL) {
                callback.canceled();
                return;
            }

            // Unexpected error.
            if (status != NativeFileDialog.NFD_OKAY) {
                String errorText = NativeFileDialog.NFD_GetError();
                throw new RuntimeException("Native file dialog error: " + errorText);
            }

            String selectedFilePath = pp.getStringUTF8(0);
//                NativeFileDialog.NFD_FreePath(pathPointer.get(0));

            Array<FileHandle> array = new Array<>();
            array.add(Gdx.files.absolute(selectedFilePath));

            callback.selected(array);
        } catch (Throwable e) {
            defaultFileService.openFile(dialogTitle, initialFile, fileFilters, callback);
        }
    }

    @Override
    public void openMultipleFiles(@Null String dialogTitle, @Null FileHandle initialFile, @Null FileFilter[] fileFilters, Callback callback) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            @Null String initialPath = prepareInitialPath(initialFile);
            @Null NFDFilterItem.Buffer filterList = prepareFilterList(stack, fileFilters);

            PointerBuffer pp = stack.mallocPointer(1);

            int status = NativeFileDialog.NFD_OpenDialogMultiple(pp, filterList, initialPath);

            if (status == NativeFileDialog.NFD_CANCEL) {
                callback.canceled();
                return;
            }

            // Unexpected error.
            if (status != NativeFileDialog.NFD_OKAY) {
                String errorText = NativeFileDialog.NFD_GetError();
                throw new RuntimeException("Native file dialog error: " + errorText);
            }

//            long selectedAmount = pathAmountPointer.get(0);
//            // If nothing is selected, consider the selection operation is canceled.
//            if (selectedAmount == 0) {
//                callback.canceled();
//                return;
//            }

//            Array<FileHandle> array = new Array<>();
//            for (long i = 0; i < selectedAmount; i++) {
//                String selectedPath = NativeFileDialog.NFD_PathSet_GetPath(pathSet, i);
//                array.add(Gdx.files.absolute(selectedPath));
//            }

            long pathSet = pp.get(0);
            NFDPathSetEnum psEnum = NFDPathSetEnum.calloc(stack);
            NativeFileDialog.NFD_PathSet_GetEnum(pathSet, psEnum);

            Array<FileHandle> array = new Array<>();
            int i = 0;
            while (NativeFileDialog.NFD_PathSet_EnumNext(psEnum, pp) == NativeFileDialog.NFD_OKAY &&
                    pp.get(0) != MemoryUtil.NULL) {
                array.add(Gdx.files.absolute(pp.getStringUTF8(0)));
                NativeFileDialog.NFD_PathSet_FreePath(pp.get(0));
            }

            NativeFileDialog.NFD_PathSet_FreeEnum(psEnum);
            NativeFileDialog.NFD_PathSet_Free(pathSet);

            // If nothing is selected, consider the selection operation is canceled.
            if (array.size == 0) {
                callback.canceled();
                return;
            }

            callback.selected(array);
        } catch (Throwable e) {
            defaultFileService.openMultipleFiles(dialogTitle, initialFile, fileFilters, callback);
        }
    }

    @Override
    public void saveFile(@Null String dialogTitle, @Null FileHandle initialFile, @Null FileFilter[] fileFilters, Callback callback) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
        @Null String initialPath = prepareInitialPath(initialFile);
        @Null NFDFilterItem.Buffer filterList = prepareFilterList(stack, fileFilters);

        PointerBuffer pp = MemoryUtil.memAllocPointer(1);

            int status = NativeFileDialog.NFD_SaveDialog(pp, filterList, initialPath, null);

            if (status == NativeFileDialog.NFD_CANCEL) {
                callback.canceled();
                return;
            }

            // Unexpected error.
            if (status != NativeFileDialog.NFD_OKAY) {
                String errorText = NativeFileDialog.NFD_GetError();
                throw new RuntimeException("Native file dialog error: " + errorText);
            }

            String selectedFilePath = pp.getStringUTF8(0);

            Array<FileHandle> array = new Array<>();
            array.add(Gdx.files.absolute(selectedFilePath));

            callback.selected(array);
        } catch (Throwable e) {
            defaultFileService.saveFile(dialogTitle, initialFile, fileFilters, callback);
        }
    }

    private static @Null String prepareInitialPath(@Null FileHandle fileHandle) {
        if (fileHandle == null)
            return null;

        return fileHandle.file().getAbsolutePath();
    }

//    private static @Null String prepareFilterList(@Null FileFilter[] filters) {
//        if (filters == null)
//            return null;
//
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < filters.length; i++) {
//            FileFilter filter = filters[i];
//            if (sb.length > 0) {
//                sb.append(";");
//            }
//            sb.append(Strings.join(",", (Object[]) filter.extensions));
//        }
//
//        if (sb.length == 0)
//            return null;
//
//        return sb.toString();
//    }

    private static @Null NFDFilterItem.Buffer prepareFilterList(MemoryStack stack, @Null FileFilter[] filters) {
        if (filters == null)
            return null;

        NFDFilterItem.Buffer filterBuffer = NFDFilterItem.malloc(filters.length, stack);;

        for (int i = 0; i < filters.length; i++) {
            FileFilter filter = filters[i];
            filterBuffer.get(i)
                    .name(stack.UTF8(filter.description))
                    .spec(stack.UTF8(Strings.join(",", (Object[]) filter.extensions)));
        }

        return filterBuffer;
    }
}

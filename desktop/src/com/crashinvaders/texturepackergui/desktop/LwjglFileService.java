package com.crashinvaders.texturepackergui.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.controllers.DefaultFileService;
import com.crashinvaders.texturepackergui.controllers.FileService;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NativeFileDialog;

import static org.lwjgl.system.MemoryUtil.memFree;

public class LwjglFileService implements FileService {

    @Inject InterfaceService interfaceService;

    private DefaultFileService defaultFileService;

    @Initiate
    void init() {
        DefaultFileService defaultFileService = new DefaultFileService();
        defaultFileService.initDependencies(interfaceService);
        this.defaultFileService = defaultFileService;
    }

    @Override
    public void pickDirectory(FileHandle initialFolder, FileChooserAdapter callback) {
        String initialPath = initialFolder.path();

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            initialPath = initialPath.replace("/", "\\");
        }

        PointerBuffer pathPointer = MemoryUtil.memAllocPointer(1);

        try {
            int status = NativeFileDialog.NFD_PickFolder(initialPath, pathPointer);

            if (status == NativeFileDialog.NFD_CANCEL) {
                callback.canceled();
                return;
            }

            // Unexpected error.
            if (status != NativeFileDialog.NFD_OKAY) {
                throw new RuntimeException("Native file dialog error: " + status);
            }

            String folder = pathPointer.getStringUTF8(0);
            NativeFileDialog.nNFD_Free(pathPointer.get(0));

            Array<FileHandle> array = new Array<>();
            array.add(Gdx.files.absolute(folder));

            callback.selected(array);
        } catch (Throwable e) {
//            FileChooser fileChooser = new FileChooser(FileChooser.Mode.OPEN);
//            fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);
//            fileChooser.setDirectory(initialPath);
//            fileChooser.setListener(callback);

//            getStage().addActor(fileChooser.fadeIn());

            defaultFileService.pickDirectory(initialFolder, callback);
        } finally {
            memFree(pathPointer);
        }
    }

    private Stage getStage() {
        return interfaceService.getCurrentController().getStage();
    }
}

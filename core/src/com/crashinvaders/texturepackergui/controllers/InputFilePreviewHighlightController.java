package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Null;
import com.crashinvaders.texturepackergui.controllers.main.CanvasController;
import com.crashinvaders.texturepackergui.controllers.model.InputFile;
import com.crashinvaders.texturepackergui.events.InputFileHoverEvent;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.kiwi.util.common.Strings;

@Component
public class InputFilePreviewHighlightController {
    private static final String TAG = InputFilePreviewHighlightController.class.getSimpleName();

    @Inject CanvasController canvasController;

    private InputFile highlightFile = null;

//    @Initiate void init() {
//        System.out.println("InputFilePreviewHighlightController.init");
//    }

//    @Destroy void destroy() {
//        System.out.println("InputFilePreviewHighlightController.destroy");
//    }

    @OnEvent(InputFileHoverEvent.class) void onEvent(InputFileHoverEvent event) {
        switch (event.action) {
            case ENTER:
                setHighlightFile(event.inputFile);
                break;
            case EXIT:
                if (this.highlightFile == event.inputFile) {
                    setHighlightFile(null);
                }
                break;
        }
    }

    private void setHighlightFile(InputFile inputFile) {
        if (this.highlightFile == inputFile) return;

        this.highlightFile = inputFile;

        if (inputFile != null) {
            RegionData regionData = resolveRegionData(inputFile);
            canvasController.setHighlightRegion(regionData.name, regionData.index);
        } else {
            canvasController.setHighlightRegion(null, RegionData.DEFAULT_INDEX);
        }
    }

    private RegionData resolveRegionData(InputFile inputFile) {
        RegionData result = RegionData.instance.reset();

        if (inputFile.getType() != InputFile.Type.Input || inputFile.isDirectory()) return result;

        // Resolve name.
        // Should match the PackingProcessor#collectImageFiles() code.
        {
            FileHandle fileHandle = inputFile.getFileHandle();
            String name = fileHandle.nameWithoutExtension();

            // Cut ninepatch file trailing.
            if (name.endsWith(".9")) {
                name = name.substring(0, name.length() - 2);
            }

            String regionName = inputFile.getRegionName();
            if (Strings.isNotEmpty(regionName)) {
                name = regionName;
            }
            result.name = name;
        }


        // Extract index (if present).
        // Index is encoded as a number value after the name separated by the underscore.
        // Example: test_name_23 (name: "test_name", index: "23")
        if (result.name.matches("^.+_\\d+$")) {
            int underscoreIdx = result.name.lastIndexOf("_");
            result.index = Integer.parseInt(result.name.substring(underscoreIdx + 1));
            result.name = result.name.substring(0, underscoreIdx);
        }

        return result;
    }

    private static class RegionData {
        public static final int DEFAULT_INDEX = -1;
        public static final RegionData instance = new RegionData();

        private String name = null;
        private int index = DEFAULT_INDEX;

        public RegionData reset() {
            name = null;
            index = DEFAULT_INDEX;
            return this;
        }
    }
}

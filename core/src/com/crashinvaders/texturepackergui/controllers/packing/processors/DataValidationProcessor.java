package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.crashinvaders.texturepackergui.controllers.model.InputFile;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;
import com.github.czyzby.kiwi.util.common.Strings;

/** Contains number of simple and most obvious checks to give user clear problem explanation. */
public class DataValidationProcessor implements PackProcessor {
    private static final String TAG = DataValidationProcessor.class.getSimpleName();

    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        ProjectModel project = node.getProject();
        PackModel pack = node.getPack();

        if (Strings.isEmpty(pack.getOutputDir()))
            throw new IllegalStateException("Output directory is not specified");

        if (pack.getInputFiles().size == 0)
            throw new IllegalStateException("There is no input files to perform packing");

        // Create output dir if it doesn't exist
        FileHandle outputDir = Gdx.files.absolute(pack.getOutputDir());
        if (!outputDir.exists()) {
            System.out.println("Output directory doesn't exist. Creating: \"" + outputDir.file().getAbsolutePath() + "\"");
            outputDir.mkdirs();
        }

        // Check if the output dir is matching to one of the input files/dirs to avoid accidental source image data loss.
        // https://github.com/crashinvaders/gdx-texture-packer-gui/issues/94
        for (InputFile inputFile : node.getPack().getInputFiles()) {
            if (inputFile.getType() != InputFile.Type.Input) return;

            final FileHandle fileHandle;
            if (inputFile.isDirectory()) {
                fileHandle = inputFile.getFileHandle();
            } else {
                fileHandle = inputFile.getFileHandle().parent();
            }

            if (fileHandle.equals(outputDir)) {
                throw new IllegalStateException("The atlas output dir (" + outputDir.path() + ") is matching to one of the input files/dirs (" + inputFile.getFileHandle().path() + "). " +
                        "That might lead to an accidental source image loss. Please use separate output directory to write your atlases.");
            }
        }
    }
}

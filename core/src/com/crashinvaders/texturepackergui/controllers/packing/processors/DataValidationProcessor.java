package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.crashinvaders.texturepackergui.controllers.model.ProjectModel;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;
import com.github.czyzby.kiwi.util.common.Strings;

/** Contains number of simple and most obvious checks to give user clear problem explanation. */
public class DataValidationProcessor implements PackProcessor {
    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        ProjectModel project = node.getProject();
        PackModel pack = node.getPack();

        if (Strings.isEmpty(pack.getOutputDir()))
            throw new IllegalStateException("Output directory is not specified");

        if (pack.getInputFiles().size == 0)
            throw new IllegalStateException("There is no input files to perform packing");
    }
}

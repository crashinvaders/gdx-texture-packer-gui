package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.crashinvaders.texturepackergui.services.model.FileTypeType;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.services.model.filetype.FileTypeModel;
import com.crashinvaders.texturepackergui.services.model.filetype.PngFileTypeModel;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;

public class PngFileTypeProcessor implements PackProcessor {

    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        PackModel pack = node.getPack();
        ProjectModel project = node.getProject();

        if (project.getFileType().getType() != FileTypeType.PNG) return;

        PngFileTypeModel fileType = project.getFileType();

        pack.getSettings().outputFormat = "png";
        pack.getSettings().format = fileType.getEncoding();
    }
}

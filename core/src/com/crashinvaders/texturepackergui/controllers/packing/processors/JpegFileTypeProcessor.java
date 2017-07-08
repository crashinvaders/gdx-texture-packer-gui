package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.badlogic.gdx.tools.texturepacker.JpegPageFileWriter;
import com.crashinvaders.texturepackergui.services.model.FileTypeType;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.ProjectModel;
import com.crashinvaders.texturepackergui.services.model.filetype.JpegFileTypeModel;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;

public class JpegFileTypeProcessor implements PackProcessor {

    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        PackModel pack = node.getPack();
        ProjectModel project = node.getProject();

        if (project.getFileType().getType() != FileTypeType.JPEG) return;

        JpegFileTypeModel fileType = project.getFileType();

        pack.getSettings().format = fileType.getEncoding();

        node.setPageFileWriter(new JpegPageFileWriter(fileType.getQuality()));
    }
}

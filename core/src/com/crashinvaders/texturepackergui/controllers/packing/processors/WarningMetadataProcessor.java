package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;

public class WarningMetadataProcessor implements PackProcessor {
    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        boolean hasWarnings = node.getMetadata(PackProcessingNode.META_HAS_WARNINGS, false);
        if (hasWarnings)
            return;

        // Do nothing for now.
    }
}

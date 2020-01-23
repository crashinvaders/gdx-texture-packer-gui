package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;

import java.util.Locale;

public class TotalTimeMetadataProcessor implements PackProcessor {
    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        long totalTime = (long)node.getMetadata(PackProcessingNode.META_END_TIME) - (long)node.getMetadata(PackProcessingNode.META_START_TIME);
        node.addMetadata(PackProcessingNode.META_TOTAL_TIME, totalTime);

        System.out.println(String.format(Locale.US, "Total processing time: %.2fsec", totalTime/1000000000D));
    }
}

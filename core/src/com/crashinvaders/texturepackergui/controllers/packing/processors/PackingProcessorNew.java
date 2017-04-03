package com.crashinvaders.texturepackergui.controllers.packing.processors;

import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessor;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackagingHandler;

public class PackingProcessorNew implements PackProcessor {
    @Override
    public void processPackage(PackProcessingNode node) throws Exception {
        PackModel pack = node.getPack();
        String settingsOrigExtension = pack.getSettings().atlasExtension;

        System.out.println("Packing started");

        PackagingHandler packagingHandler = new PackagingHandler(pack);
        packagingHandler.pack();

        pack.getSettings().atlasExtension = settingsOrigExtension;

        System.out.println("Packing done");
    }
}

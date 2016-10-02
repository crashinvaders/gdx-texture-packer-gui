package com.crashinvaders.texturepackergui.utils.packprocessing;

import com.badlogic.gdx.utils.Array;

public class CompositePackProcessor implements PackProcessor {

    private final Array<PackProcessor> processors = new Array<>(true, 16);

    public CompositePackProcessor(PackProcessor... processors) {
        this.processors.addAll(processors);
    }

    public CompositePackProcessor(Array<PackProcessor> processors) {
        this.processors.addAll(processors);
    }

    @Override
    public void processPackage(PackProcessingNode processingNode) throws Exception {
        for (int i = 0; i < processors.size; i++) {
            processors.get(i).processPackage(processingNode);
        }
    }
}

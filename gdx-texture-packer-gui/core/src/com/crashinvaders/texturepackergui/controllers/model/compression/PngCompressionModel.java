package com.crashinvaders.texturepackergui.controllers.model.compression;

import com.crashinvaders.common.statehash.StateHashable;
import com.crashinvaders.texturepackergui.controllers.model.PngCompressionType;

public abstract class PngCompressionModel implements StateHashable {
    protected final PngCompressionType type;

    public PngCompressionModel(PngCompressionType type) {
        this.type = type;
    }

    public PngCompressionType getType() {
        return type;
    }

    public abstract String serializeState();
    public abstract void deserializeState(String data);

    @Override
    public int computeStateHash() {
        return type.hashCode();
    }
}

package com.crashinvaders.texturepackergui.controllers.model.compression;

import com.crashinvaders.texturepackergui.controllers.model.PngCompressionType;

public abstract class PngCompressionModel {
    protected final PngCompressionType type;

    public PngCompressionModel(PngCompressionType type) {
        this.type = type;
    }

    public PngCompressionType getType() {
        return type;
    }

    public abstract String serializeState();
    public abstract void deserializeState(String data);
}

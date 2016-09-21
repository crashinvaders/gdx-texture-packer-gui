package com.crashinvaders.texturepackergui.services.model.compression;

import com.crashinvaders.texturepackergui.services.model.PngCompressionType;

public class TinyPngCompressionModel extends PngCompressionModel {

    public TinyPngCompressionModel() {
        super(PngCompressionType.TINY_PNG);
    }

    @Override
    public String serializeState() {
        return "";
    }

    @Override
    public void deserializeState(String data) {
    }
}

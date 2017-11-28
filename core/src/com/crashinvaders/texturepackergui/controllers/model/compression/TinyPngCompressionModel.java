package com.crashinvaders.texturepackergui.controllers.model.compression;

import com.crashinvaders.texturepackergui.controllers.model.PngCompressionType;

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

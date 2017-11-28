package com.crashinvaders.texturepackergui.controllers.model;

public class ScaleFactorModel {
    private final String suffix;
    private final float factor;

    public ScaleFactorModel(String suffix, float factor) {
        this.suffix = suffix;
        this.factor = factor;
    }

    public ScaleFactorModel(ScaleFactorModel scaleFactorModel) {
        this.suffix = scaleFactorModel.getSuffix();
        this.factor = scaleFactorModel.getFactor();
    }

    public String getSuffix() {
        return suffix;
    }

    public float getFactor() {
        return factor;
    }
}

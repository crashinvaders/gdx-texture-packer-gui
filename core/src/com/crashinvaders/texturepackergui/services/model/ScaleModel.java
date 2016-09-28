package com.crashinvaders.texturepackergui.services.model;

public class ScaleModel {
    private String suffix = "";
    private float factor = 1f;

    public ScaleModel() {
    }

    public ScaleModel(String suffix, float factor) {
        this.suffix = suffix;
        this.factor = factor;
    }

    public ScaleModel(ScaleModel scaleModel) {
        this.suffix = scaleModel.getSuffix();
        this.factor = scaleModel.getFactor();
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public float getFactor() {
        return factor;
    }

    public void setFactor(float factor) {
        this.factor = factor;
    }
}

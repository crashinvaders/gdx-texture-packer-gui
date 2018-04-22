package com.crashinvaders.texturepackergui.controllers.model;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class ScaleFactorModel {
    private final String suffix;
    private final float factor;
    private final TexturePacker.Resampling resampling;

    public ScaleFactorModel(String suffix, float factor, TexturePacker.Resampling resampling) {
        this.suffix = suffix;
        this.factor = factor;
        this.resampling = resampling;
    }

    public ScaleFactorModel(ScaleFactorModel scaleFactorModel) {
        this.suffix = scaleFactorModel.getSuffix();
        this.factor = scaleFactorModel.getFactor();
        this.resampling = scaleFactorModel.getResampling();
    }

    public String getSuffix() {
        return suffix;
    }

    public float getFactor() {
        return factor;
    }

    public TexturePacker.Resampling getResampling() {
        return resampling;
    }
}

package com.crashinvaders.texturepackergui.controllers.model;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.crashinvaders.common.statehash.StateHashUtils;
import com.crashinvaders.common.statehash.StateHashable;

public class ScaleFactorModel implements StateHashable {
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

    @Override
    public int computeStateHash() {
        return StateHashUtils.computeHash(suffix, factor, resampling);
    }
}

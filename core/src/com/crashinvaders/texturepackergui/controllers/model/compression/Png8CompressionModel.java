package com.crashinvaders.texturepackergui.controllers.model.compression;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.*;
import com.crashinvaders.common.statehash.StateHashUtils;
import com.crashinvaders.texturepackergui.controllers.model.PngCompressionType;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.Dithered.DitherAlgorithm;

import java.io.StringWriter;

public class Png8CompressionModel extends PngCompressionModel {

    private int level = 6;
    private int threshold = 400;
    private DitherAlgorithm ditherAlgorithm = DitherAlgorithm.SCATTER;
    
    public Png8CompressionModel() {
        super(PngCompressionType.TE_PNG8);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public DitherAlgorithm getDitherAlgorithm() {
        return ditherAlgorithm;
    }

    public void setDitherAlgorithm(DitherAlgorithm algorithm) {
        this.ditherAlgorithm = algorithm;
    }

    @Override
    public String serializeState() {
        StringWriter buffer = new StringWriter();
        try {
            Json json = new Json();
            json.setWriter(new JsonWriter(buffer));
            json.writeObjectStart();
            json.writeValue("level", level);
            json.writeValue("threshold", threshold);
            json.writeValue("ditherAlgorithm", ditherAlgorithm.ordinal());
            json.writeObjectEnd();
            return buffer.toString();
        } finally {
            StreamUtils.closeQuietly(buffer);
        }
    }

    @Override
    public void deserializeState(String data) {
        if (data == null) return;

        JsonValue jsonValue = new JsonReader().parse(data);
        level = jsonValue.getInt("level", level);
        threshold = jsonValue.getInt("threshold", threshold);

        int ditherAlgorithmOrdinal = jsonValue.getInt("ditherAlgorithm", ditherAlgorithm.ordinal());
        ditherAlgorithmOrdinal = MathUtils.clamp(ditherAlgorithmOrdinal, 0, DitherAlgorithm.values().length - 1);
        ditherAlgorithm = DitherAlgorithm.values()[ditherAlgorithmOrdinal];
    }

    @Override
    public int computeStateHash() {
        return StateHashUtils.computeHash(super.computeStateHash(), level, threshold, ditherAlgorithm);
    }
}

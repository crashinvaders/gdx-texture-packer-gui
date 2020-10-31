package com.crashinvaders.texturepackergui.controllers.model.compression;

import com.badlogic.gdx.utils.*;
import com.crashinvaders.common.statehash.StateHashUtils;
import com.crashinvaders.texturepackergui.controllers.model.PngCompressionType;

import java.io.StringWriter;

public class Png8CompressionModel extends PngCompressionModel {

    private int level = 6;
    private int threshold = 400;
    private boolean dithering = true;
    
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

    public boolean isDithering() {
        return dithering;
    }

    public void setDithering(boolean dithering) {
        this.dithering = dithering;
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
            json.writeValue("dithering", dithering);
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
        dithering = jsonValue.getBoolean("dithering", dithering);
    }

    @Override
    public int computeStateHash() {
        return StateHashUtils.computeHash(super.computeStateHash(), level, threshold, dithering);
    }
}

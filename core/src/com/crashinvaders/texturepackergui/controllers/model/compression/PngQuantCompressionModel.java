package com.crashinvaders.texturepackergui.controllers.model.compression;

import com.badlogic.gdx.utils.*;
import com.crashinvaders.common.statehash.StateHashUtils;
import com.crashinvaders.texturepackergui.controllers.model.PngCompressionType;

import java.io.StringWriter;
import java.util.zip.Deflater;

public class PngQuantCompressionModel extends PngCompressionModel {

    private int deflateLevel = Deflater.BEST_COMPRESSION;
    private int maxColors = 256;
    private int minQuality = 65;
    private int maxQuality = 80;

    public PngQuantCompressionModel() {
        super(PngCompressionType.PNGQUANT);
    }

    public int getDeflateLevel() {
        return deflateLevel;
    }

    public void setDeflateLevel(int deflateLevel) {
        this.deflateLevel = deflateLevel;
    }

    public int getMaxColors() {
        return maxColors;
    }

    public void setMaxColors(int maxColors) {
        this.maxColors = maxColors;
    }

    public int getMinQuality() {
        return minQuality;
    }

    public void setMinQuality(int minQuality) {
        this.minQuality = minQuality;
    }

    public int getMaxQuality() {
        return maxQuality;
    }

    public void setMaxQuality(int maxQuality) {
        this.maxQuality = maxQuality;
    }

    @Override
    public String serializeState() {
        StringWriter buffer = new StringWriter();
        try {
            Json json = new Json();
            json.setWriter(new JsonWriter(buffer));
            json.writeObjectStart();
            json.writeValue("deflateLevel", deflateLevel);
            json.writeValue("maxColors", maxColors);
            json.writeValue("minQuality", minQuality);
            json.writeValue("maxQuality", maxQuality);
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
        deflateLevel = jsonValue.getInt("deflateLevel", deflateLevel);
        minQuality = jsonValue.getInt("maxColors", maxColors);
        minQuality = jsonValue.getInt("minQuality", minQuality);
        maxQuality = jsonValue.getInt("maxQuality", maxQuality);
    }

    @Override
    public int computeStateHash() {
        return StateHashUtils.computeHash(super.computeStateHash(), deflateLevel, maxColors, minQuality, maxQuality);
    }
}

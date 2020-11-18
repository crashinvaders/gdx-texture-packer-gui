package com.crashinvaders.texturepackergui.controllers.model.compression;

import com.badlogic.gdx.utils.*;
import com.crashinvaders.common.statehash.StateHashUtils;
import com.crashinvaders.texturepackergui.controllers.model.PngCompressionType;

import java.io.StringWriter;
import java.util.zip.Deflater;

public class PngquantCompressionModel extends PngCompressionModel {

    private int deflateLevel = Deflater.BEST_COMPRESSION; // [0..9]
    private int speed = 4; // [1..10]
    private int maxColors = 256; // [0..256]
    private int minQuality = 65; // [0..100]
    private int maxQuality = 80; // [0..100]
    private float ditheringLevel = 0.0f; // [0..1]

    public PngquantCompressionModel() {
        super(PngCompressionType.PNGQUANT);
    }

    public int getDeflateLevel() {
        return deflateLevel;
    }

    public void setDeflateLevel(int deflateLevel) {
        this.deflateLevel = deflateLevel;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
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

    public float getDitheringLevel() {
        return ditheringLevel;
    }

    public void setDitheringLevel(float ditheringLevel) {
        this.ditheringLevel = ditheringLevel;
    }

    @Override
    public String serializeState() {
        StringWriter buffer = new StringWriter();
        try {
            Json json = new Json();
            json.setWriter(new JsonWriter(buffer));
            json.writeObjectStart();
            json.writeValue("deflateLevel", deflateLevel);
            json.writeValue("speed", speed);
            json.writeValue("maxColors", maxColors);
            json.writeValue("minQuality", minQuality);
            json.writeValue("maxQuality", maxQuality);
            json.writeValue("ditheringLevel", ditheringLevel);
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
        maxQuality = jsonValue.getInt("speed", speed);
        minQuality = jsonValue.getInt("maxColors", maxColors);
        minQuality = jsonValue.getInt("minQuality", minQuality);
        maxQuality = jsonValue.getInt("maxQuality", maxQuality);
        ditheringLevel = jsonValue.getFloat("ditheringLevel", ditheringLevel);
    }

    @Override
    public int computeStateHash() {
        return StateHashUtils.computeHash(super.computeStateHash(),
                deflateLevel, speed, maxColors, minQuality, maxQuality, deflateLevel);
    }
}

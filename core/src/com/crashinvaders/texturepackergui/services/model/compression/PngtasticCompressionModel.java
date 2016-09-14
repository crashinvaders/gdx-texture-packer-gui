package com.crashinvaders.texturepackergui.services.model.compression;

import com.badlogic.gdx.utils.*;
import com.crashinvaders.texturepackergui.services.model.PngCompressionType;

import java.io.StringWriter;

public class PngtasticCompressionModel extends PngCompressionModel {

    private int level = 5;

    public PngtasticCompressionModel() {
        super(PngCompressionType.PNGTASTIC);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String serializeState() {
        StringWriter buffer = new StringWriter();
        try {
            Json json = new Json();
            json.setWriter(new JsonWriter(buffer));
            json.writeObjectStart();
            json.writeValue("level", level);
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
    }
}

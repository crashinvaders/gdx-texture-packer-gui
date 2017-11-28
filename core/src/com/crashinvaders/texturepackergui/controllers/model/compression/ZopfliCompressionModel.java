package com.crashinvaders.texturepackergui.controllers.model.compression;

import com.badlogic.gdx.utils.*;
import com.crashinvaders.texturepackergui.controllers.model.PngCompressionType;

import java.io.StringWriter;

public class ZopfliCompressionModel extends PngCompressionModel {

    private int level = 3;
    private int iterations = 1;

    public ZopfliCompressionModel() {
        super(PngCompressionType.ZOPFLI);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    @Override
    public String serializeState() {
        StringWriter buffer = new StringWriter();
        try {
            Json json = new Json();
            json.setWriter(new JsonWriter(buffer));
            json.writeObjectStart();
            json.writeValue("level", level);
            json.writeValue("iterations", iterations);
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
        iterations = jsonValue.getInt("iterations", iterations);
    }
}

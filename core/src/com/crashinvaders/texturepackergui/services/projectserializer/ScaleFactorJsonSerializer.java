package com.crashinvaders.texturepackergui.services.projectserializer;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.crashinvaders.texturepackergui.services.model.ScaleFactorModel;

public class ScaleFactorJsonSerializer implements Json.Serializer<ScaleFactorModel> {

    @Override
    public void write(Json json, ScaleFactorModel model, Class knownType) {
        json.writeObjectStart();
        json.writeValue("suffix", model.getSuffix());
        json.writeValue("factor", model.getFactor());
        json.writeObjectEnd();
    }

    @Override
    public ScaleFactorModel read(Json json, JsonValue jsonData, Class type) {
        String suffix = "";
        float factor = 1f;

        JsonValue.JsonIterator iterator = jsonData.iterator();
        while (iterator.hasNext()) {
            JsonValue value = iterator.next();
            switch (value.name) {
                case "suffix":
                    suffix = value.asString();
                    break;
                case "factor":
                    factor = value.asFloat();
                    break;
            }
        }
        return new ScaleFactorModel(suffix, factor);
    }
}

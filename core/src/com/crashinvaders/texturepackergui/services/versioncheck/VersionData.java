package com.crashinvaders.texturepackergui.services.versioncheck;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.crashinvaders.common.Version;

public class VersionData {
    public static final int NO_AVAILABLE_VERSION = -1;

    public int id = NO_AVAILABLE_VERSION;
    public String url;
    public String name;
    public String tag;
    public Version version;
    public String description;
    public String plainJson;

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public Version getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getPlainJson() {
        return plainJson;
    }

    public static class Serializer implements com.badlogic.gdx.utils.Json.Serializer<VersionData> {
        @Override
        public VersionData read(Json json, JsonValue jsonData, Class type) {
            VersionData data = new VersionData();
            data.plainJson = jsonData.prettyPrint(JsonWriter.OutputType.json, 0);

            for (JsonValue entry = jsonData.child; entry != null; entry = entry.next) {
                switch (entry.name) {
                    case "id":
                        data.id = entry.asInt();
                        break;
                    case "html_url":
                        data.url = entry.asString();
                        break;
                    case "tag_name":
                        String tagName = entry.asString();
                        data.tag = tagName;
                        data.version = new Version(tagName);
                        break;
                    case "name":
                        data.name = entry.asString();
                        break;
                    case "body":
                        data.description = entry.asString();
                        break;
                }
            }

            return data;
        }

        @Override
        public void write(Json json, VersionData object, Class knownType) {
            throw new UnsupportedOperationException("Writing is not supported");
        }
    }
}

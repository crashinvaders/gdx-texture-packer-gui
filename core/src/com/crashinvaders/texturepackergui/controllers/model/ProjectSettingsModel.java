package com.crashinvaders.texturepackergui.controllers.model;

import com.badlogic.gdx.utils.*;
import com.crashinvaders.common.statehash.StateHashUtils;
import com.crashinvaders.common.statehash.StateHashable;
import com.github.czyzby.autumn.processor.event.EventDispatcher;

import java.io.StringWriter;

public class ProjectSettingsModel implements StateHashable {

    protected EventDispatcher eventDispatcher;

    private final InputFileSettingsModel inputFiles = new InputFileSettingsModel();

    public InputFileSettingsModel getInputFiles() {
        return inputFiles;
    }

    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public String serializeState() {
        StringWriter buffer = new StringWriter();
        try {
            Json json = new Json();
            json.setWriter(new JsonWriter(buffer));
            json.writeObjectStart();
            json.writeValue("inputFiles", inputFiles);
            json.writeObjectEnd();
            return buffer.toString();
        } finally {
            StreamUtils.closeQuietly(buffer);
        }
    }

    public void deserializeState(String data) {
        if (data == null) return;

        JsonValue jsonValue = new JsonReader().parse(data);
        this.inputFiles.read(null, jsonValue.get("inputFiles"));
    }

    @Override
    public int computeStateHash() {
        return StateHashUtils.computeHash(inputFiles);
    }

    public class InputFileSettingsModel implements StateHashable, Json.Serializable {

//        private boolean keepInputFileExtensions = false;

//        public boolean isKeepInputFileExtensions() {
//            return keepInputFileExtensions;
//        }

//        public void setKeepInputFileExtensions(boolean value) {
//            if (this.keepInputFileExtensions == value) return;
//
//            this.keepInputFileExtensions = value;
//
//            if (eventDispatcher != null) {
//                eventDispatcher.postEvent(new ChangedEvent(
//                        ProjectSettingsModel.this,
//                        ChangedEvent.Property.INPUT_FILES));
//            }
//        }

        @Override
        public void write(Json json) {
//            json.writeValue("keepInputFileExtensions", keepInputFileExtensions);
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
//            this.keepInputFileExtensions = jsonData.getBoolean("keepInputFileExtensions", this.keepInputFileExtensions);
        }

        @Override
        public int computeStateHash() {
//            return StateHashUtils.computeHash(keepInputFileExtensions);
            return 0;
        }
    }

    public static class ChangedEvent {

        private final ProjectSettingsModel settings;
        private final Property property;

        public ChangedEvent(ProjectSettingsModel settings, Property property) {
            this.settings = settings;
            this.property = property;
        }

        public ProjectSettingsModel getSettings() {
            return settings;
        }

        public Property getProperty() {
            return property;
        }

        public enum Property {
//            INPUT_FILES,
        }
    }

}

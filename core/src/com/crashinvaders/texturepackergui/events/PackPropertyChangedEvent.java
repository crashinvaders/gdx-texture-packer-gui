package com.crashinvaders.texturepackergui.events;

import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.services.model.InputFile;

public class PackPropertyChangedEvent {

    private final PackModel pack;
    private final Property property;
    private InputFile inputFile;

    public PackPropertyChangedEvent(PackModel pack, Property property) {
        this.pack = pack;
        this.property = property;
    }

    public PackModel getPack() {
        return pack;
    }

    public Property getProperty() {
        return property;
    }

    public InputFile getInputFile() {
        return inputFile;
    }

    public PackPropertyChangedEvent setInputFile(InputFile inputFile) {
        this.inputFile = inputFile;
        return this;
    }

    public enum Property {
        NAME,
        INPUT,
        OUTPUT,
        FILENAME,
        SCALE_FACTORS,
        INPUT_FILE_ADDED,
        INPUT_FILE_REMOVED;
    }
}

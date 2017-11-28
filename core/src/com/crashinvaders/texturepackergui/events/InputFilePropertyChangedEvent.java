package com.crashinvaders.texturepackergui.events;

import com.crashinvaders.texturepackergui.controllers.model.InputFile;

public class InputFilePropertyChangedEvent {

    private InputFile inputFile;

    public InputFilePropertyChangedEvent(InputFile inputFile) {
        this.inputFile = inputFile;
    }

    public InputFile getInputFile() {
        return inputFile;
    }
}

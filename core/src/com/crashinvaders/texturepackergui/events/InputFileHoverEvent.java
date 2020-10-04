package com.crashinvaders.texturepackergui.events;

import com.crashinvaders.texturepackergui.controllers.model.InputFile;

public class InputFileHoverEvent {

    public final InputFile inputFile;
    public final Action action;

    public InputFileHoverEvent(InputFile inputFile, Action action) {
        this.inputFile = inputFile;
        this.action = action;
    }

    public enum Action {
        ENTER,
        EXIT
    }
}

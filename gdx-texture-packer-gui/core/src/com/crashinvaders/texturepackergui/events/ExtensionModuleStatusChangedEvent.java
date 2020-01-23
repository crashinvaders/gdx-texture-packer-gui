package com.crashinvaders.texturepackergui.events;

import com.crashinvaders.texturepackergui.controllers.extensionmodules.ExtensionModuleController;

public class ExtensionModuleStatusChangedEvent {
    private final ExtensionModuleController moduleController;

    public ExtensionModuleStatusChangedEvent(ExtensionModuleController moduleController) {
        this.moduleController = moduleController;
    }

    public ExtensionModuleController getModuleController() {
        return moduleController;
    }
}

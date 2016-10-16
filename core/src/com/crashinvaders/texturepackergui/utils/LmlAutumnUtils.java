package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.lml.parser.LmlData;
import com.github.czyzby.lml.parser.action.ActionContainer;

public class LmlAutumnUtils {

    public static <T extends Actor> T parseLml(InterfaceService interfaceService, String actionContainerName, ActionContainer actionContainer, FileHandle fileHandle) {
        boolean containerRegistered = registerActionContainer(interfaceService, actionContainerName, actionContainer);

        //noinspection unchecked
        T actor = (T) interfaceService.getParser().parseTemplate(fileHandle).first();

        if (containerRegistered) {
            unregisterActionContainer(interfaceService, actionContainerName);
        }
        return actor;
    }

    /**
     * @return false if action container already been registered
     */
    public static boolean registerActionContainer(InterfaceService interfaceService, String name, ActionContainer actionContainer) {
        LmlData lmlData = interfaceService.getParser().getData();
        if (lmlData.getActionContainer(name) != null) {
            return false;
        }
        lmlData.addActionContainer(name, actionContainer);
        return true;
    }

    public static void unregisterActionContainer(InterfaceService interfaceService, String name) {
        LmlData lmlData = interfaceService.getParser().getData();
        lmlData.removeActionContainer(name);
    }
}

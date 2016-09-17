package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.crashinvaders.texturepackergui.views.ContentDialog;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;

public class WidgetUtils {
    /**
     * @param lmlTemplate lml template with content that will be placed in dialog
     */
    public static ContentDialog showContentDialog(InterfaceService interfaceService, String title, FileHandle lmlTemplate) {
        Actor content = interfaceService.getParser().parseTemplate(lmlTemplate).first();
        ContentDialog dialog = new ContentDialog(title, content);
        dialog.show(interfaceService.getCurrentController().getStage());
        return dialog;
    }
}

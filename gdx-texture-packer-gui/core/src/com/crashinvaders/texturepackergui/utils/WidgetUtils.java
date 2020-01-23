package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.crashinvaders.texturepackergui.views.ContentDialog;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisWindow;

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

    /** Obtains close button from {@link VisWindow} */
    public static VisImageButton obtainCloseButton(VisWindow window) {
        return  (VisImageButton) window.getTitleTable().getChildren().peek();
    }
}

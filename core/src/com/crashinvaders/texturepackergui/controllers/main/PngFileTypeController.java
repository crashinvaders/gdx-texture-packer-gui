package com.crashinvaders.texturepackergui.controllers.main;

import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisSelectBox;

@Component
public class PngFileTypeController implements ActionContainer {

    @LmlActor("cboPngTextureEncoding") VisSelectBox<String> cboEncoding;

    @LmlAction("onEncodingChanged") void onEncodingChanged() {
        System.out.println("value = [" + cboEncoding.getSelected() + "]");
    }

    public String getActionContainerName() {
        return this.getClass().getSimpleName();
    }
}

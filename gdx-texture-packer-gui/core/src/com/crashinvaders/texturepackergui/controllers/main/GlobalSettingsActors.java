package com.crashinvaders.texturepackergui.controllers.main;

import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.VisSelectBox;

@SuppressWarnings("WeakerAccess")
public class GlobalSettingsActors {
    @LmlActor("cboFileType") VisSelectBox<WidgetData.FileType> cboFileType;
}

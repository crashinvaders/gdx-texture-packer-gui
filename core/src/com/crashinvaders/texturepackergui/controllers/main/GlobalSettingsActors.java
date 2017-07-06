package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.VisSelectBox;

//TODO remove
@SuppressWarnings("WeakerAccess")
public class GlobalSettingsActors {
    @LmlActor("cboFileType") VisSelectBox<WidgetData.FileType> cboFileType;
}

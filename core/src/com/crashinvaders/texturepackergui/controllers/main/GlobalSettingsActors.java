package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.VisSelectBox;

//TODO remove
@SuppressWarnings("WeakerAccess")
public class GlobalSettingsActors {

    @LmlActor("containerPngCompSettings") ShrinkContainer containerPngCompSettings;
    @LmlActor("cboPngCompression") SelectBox<WidgetData.PngCompression> cboPngCompression;
    
    @LmlActor("containerEtcCompSettings") ShrinkContainer containerEtcCompSettings;
    @LmlActor("cboEtcCompression") SelectBox<WidgetData.CompressionEtc> cboEtcCompression;

    @LmlActor("cboFileType") VisSelectBox<WidgetData.FileType> cboFileType;
}

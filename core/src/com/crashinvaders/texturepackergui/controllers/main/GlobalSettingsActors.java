package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.github.czyzby.lml.annotation.LmlActor;

@SuppressWarnings("WeakerAccess")
public class GlobalSettingsActors {

    @LmlActor("containerPngCompSettings") ShrinkContainer containerPngCompSettings;
    @LmlActor("cboPngCompression") SelectBox<WidgetData.CompressionPng> cboPngCompression;
    
    @LmlActor("containerEtcCompSettings") ShrinkContainer containerEtcCompSettings;
    @LmlActor("cboEtcCompression") SelectBox<WidgetData.CompressionEtc> cboEtcCompression;
}

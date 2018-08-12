package com.crashinvaders.texturepackergui.controllers.main;

import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuItem;

@SuppressWarnings("WeakerAccess")
public class PackMenuActors {

    @LmlActor("muPack") Menu muPack;
    @LmlActor("miPackPackSelected") MenuItem miPackSelected;
    @LmlActor("miPackPackAll") MenuItem miPackAll;
    @LmlActor("miPackCopySettingsToAllPacks") MenuItem miCopySettingsToAllPacks;
}

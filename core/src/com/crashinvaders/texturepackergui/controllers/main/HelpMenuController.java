package com.crashinvaders.texturepackergui.controllers.main;

import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.MenuItem;

@SuppressWarnings("WeakerAccess")
public class HelpMenuController {

    @LmlActor("miHelpCheckForUpdates") MenuItem miCheckForUpdates;
    @LmlActor("miHelpAbout") MenuItem miAbout;
}

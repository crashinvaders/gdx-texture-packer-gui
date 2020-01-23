package com.crashinvaders.texturepackergui.controllers.main;

import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuItem;

@SuppressWarnings("WeakerAccess")
public class HelpMenuActors {

    @LmlActor("muHelp") Menu muHelp;
    @LmlActor("miHelpCheckForUpdates") MenuItem miCheckForUpdates;
    @LmlActor("miHelpAbout") MenuItem miAbout;
}

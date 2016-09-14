package com.crashinvaders.texturepackergui.controllers.main;

import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;

@SuppressWarnings("WeakerAccess")
public class FileMenuActors {

    @LmlActor("miFileNew") MenuItem miNew;
    @LmlActor("miFileOpen") MenuItem miOpen;
    @LmlActor("miFileOpenRecent") MenuItem miOpenRecent;
    @LmlActor("miFileSave") MenuItem miSave;
    @LmlActor("miFileSaveAs") MenuItem miSaveAs;
    @LmlActor("pmFileOpenRecent") PopupMenu pmOpenRecent;
}

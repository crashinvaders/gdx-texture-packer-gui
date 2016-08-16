package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.github.czyzby.lml.annotation.LmlActor;
import com.kotcrab.vis.ui.widget.VisList;
import com.kotcrab.vis.ui.widget.VisTextField;

@SuppressWarnings("WeakerAccess")
public class ProjectConfigController {

    @LmlActor("btnMenuNewPack") Button btnMenuNewPack;
    @LmlActor("btnMenuRenamePack") Button btnMenuRenamePack;
    @LmlActor("btnMenuCopyPack") Button btnMenuCopyPack;
    @LmlActor("btnMenuDeletePack") Button btnMenuDeletePack;
    @LmlActor("btnMenuPackUp") Button btnMenuPackUp;
    @LmlActor("btnMenuPackDown") Button btnMenuPackDown;
    @LmlActor("btnMenuOpenProject") Button btnMenuOpenProject;
    @LmlActor("btnMenuCloseProject") Button btnMenuCloseProject;
    @LmlActor("listPacks") VisList listPacks;
    @LmlActor("edtInputDir") VisTextField edtInputDir;
    @LmlActor("btnPickInputDir") Button btnPickInputDir;
    @LmlActor("edtOutputDir") VisTextField edtOutputDir;
    @LmlActor("btnPickOutputDir") Button btnPickOutputDir;
    @LmlActor("edtFileName") VisTextField edtFileName;
    @LmlActor("btnPackAll") Button btnPackAll;
    @LmlActor("btnPackSelected") Button btnPackSelected;
}

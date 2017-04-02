package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.kotcrab.vis.ui.widget.ListView;
import com.kotcrab.vis.ui.widget.VisList;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTextField;

@SuppressWarnings("WeakerAccess")
public class PackListActors {

    @LmlActor("btnMenuNewPack") Button btnMenuNewPack;
    @LmlActor("btnMenuRenamePack") Button btnMenuRenamePack;
    @LmlActor("btnMenuCopyPack") Button btnMenuCopyPack;
    @LmlActor("btnMenuDeletePack") Button btnMenuDeletePack;
    @LmlActor("btnMenuPackUp") Button btnMenuPackUp;
    @LmlActor("btnMenuPackDown") Button btnMenuPackDown;
    @LmlActor("btnMenuOpenProject") Button btnMenuOpenProject;
    @LmlActor("btnMenuCloseProject") Button btnMenuCloseProject;
    @LmlActor("edtOutputDir") VisTextField edtOutputDir;
    @LmlActor("btnPickOutputDir") Button btnPickOutputDir;
    @LmlActor("edtFileName") VisTextField edtFileName;
    @LmlActor("btnPackAll") Button btnPackAll;
    @LmlActor("btnPackSelected") Button btnPackSelected;

    @LmlActor("lvPacks") ListView.ListViewTable<PackModel> packListTable;
    ListView<PackModel> packList;
    PackListAdapter packListAdapter;
}

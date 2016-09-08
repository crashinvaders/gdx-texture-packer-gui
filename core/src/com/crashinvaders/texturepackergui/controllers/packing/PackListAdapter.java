package com.crashinvaders.texturepackergui.controllers.packing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.kotcrab.vis.ui.util.adapter.ArrayAdapter;
import com.kotcrab.vis.ui.widget.VisTable;

class PackListAdapter extends ArrayAdapter<PackModel, PackListViewItem> {
    private final InterfaceService interfaceService;

    public PackListAdapter(InterfaceService interfaceService) {
        super(new Array<PackModel>());
        this.interfaceService = interfaceService;
    }

    @Override
    protected PackListViewItem createView(PackModel pack) {
        VisTable view = (VisTable)interfaceService.getParser().parseTemplate(Gdx.files.internal("lml/dialogPackingListItem.lml")).first();
        return new PackListViewItem(pack, view);
    }
}

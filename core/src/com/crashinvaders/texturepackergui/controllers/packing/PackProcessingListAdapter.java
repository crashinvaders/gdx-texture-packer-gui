package com.crashinvaders.texturepackergui.controllers.packing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.kotcrab.vis.ui.util.adapter.ArrayAdapter;
import com.kotcrab.vis.ui.widget.VisTable;

class PackProcessingListAdapter extends ArrayAdapter<PackProcessingNode, ProcessingNodeListViewItem> {
    private final InterfaceService interfaceService;

    public PackProcessingListAdapter(InterfaceService interfaceService) {
        super(new Array<PackProcessingNode>());
        this.interfaceService = interfaceService;
    }

    @Override
    protected ProcessingNodeListViewItem createView(PackProcessingNode node) {
        VisTable view = (VisTable)interfaceService.getParser().parseTemplate(Gdx.files.internal("lml/dialogPackingListItem.lml")).first();
        return new ProcessingNodeListViewItem(node, view);
    }
}

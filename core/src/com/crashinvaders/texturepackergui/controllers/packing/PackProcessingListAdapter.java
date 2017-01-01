package com.crashinvaders.texturepackergui.controllers.packing;

import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.lml.parser.LmlParser;
import com.kotcrab.vis.ui.util.adapter.ArrayAdapter;

class PackProcessingListAdapter extends ArrayAdapter<PackProcessingNode, ProcessingNodeListViewItem> {
    private final InterfaceService interfaceService;
    private int nextOrderNum = 0;

    public PackProcessingListAdapter(InterfaceService interfaceService) {
        super(new Array<PackProcessingNode>());
        this.interfaceService = interfaceService;
    }

    @Override
    protected ProcessingNodeListViewItem createView(PackProcessingNode node) {
        LmlParser parser = interfaceService.getParser();
        return new ProcessingNodeListViewItem(parser, node, nextOrderNum++);
    }
}

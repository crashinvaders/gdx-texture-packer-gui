package com.crashinvaders.texturepackergui.controllers.test;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.github.czyzby.autumn.mvc.stereotype.View;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.util.adapter.ArrayAdapter;
import com.kotcrab.vis.ui.util.adapter.ItemAdapter;
import com.kotcrab.vis.ui.widget.ListView;
import com.kotcrab.vis.ui.widget.VisLabel;

@View(id = "test", value = "lml/test.lml", first = false)
public class TestController implements ActionContainer {

    @LmlActor("listview") ListView.ListViewTable<String> listViewTable;

    @LmlAfter() void initView() {
        generateListViewItems();
    }

    @LmlAction("instantiateListAdapter") ItemAdapter<String> instantiateListAdapter() {
        return new ListStringAdapter();
    }

    @LmlAction("generateListViewItems") void generateListViewItems() {
        ListStringAdapter adapter = (ListStringAdapter) listViewTable.getListView().getAdapter();
        adapter.clear();

        for (int i = 0; i < 4; i++) {
            adapter.add("item"+ MathUtils.random(1000, 9999));
        }

        // Select first item
        adapter.getSelectionManager().deselectAll();
        adapter.getSelectionManager().select(adapter.get(0));
    }

    private static class ListStringAdapter extends ArrayAdapter<String, VisLabel> {
        public ListStringAdapter() {
            super(new Array<String>());
            setSelectionMode(SelectionMode.SINGLE);
        }

        @Override
        protected VisLabel createView(String item) {
            return new VisLabel(item);
        }

        @Override
        protected void selectView(VisLabel view) {
            view.setColor(Color.GREEN);
        }

        @Override
        protected void deselectView(VisLabel view) {
            view.setColor(Color.WHITE);
        }
    }
}

package com.crashinvaders.texturepackergui.lml.attributes;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActorConsumer;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.kotcrab.vis.ui.widget.ListView;

/**
 * Assigns on click listener for ListView.
 * Action triggers when any area outside of the list items is being clicked.
 */
public class ListViewOnClickLmlAttribute implements LmlAttribute<ListView.ListViewTable> {
    @Override
    public Class<ListView.ListViewTable> getHandledType() {
        return ListView.ListViewTable.class;
    }

    @Override
    public void process(LmlParser parser, LmlTag tag, final ListView.ListViewTable listTable, String rawAttributeData) {
        final ActorConsumer<?, ListView.ListViewTable> action = parser.parseAction(rawAttributeData, listTable);
        if (action == null) {
            parser.throwError("Could not find action for: " + rawAttributeData + " with actor: " + listTable);
            return;
        }

        listTable.getListView().getScrollPane().addListener(new ClickListener(0) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (event.getTarget() == event.getListenerActor()) {
                    action.consume(listTable);
                }
            }
        });
    }
}

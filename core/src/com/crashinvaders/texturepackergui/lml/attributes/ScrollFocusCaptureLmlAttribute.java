package com.crashinvaders.texturepackergui.lml.attributes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.common.scene2d.ScrollFocusCaptureInputListener;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActorConsumer;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.kotcrab.vis.ui.widget.ListView;

import java.util.Iterator;

/** @see com.crashinvaders.common.scene2d.ScrollFocusCaptureInputListener */
public class ScrollFocusCaptureLmlAttribute implements LmlAttribute<Actor> {

    @Override
    public Class<Actor> getHandledType() {
        return Actor.class;
    }

    @Override
    public void process(final LmlParser parser, final LmlTag tag, Actor actor, final String rawAttributeData) {
        // Due to ListView tricky structure we should dig a little to get to the scrollable actor
        if (actor instanceof ListView.ListViewTable) {
            actor = ((ListView.ListViewTable) actor).getListView().getScrollPane();
        }

        boolean value = Boolean.parseBoolean(rawAttributeData);
        if (value) {
            // Add scroll focus capture listeners
            actor.addListener(new ScrollFocusCaptureInputListener());
        } else {
            // Remove scroll focus capture listener
            Iterator<EventListener> iterator = actor.getListeners().iterator();
            while (iterator.hasNext()) {
                EventListener listener = iterator.next();
                if (listener instanceof ScrollFocusCaptureInputListener) {
                    iterator.remove();
                }
            }
        }
    }
}

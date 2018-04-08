package com.crashinvaders.texturepackergui.lml.attributes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.crashinvaders.common.scene2d.TimeThresholdChangeListener;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActorConsumer;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;

/** @see com.crashinvaders.common.scene2d.TimeThresholdChangeListener */
public class TimeThresholdChangeListenerLmlAttribute implements LmlAttribute<Actor> {

    @Override
    public Class<Actor> getHandledType() {
        return Actor.class;
    }

    @Override
    public void process(final LmlParser parser, final LmlTag tag, final Actor actor, final String rawAttributeData) {
        final ActorConsumer<?, Actor> action = parser.parseAction(rawAttributeData, actor);
        if (action == null) {
            parser.throwError("Could not find action for: " + rawAttributeData + " with actor: " + actor);
        }
        actor.addListener(new TimeThresholdChangeListener(0.5f) {
            @Override
            public void onChanged() {
                action.consume(actor);
            }
        });
    }
}

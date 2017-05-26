package com.crashinvaders.texturepackergui.config.attributes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.crashinvaders.common.scene2d.TimeThresholdChangeListenerAction;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActorConsumer;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;

/** @see TimeThresholdChangeListenerAction */
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
        actor.addAction(new TimeThresholdChangeListenerAction(0.5f, new TimeThresholdChangeListenerAction.DelayedChangeListener() {
            @Override
            public void onActorChanged(Actor actor) {
                action.consume(actor);
            }
        }));
    }
}

package com.crashinvaders.texturepackergui.lml.attributes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;

public class DelegateInputEventsLmlAttribute implements LmlAttribute<Actor> {

    @Override
    public Class<Actor> getHandledType() {
        return Actor.class;
    }

    @Override
    public void process(LmlParser parser, LmlTag tag, Actor actor, String rawAttributeData) {
        String id = parser.parseString(rawAttributeData);
        final Actor targetActor = parser.getActorsMappedByIds().get(id);
        if (targetActor == null) {
            parser.throwErrorIfStrict("Cannot find actor for ID: " + id);
            return;
        }

        actor.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof InputEvent) {
                    return targetActor.notify(event, false);
                }
                return false;
            }
        });
    }
}

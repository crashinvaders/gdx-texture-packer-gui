package com.crashinvaders.texturepackergui.lml.attributes;

import com.badlogic.gdx.Gdx;
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
    public void process(final LmlParser parser, LmlTag tag, final Actor actor, String rawAttributeData) {
        final String id = parser.parseString(rawAttributeData);
        // Post for one frame to let layout be fully parsed (target actor may be parsed after this attribute).
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
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
        });
    }
}

package com.crashinvaders.texturepackergui.lml.attributes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActorConsumer;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;

/** Hook for keyboard focus changed events */
public class KeyboardFocusChangedLmlAttribute implements LmlAttribute<Actor> {

    @Override
    public Class<Actor> getHandledType() {
        return Actor.class;
    }

    @Override
    public void process(final LmlParser parser, final LmlTag tag, final Actor actor, final String rawAttributeData) {
        final ActorConsumer<?, Params> action = parser.parseAction(rawAttributeData, tmpParams);
        if (action == null) {
            parser.throwError("Could not find action for: " + rawAttributeData + " with actor: " + actor);
        }
        actor.addListener(new FocusListener() {
            @Override public void keyboardFocusChanged(FocusEvent event, Actor target, boolean focused) {
                if (target == actor) {
                    tmpParams.actor = actor;
                    tmpParams.focused = focused;
                    action.consume(tmpParams);
                    tmpParams.reset();
                }
            }
        });
    }

    private static Params tmpParams = new Params();
    public static class Params {
        public Actor actor;
        public boolean focused;

        public void reset() {
            actor = null;
        }
    }
}

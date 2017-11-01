package com.crashinvaders.texturepackergui.config.tags;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActorConsumer;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;

/**
 * Same as {@link com.github.czyzby.lml.parser.impl.attribute.OnClickLmlAttribute} but respects {@link InputEvent}'s canceled state.
 */
public class PatchedOnClickLmlAttribute implements LmlAttribute<Actor> {
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
        actor.addListener(new ClickListener() {
            @Override
            public void clicked(final InputEvent event, final float x, final float y) {
                if (event.isCancelled()) return;
                action.consume(actor);
            }
        });
    }
}

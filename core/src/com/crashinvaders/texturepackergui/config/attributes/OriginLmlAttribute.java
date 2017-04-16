
package com.crashinvaders.texturepackergui.config.attributes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.crashinvaders.common.scene2d.actions.ActionsExt;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.util.LmlUtilities;

/** @see Actor#setOrigin(int)  */
public class OriginLmlAttribute implements LmlAttribute<Actor> {
    @Override
    public Class<Actor> getHandledType() {
        return Actor.class;
    }

    @Override
    public void process(final LmlParser parser, final LmlTag tag, final Actor actor, final String rawAttributeData) {
        final int origin = LmlUtilities.parseAlignment(parser, actor, rawAttributeData);
        // Simple trick to make this attribute applied after actor is laid out (likely)
        actor.addAction(ActionsExt.post(Actions.run(new Runnable() {
            @Override
            public void run() {
                actor.setOrigin(origin);
            }
        })));
    }
}

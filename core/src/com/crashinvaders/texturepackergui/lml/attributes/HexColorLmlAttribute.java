
package com.crashinvaders.texturepackergui.lml.attributes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;

/** Let HEX color values to be assigned as an actor's color. */
public class HexColorLmlAttribute implements LmlAttribute<Actor> {
    @Override
    public Class<Actor> getHandledType() {
        return Actor.class;
    }

    @Override
    public void process(final LmlParser parser, final LmlTag tag, final Actor actor, final String rawAttributeData) {
        String hexValue = parser.parseString(rawAttributeData, actor);
        Color color = Color.valueOf(hexValue);
        actor.setColor(color);
    }
}

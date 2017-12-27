
package com.crashinvaders.texturepackergui.lml.attributes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;

/** Allows to set actor's color from a hex value string.
 *
 * @see Color#valueOf(String)
 * @author Metaphore */
public class HexColorLmlAttribute implements LmlAttribute<Actor> {
    @Override
    public Class<Actor> getHandledType() {
        return Actor.class;
    }

    @Override
    public void process(final LmlParser parser, final LmlTag tag, final Actor actor, final String rawAttributeData) {
        String hexValue = parser.parseString(rawAttributeData, actor);
        try {
            Color color = Color.valueOf(hexValue);
            actor.setColor(color);
        } catch (Exception exception) {
            parser.throwErrorIfStrict(
                    "Unable to parse HEX color value from string \"" + hexValue + "\"",
                    exception);
        }
    }
}

package com.crashinvaders.texturepackergui.lml.attributes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.crashinvaders.texturepackergui.utils.CommonUtils;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.attribute.ColorLmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;

/**
 * Extends {@link ColorLmlAttribute} with extra options.
 * 1. A HEX value could be used to describe a color. Available patterns are: #RGB, #RGBA, #RRGGBB, #RRGGBBAA.
 */
public class AdvancedColorLmLAttribute extends ColorLmlAttribute {

    @Override
    public Class<Actor> getHandledType() {
        return Actor.class;
    }

    @Override
    public void process(final LmlParser parser, final LmlTag tag, final Actor actor, final String rawAttributeData) {
        if (rawAttributeData.startsWith("#")) {
            String hexCode = rawAttributeData.substring(1);
            Color color;
            try {
                color = CommonUtils.parseHexColor(hexCode);
            } catch (Exception e) {
                parser.throwError("Error parsing HEX code value \"" + hexCode + "\"", e);
                return;
            }
            actor.setColor(color);
        } else {
            super.process(parser, tag, actor, rawAttributeData);
        }
    }
}

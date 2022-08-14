package com.crashinvaders.common.scene2d.lml.attributes;

import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;

/** See {@link HorizontalGroup#expand(boolean)}. Mapped to "groupExpand".
 *
 * @author metaphore */
public class HorizontalGroupExpandLmlAttribute implements LmlAttribute<HorizontalGroup> {
    @Override
    public Class<HorizontalGroup> getHandledType() {
        return HorizontalGroup.class;
    }

    @Override
    public void process(final LmlParser parser, final LmlTag tag, final HorizontalGroup actor,
            final String rawAttributeData) {
        actor.expand(parser.parseBoolean(rawAttributeData, actor));
    }
}

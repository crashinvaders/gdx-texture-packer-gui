package com.crashinvaders.texturepackergui.lml.attributes.seekbar;

import com.crashinvaders.texturepackergui.lml.tags.seekbar.IntSeekBarLmlBuilder;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlBuildingAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;

public class IntSeekBarMinLmlAttribute implements LmlBuildingAttribute<IntSeekBarLmlBuilder> {
    @Override
    public Class<IntSeekBarLmlBuilder> getBuilderType() {
        return IntSeekBarLmlBuilder.class;
    }

    @Override
    public boolean process(final LmlParser parser, final LmlTag tag, final IntSeekBarLmlBuilder builder,
            final String rawAttributeData) {
        builder.setMin(parser.parseInt(rawAttributeData));
        return FULLY_PARSED;
    }
}

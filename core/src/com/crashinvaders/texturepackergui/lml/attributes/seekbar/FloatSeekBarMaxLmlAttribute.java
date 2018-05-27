package com.crashinvaders.texturepackergui.lml.attributes.seekbar;

import com.crashinvaders.texturepackergui.lml.tags.seekbar.FloatSeekBarLmlBuilder;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlBuildingAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;

public class FloatSeekBarMaxLmlAttribute implements LmlBuildingAttribute<FloatSeekBarLmlBuilder> {
    @Override
    public Class<FloatSeekBarLmlBuilder> getBuilderType() {
        return FloatSeekBarLmlBuilder.class;
    }

    @Override
    public boolean process(final LmlParser parser, final LmlTag tag, final FloatSeekBarLmlBuilder builder,
            final String rawAttributeData) {
        builder.setMax(parser.parseFloat(rawAttributeData));
        return FULLY_PARSED;
    }
}

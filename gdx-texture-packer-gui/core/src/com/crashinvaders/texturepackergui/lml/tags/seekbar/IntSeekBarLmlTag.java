package com.crashinvaders.texturepackergui.lml.tags.seekbar;

import com.crashinvaders.texturepackergui.views.seekbar.IntSeekBarModel;
import com.crashinvaders.texturepackergui.views.seekbar.SeekBarModel;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;

public class IntSeekBarLmlTag extends AbstractSeekBarLmlTag {
    public IntSeekBarLmlTag(LmlParser parser, LmlTag parentTag, StringBuilder rawTagData) {
        super(parser, parentTag, rawTagData);
    }

    @Override
    protected LmlActorBuilder getNewInstanceOfBuilder() {
        return new IntSeekBarLmlBuilder();
    }

    @Override
    protected SeekBarModel createModel(LmlActorBuilder builder) {
        final IntSeekBarLmlBuilder intBuilder = (IntSeekBarLmlBuilder) builder;
        return new IntSeekBarModel(
                intBuilder.getValue(), intBuilder.getMin(),
                intBuilder.getMax(),
                intBuilder.getStep()
        );
    }

    public static class Provider implements LmlTagProvider {
        @Override
        public LmlTag create(LmlParser parser, LmlTag parentTag, StringBuilder rawTagData) {
            return new IntSeekBarLmlTag(parser, parentTag, rawTagData);
        }
    }
}

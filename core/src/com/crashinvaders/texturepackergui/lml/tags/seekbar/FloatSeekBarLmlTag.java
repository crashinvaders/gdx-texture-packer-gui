package com.crashinvaders.texturepackergui.lml.tags.seekbar;

import com.crashinvaders.texturepackergui.views.seekbar.FloatSeekBarModel;
import com.crashinvaders.texturepackergui.views.seekbar.IntSeekBarModel;
import com.crashinvaders.texturepackergui.views.seekbar.SeekBarModel;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;

public class FloatSeekBarLmlTag extends AbstractSeekBarLmlTag {
    public FloatSeekBarLmlTag(LmlParser parser, LmlTag parentTag, StringBuilder rawTagData) {
        super(parser, parentTag, rawTagData);
    }

    @Override
    protected LmlActorBuilder getNewInstanceOfBuilder() {
        return new FloatSeekBarLmlBuilder();
    }

    @Override
    protected SeekBarModel createModel(LmlActorBuilder builder) {
        final FloatSeekBarLmlBuilder floatBuilder = (FloatSeekBarLmlBuilder) builder;
        return new FloatSeekBarModel(
                floatBuilder.getValue(),
                floatBuilder.getMin(),
                floatBuilder.getMax(),
                floatBuilder.getStepSize(),
                floatBuilder.getPrecision());
    }

    public static class Provider implements LmlTagProvider {
        @Override
        public LmlTag create(LmlParser parser, LmlTag parentTag, StringBuilder rawTagData) {
            return new FloatSeekBarLmlTag(parser, parentTag, rawTagData);
        }
    }
}

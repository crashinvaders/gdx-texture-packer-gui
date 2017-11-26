package com.crashinvaders.texturepackergui.config.tags;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.crashinvaders.common.scene2d.ScalarScalableWrapper;
import com.crashinvaders.common.scene2d.TransformScalableWrapper;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.AbstractGroupLmlTag;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;

public class ScalarScalableWrapperLmlTag extends AbstractGroupLmlTag {

    public ScalarScalableWrapperLmlTag(LmlParser parser, LmlTag parentTag, StringBuilder rawTagData) {
        super(parser, parentTag, rawTagData);
    }

    @Override
    protected ScalarScalableWrapper<Actor> getNewInstanceOfGroup(LmlActorBuilder builder) {
        return new ScalarScalableWrapper<>();
    }

    @Override
    protected void handlePlainTextLine(final String plainTextLine) {
        addChild(toLabel(plainTextLine));
    }

    @Override
    protected void handleValidChild(final LmlTag childTag) {
        addChild(childTag.getActor());
    }

    /** @param child will be set as container's child. */
    protected void addChild(final Actor child) {
        ScalarScalableWrapper<Actor> wrapper = getWrapper();
        if (wrapper.getActor() != null) {
            getParser().throwErrorIfStrict("TransformScalableWrapper widget can manage only one child.");
        }
        wrapper.setActor(child);
    }

    /** @return casted actor. */
    @SuppressWarnings("unchecked")
    protected ScalarScalableWrapper<Actor> getWrapper() {
        return (ScalarScalableWrapper<Actor>) getActor();
    }

    public static class TagProvider implements LmlTagProvider {
        @Override
        public LmlTag create(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
            return new ScalarScalableWrapperLmlTag(parser, parentTag, rawTagData);
        }
    }

}

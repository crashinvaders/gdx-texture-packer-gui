package com.crashinvaders.texturepackergui.lml.tags;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.actor.ContainerLmlTag;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;

/** @see ShrinkContainer */
public class ShrinkContainerLmlTag extends ContainerLmlTag {

    public ShrinkContainerLmlTag(LmlParser parser, LmlTag parentTag, StringBuilder rawTagData) {
        super(parser, parentTag, rawTagData);
    }

    @Override
    protected Actor getNewInstanceOfActor(final LmlActorBuilder builder) {
        return new ShrinkContainer<Actor>();
    }

    public static class TagProvider implements LmlTagProvider {
        @Override
        public LmlTag create(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
            return new ShrinkContainerLmlTag(parser, parentTag, rawTagData);
        }
    }
}

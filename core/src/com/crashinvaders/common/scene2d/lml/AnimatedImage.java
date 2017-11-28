package com.crashinvaders.common.scene2d.lml;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.github.czyzby.kiwi.util.common.Strings;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.actor.ImageLmlTag;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;

//TODO remove this once https://github.com/czyzby/gdx-lml/issues/63 gets resolved
/**
 * Small patch to support non-continuous rendering mode
 * @see com.github.czyzby.lml.scene2d.ui.reflected.AnimatedImage
 */
public class AnimatedImage extends com.github.czyzby.lml.scene2d.ui.reflected.AnimatedImage {

    public AnimatedImage(Skin skin, String... frameNames) {
        super(skin, frameNames);
    }

    public AnimatedImage(Drawable... frames) {
        super(frames);
    }

    public AnimatedImage(Iterable<Drawable> frames) {
        super(frames);
    }

    public AnimatedImage(Array<Drawable> frames) {
        super(frames);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        Gdx.graphics.requestRendering();
    }

    public static class LmlTag extends ImageLmlTag {
        public LmlTag(final LmlParser parser, final com.github.czyzby.lml.parser.tag.LmlTag parentTag, final StringBuilder rawTagData) {
            super(parser, parentTag, rawTagData);
        }

        @Override
        protected LmlActorBuilder getNewInstanceOfBuilder() {
            final LmlActorBuilder builder = super.getNewInstanceOfBuilder();
            builder.setStyleName(Strings.EMPTY_STRING);
            return builder;
        }

        @Override
        protected Actor getNewInstanceOfActor(final LmlActorBuilder builder) {
            final String[] frames = getParser().parseArray(builder.getStyleName());
            return new AnimatedImage(getSkin(builder), frames);
        }

        public static class Provider implements LmlTagProvider {
            @Override
            public com.github.czyzby.lml.parser.tag.LmlTag create(LmlParser parser, com.github.czyzby.lml.parser.tag.LmlTag parentTag, StringBuilder rawTagData) {
                return new LmlTag(parser, parentTag, rawTagData);
            }
        }
    }
}

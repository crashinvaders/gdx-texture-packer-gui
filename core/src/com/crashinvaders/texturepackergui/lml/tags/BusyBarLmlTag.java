package com.crashinvaders.texturepackergui.lml.tags;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.crashinvaders.texturepackergui.views.BusyBar;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.AbstractNonParentalActorLmlTag;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;

public class BusyBarLmlTag extends AbstractNonParentalActorLmlTag {

    public BusyBarLmlTag(LmlParser parser, LmlTag parentTag, StringBuilder rawTagData) {
        super(parser, parentTag, rawTagData);
    }

    @Override
    protected Actor getNewInstanceOfActor(LmlActorBuilder builder) {
        return new BusyBar(this.getSkin(builder).get(builder.getStyleName(), BusyBar.Style.class));
    }

    public static class TagProvider implements LmlTagProvider {
        @Override
        public LmlTag create(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
            return new BusyBarLmlTag(parser, parentTag, rawTagData);
        }
    }
}

package com.crashinvaders.texturepackergui.lml.tags;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.crashinvaders.texturepackergui.views.GridGroupExt;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.AbstractGroupLmlTag;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;

/**
 * A temporary drop-off replacement for {@link com.github.czyzby.lml.vis.parser.impl.tag.GridGroupLmlTag}
 * @see GridGroupExt
 */
public class GridGroupExtLmlTag extends AbstractGroupLmlTag {

    public GridGroupExtLmlTag(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
        super(parser, parentTag, rawTagData);
    }

    @Override
    protected Group getNewInstanceOfGroup(final LmlActorBuilder builder) {
        return new GridGroupExt();
    }

    public static class TagProvider implements LmlTagProvider {
        @Override
        public LmlTag create(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
            return new GridGroupExtLmlTag(parser, parentTag, rawTagData);
        }
    }
}

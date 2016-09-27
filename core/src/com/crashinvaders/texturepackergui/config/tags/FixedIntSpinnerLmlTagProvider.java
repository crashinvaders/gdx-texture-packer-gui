package com.crashinvaders.texturepackergui.config.tags;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;

/**
 * Temporary patch for https://github.com/kotcrab/vis-editor/issues/219
 *
 * @author MJ
 * @author Metaphore
 */
public class FixedIntSpinnerLmlTagProvider implements LmlTagProvider {
    @Override
    public LmlTag create(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
        return new FixedIntSpinnerLmlTag(parser, parentTag, rawTagData);
    }
}
package com.crashinvaders.texturepackergui.config.macro;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;

public class TextureFormatLmlMacroTagProvider implements LmlTagProvider {
    @Override
    public LmlTag create(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
        return new TextureFormatLmlMacroTag(parser, parentTag, rawTagData);
    }
}

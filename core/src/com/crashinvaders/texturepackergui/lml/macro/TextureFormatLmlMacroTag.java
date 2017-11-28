package com.crashinvaders.texturepackergui.lml.macro;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.AbstractMacroLmlTag;
import com.github.czyzby.lml.parser.tag.LmlTag;

public class TextureFormatLmlMacroTag extends AbstractMacroLmlTag {

    public TextureFormatLmlMacroTag(LmlParser parser, LmlTag parentTag, StringBuilder rawTagData) {
        super(parser, parentTag, rawTagData);
    }

    @Override
    public void handleDataBetweenTags(CharSequence rawData) {

    }
}

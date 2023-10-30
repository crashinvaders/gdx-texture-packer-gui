package com.crashinvaders.texturepackergui.lml.attributes;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.kotcrab.vis.ui.widget.VisCheckBox;

public class CheckBoxSpacingLmlAttribute implements LmlAttribute<VisCheckBox> {
    @Override
    public Class<VisCheckBox> getHandledType() {
        return VisCheckBox.class;
    }

    @Override
    public void process(LmlParser parser, LmlTag tag, final VisCheckBox actor, String rawAttributeData) {
        float spacing = parser.parseFloat(rawAttributeData);
        actor.getCells().get(1).padLeft(spacing);
    }
}

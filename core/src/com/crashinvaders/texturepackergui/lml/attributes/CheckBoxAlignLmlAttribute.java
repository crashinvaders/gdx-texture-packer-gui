package com.crashinvaders.texturepackergui.lml.attributes;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.util.LmlUtilities;
import com.kotcrab.vis.ui.widget.VisCheckBox;

public class CheckBoxAlignLmlAttribute implements LmlAttribute<VisCheckBox> {
    @Override
    public Class<VisCheckBox> getHandledType() {
        return VisCheckBox.class;
    }

    @Override
    public void process(LmlParser parser, LmlTag tag, final VisCheckBox actor, String rawAttributeData) {
        int alignment = LmlUtilities.parseAlignment(parser, actor, rawAttributeData);
        actor.getCells().get(0).align(alignment);
    }
}

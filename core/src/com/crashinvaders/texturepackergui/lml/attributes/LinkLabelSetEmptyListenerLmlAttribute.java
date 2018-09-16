package com.crashinvaders.texturepackergui.lml.attributes;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.kotcrab.vis.ui.widget.LinkLabel;

public class LinkLabelSetEmptyListenerLmlAttribute implements LmlAttribute<LinkLabel> {
    @Override
    public Class<LinkLabel> getHandledType() {
        return LinkLabel.class;
    }

    @Override
    public void process(final LmlParser parser, final LmlTag tag, final LinkLabel actor, final String rawAttributeData) {
        if (parser.parseBoolean(rawAttributeData)) {
            actor.setListener(new LinkLabel.LinkLabelListener() {
                @Override
                public void clicked(String url) {
                    // Do nothing.
                }
            });
        }
    }
}

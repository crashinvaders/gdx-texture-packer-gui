
package com.crashinvaders.texturepackergui.config.attributes;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.kotcrab.vis.ui.widget.VisImage;

public class ImageDrawableLmlAttribute implements LmlAttribute<VisImage> {
    @Override
    public Class<VisImage> getHandledType() {
        return VisImage.class;
    }

    @Override
    public void process(final LmlParser parser, final LmlTag tag, final VisImage actor, final String rawAttributeData) {
        Drawable drawable = parser.getData().getDefaultSkin().getDrawable(parser.parseString(rawAttributeData, actor));
        actor.setDrawable(drawable);
    }
}

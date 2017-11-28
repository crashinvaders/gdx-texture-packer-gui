package com.crashinvaders.texturepackergui.lml.attributes;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.kotcrab.vis.ui.widget.MenuItem;

public class MenuItemFillImageLmlAttribute implements LmlAttribute<MenuItem> {
    @Override
    public Class<MenuItem> getHandledType() {
        return MenuItem.class;
    }

    @Override
    public void process(final LmlParser parser, final LmlTag tag, final MenuItem actor, final String rawAttributeData) {
        Image image = new Image(parser.getData().getDefaultSkin().getDrawable(rawAttributeData));
        Container<Image> imageContainer = new Container<>(image);
        imageContainer.setFillParent(true);
        imageContainer.align(Align.left);
        imageContainer.padLeft(25f);
        actor.addActor(imageContainer);
    }
}

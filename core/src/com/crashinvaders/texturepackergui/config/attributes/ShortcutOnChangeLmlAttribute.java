package com.crashinvaders.texturepackergui.config.attributes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.crashinvaders.texturepackergui.App;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.attribute.OnChangeLmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.kotcrab.vis.ui.widget.MenuItem;

/**
 * Same old {OnChangeLmlAttribute} but with auto assigning of proper shortcut string (if actor supports it)
 */
public class ShortcutOnChangeLmlAttribute extends OnChangeLmlAttribute {
    @Override
    public void process(final LmlParser parser, final LmlTag tag, final Actor actor, final String rawAttributeData) {
        super.process(parser, tag, actor, rawAttributeData);
        assignShortcut(actor, rawAttributeData);
    }

    private void assignShortcut(Actor actor, String rawAttributeData) {
        if (actor instanceof MenuItem) {
            MenuItem menuItem = (MenuItem) actor;

            String shortcut = App.inst().getShortcuts().resolveShortcutString(rawAttributeData);
            menuItem.setShortcut(shortcut);
        }
    }
}

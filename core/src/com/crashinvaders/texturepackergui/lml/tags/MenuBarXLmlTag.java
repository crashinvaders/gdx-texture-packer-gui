package com.crashinvaders.texturepackergui.lml.tags;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.actor.TableLmlTag;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBarX;

public class MenuBarXLmlTag extends TableLmlTag {

    private MenuBarX menuBar;

    public MenuBarXLmlTag(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
        super(parser, parentTag, rawTagData);
    }

    @Override
    protected Actor getNewInstanceOfActor(final LmlActorBuilder builder) {
        menuBar = new MenuBarX(builder.getStyleName());
        return menuBar.getTable();
    }

    /** @return managed {@link MenuBarX} object. */
    @Override
    public Object getManagedObject() {
        return menuBar;
    }

    @Override
    protected void addChild(final Actor actor) {
        if (actor instanceof Menu) {
            menuBar.addMenu((Menu) actor);
        } else {
            getParser().throwErrorIfStrict("Menu bars can handle only menu children. Found child: " + actor);
            super.addChild(actor);
        }
    }

    public static class Provider implements LmlTagProvider {
        @Override
        public LmlTag create(LmlParser parser, LmlTag parentTag, StringBuilder rawTagData) {
            return new MenuBarXLmlTag(parser, parentTag, rawTagData);
        }
    }
}

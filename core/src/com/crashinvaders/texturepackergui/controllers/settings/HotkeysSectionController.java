package com.crashinvaders.texturepackergui.controllers.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActionContainer;

public class HotkeysSectionController implements SectionContentController, ActionContainer {

    @Inject InterfaceService interfaceService;

    private Actor rootView;

    @Override
    public void show(Container parent) {
        LmlParser lmlParser = interfaceService.getParser();
        rootView = lmlParser.createView(this, Gdx.files.internal("lml/settings/sectionHotkeys.lml")).first();
        parent.setActor(rootView);

        rootView.addAction(SettingsCommons.getSectionContentInAnimation());
    }

    @Override
    public void hide() {
        rootView.remove();
    }
}

package com.crashinvaders.texturepackergui.controllers.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.crashinvaders.texturepackergui.controllers.ViewportService;
import com.crashinvaders.texturepackergui.views.LanguageSelectBox;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActionContainer;

public class GeneralSectionController implements SectionContentController, ActionContainer {

    @Inject InterfaceService interfaceService;
    @Inject SettingsDialogController settingsDialog;
    @Inject ViewportService viewportService;

    private Actor rootView;

    @Override
    public void show(Container parent) {
        LmlParser lmlParser = interfaceService.getParser();
        rootView = lmlParser.createView(this, Gdx.files.internal("lml/settings/sectionGeneral.lml")).first();
        parent.setActor(rootView);

        rootView.addAction(SettingsCommons.getSectionContentInAnimation());
    }

    @Override
    public void hide() {
        rootView.remove();
    }

    //region Option | Language

    @LmlActor LanguageSelectBox sbxLanguage;

    @LmlAfter
    void viewInitLanguage() {
        sbxLanguage.setItems(
                LanguageSelectBox.Entry.withText("Boonga", null),
                LanguageSelectBox.Entry.withText("Kabum", null),
                LanguageSelectBox.Entry.withText("Gulp", null)
        );

        sbxLanguage.setSelectedIndex(1);
    }

    @LmlAction("createLanguageSelectBox") LanguageSelectBox createLanguageSelectBox() {
        return new LanguageSelectBox();
    }

    //endregion

    //region Option | Interface Scale
    @LmlActor("lblUiScale") Label lblUiScale;
    @LmlActor("sliderUiScale") Slider sliderUiScale;

    @LmlAfter
    void viewInitInterfaceScale() {
        float uiScale = 1f / viewportService.getScale();

        lblUiScale.setText(formatScaleValue(uiScale));
        sliderUiScale.setValue(uiScale);
    }

    @LmlAction("onUiScaleChanged") void onUiScaleChanged() {
        float uiScale = sliderUiScale.getValue();
        lblUiScale.setText(formatScaleValue(uiScale));
    }

    @LmlAction("applyNewUiScale") void applyNewUiScale() {
        float uiScale = 1f / sliderUiScale.getValue();
        viewportService.setScale(uiScale);

        Gdx.app.postRunnable(() ->
                settingsDialog.centerWindow());
    }

    private static String formatScaleValue(float uiScale) {
        return String.format("%.0f%%", uiScale*100f);
    }
    //endregion
}

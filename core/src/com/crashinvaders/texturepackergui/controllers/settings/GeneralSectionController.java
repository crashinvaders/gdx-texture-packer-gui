package com.crashinvaders.texturepackergui.controllers.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Scaling;
import com.crashinvaders.common.scene2d.CustomSelectBox;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.controllers.CommonDialogs;
import com.crashinvaders.texturepackergui.controllers.ViewportService;
import com.crashinvaders.texturepackergui.controllers.extensionmodules.CjkFontExtensionModule;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.Locales;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;

import java.util.Locale;

public class GeneralSectionController implements SectionContentController, ActionContainer {

    @Inject InterfaceService interfaceService;
    @Inject SettingsDialogController settingsDialog;
    @Inject ViewportService viewportService;
    @Inject LocaleService localeService;
    @Inject CommonDialogs commonDialogs;

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

    @LmlActor CustomSelectBox<LanguageEntry, ?> sbxLanguage;

    @LmlAfter
    void viewInitLanguage() {
        sbxLanguage.setItems(
                LanguageEntry.withText("English", AppConstants.LOCALE_EN),
                LanguageEntry.withText("Deutsch", AppConstants.LOCALE_DE),
                LanguageEntry.withText("Русский", AppConstants.LOCALE_RU),
                // In order to render CJK strings we need the CJK font extension to be activated.
                // So we replace strings with hardcoded images in common places where CJK font may be not available.
                LanguageEntry.withImage("custom/language-zh-cn", AppConstants.LOCALE_ZH_CN),
                LanguageEntry.withImage("custom/language-zh-tw", AppConstants.LOCALE_ZH_TW));

        setSelectedLanguage(localeService.getCurrentLocale());

        sbxLanguage.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                LanguageEntry selectedLanguage = sbxLanguage.getSelected();
                onLanguageEntryChanged(selectedLanguage);
            }
        });
    }

    @LmlAction("createLanguageSelectBox") CustomSelectBox<LanguageEntry, Container> createLanguageSelectBox() {
        CustomSelectBox.ViewProducer<LanguageEntry, Container> viewProducer = new CustomSelectBox.ViewProducer<LanguageEntry, Container>() {
            private final Skin skin = VisUI.getSkin();
            private final Drawable selectedBackground = skin.getDrawable("orange");

            @Override
            public Container createView(LanguageEntry item) {
                if (item.displayText != null) {
                    // Textual item.
                    VisLabel label = new VisLabel(item.displayText);
                    Container container = new Container(new Container<>(label).padLeft(4f).padRight(4f));
                    container.align(Align.left);
                    return container;

                } else {
                    // Image based item.
                    Drawable drawable = skin.getDrawable(item.displayImagePath);
                    if (drawable == null)
                        throw new IllegalStateException("Cannot find a drawable for the locale " + item.locale + " for the path " + item.displayImagePath);

                    Image image = new Image(drawable);
                    image.setScaling(Scaling.none);
                    Container container = new Container<>(new Container<>(image).padLeft(4f).padRight(4f));
                    container.align(Align.left);
                    return container;
                }
            }

            @Override
            public void updateView(Container view, LanguageEntry item) {

            }

            @Override
            public void selectView(Container view) {
                view.setBackground(selectedBackground);
            }

            @Override
            public void deselectView(Container view) {
                view.setBackground(null);
            }
        };

        return new CustomSelectBox<>(viewProducer);
    }

    private void setSelectedLanguage(Locale locale) {
        // Select the current locale item.
        String language = locale.getLanguage();
        Array<LanguageEntry> languageEntries = sbxLanguage.getItems();
        for (int i = 0; i < languageEntries.size; i++) {
            LanguageEntry entry = languageEntries.get(i);
            if (language.equals(entry.locale.getLanguage())) {
                sbxLanguage.setSelectedIndex(i);
                break;
            }
        }
    }

    private void onLanguageEntryChanged(LanguageEntry languageEntry) {
        Locale locale = languageEntry.locale;

        // Do not change to the same locale.
        if (locale.getLanguage().equals(localeService.getCurrentLocale().getLanguage()))
            return;

        // Require CJK module to be installed for CJK locales.
        if (CjkFontExtensionModule.isCjkFontRequired(locale) &&
                !commonDialogs.checkExtensionModuleActivated(CjkFontExtensionModule.class)) {

            // Reset the selected language.
            setSelectedLanguage(localeService.getCurrentLocale());
            return;
        }

        Locales.setLocale(locale);
        localeService.setCurrentLocale(locale);

        settingsDialog.requestAppRestart();
    }

    private static class LanguageEntry {
        public final @Null String displayText;
        public final @Null String displayImagePath;
        public final Locale locale;

        public static LanguageEntry withText(String displayName, Locale locale) {
            return new LanguageEntry(displayName, null, locale);
        }

        public static LanguageEntry withImage(String displayImagePath, Locale locale) {
            return new LanguageEntry(null, displayImagePath, locale);
        }

        private LanguageEntry(String displayText, String displayImagePath, Locale locale) {
            this.displayText = displayText;
            this.displayImagePath = displayImagePath;
            this.locale = locale;

            if (displayText == null && displayImagePath == null)
                throw new IllegalStateException("Either displayName or displayImagePath must be provided.");
        }

        @Override
        public String toString() {
            return displayText != null ? displayText : displayImagePath;
        }
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

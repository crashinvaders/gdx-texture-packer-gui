package com.crashinvaders.texturepackergui.controllers.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Pool;
import com.crashinvaders.common.scene2d.ShrinkContainer;
import com.crashinvaders.texturepackergui.controllers.GlobalActions;
import com.crashinvaders.texturepackergui.controllers.shortcuts.GlobalShortcutHandler;
import com.crashinvaders.texturepackergui.controllers.shortcuts.Shortcut;
import com.crashinvaders.texturepackergui.utils.CommonUtils;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.Tooltip;
import com.kotcrab.vis.ui.widget.VisLabel;

public class HotkeysSectionController implements SectionContentController, ActionContainer {

    @Inject InterfaceService interfaceService;
    @Inject GlobalShortcutHandler shortcutHandler;
    @Inject GlobalActions globalActions;
    @Inject LocaleService localeService;


    @LmlActor("hotkeyTable") Table hotkeyTable;
    @LmlActor("parseErrorShrinkContainer") ShrinkContainer parseErrorShrinkContainer;
    @LmlActor("parseErrorListGroup") VerticalGroup parseErrorListGroup;
    @LmlActor("hotkeyListRefreshIndicator") Actor hotkeyListRefreshIndicator;

    private Actor rootView;

    @Override
    public void show(Container parent) {
        LmlParser lmlParser = interfaceService.getParser();
        rootView = lmlParser.createView(this, Gdx.files.internal("lml/settings/sectionHotkeys.lml")).first();
        parent.setActor(rootView);

        rootView.addAction(SettingsCommons.getSectionContentInAnimation());

        refreshHotkeyParseErrors();
        refreshHotkeyTable();
    }

    @Override
    public void hide() {
        rootView.remove();
    }

    @LmlAction("onEditHotkeysClick") void onEditHotkeysClick() {
        globalActions.editCustomHotkeys();
    }

    @LmlAction("onRefreshHotkeysClick") void onRefreshHotkeysClick() {
        shortcutHandler.reloadShortcuts();
        refreshHotkeyParseErrors();
        refreshHotkeyTable();

        if (!shortcutHandler.hasParsingErrors()) {
            animateHotkeyListRefreshIndicator();
        }
    }

    private void refreshHotkeyParseErrors() {
        parseErrorListGroup.clear();
        parseErrorShrinkContainer.setVisible(false);

        Array<Exception> parseErrors = shortcutHandler.getParsingErrors();
        if (parseErrors.size == 0)
            return;

        LmlParser parser = interfaceService.getParser();
        final String argParsingErrorText = "parsingErrorText";

        for (Exception parseError : parseErrors) {
            String errorText = CommonUtils.fetchMessageStack(parseError, "\n");
            parser.getData().addArgument(argParsingErrorText, errorText);
            Actor errorItemView = parser.parseTemplate(Gdx.files.internal("lml/settings/hotkeyParseErrorItem.lml")).first();
            parseErrorListGroup.addActor(errorItemView);
        }
        parser.getData().removeArgument(argParsingErrorText);

        parseErrorShrinkContainer.setVisible(true);
    }

    private void refreshHotkeyTable() {
        // Clear any previous records.
        hotkeyTable.clear();

        Skin skin = interfaceService.getSkin();
        HotkeyTableStyle style = skin.get(HotkeyTableStyle.class);

        Array<Shortcut> shortcuts = shortcutHandler.getShortcuts();
        for (int i = 0; i < shortcuts.size; i++) {
            Shortcut shortcut = shortcuts.get(i);

            Drawable rowBackground = i % 2 == 0 ? style.rowBackgroundEven : style.rowBackgroundOdd;

            String actionName = shortcut.getActionDisplayName();
            Label lblAction = new Label(actionName, style.actionLabelStyle);

            hotkeyTable.add(
                    new Container(lblAction).left().background(rowBackground).padLeft(6).padRight(6)
            ).fill();

            Table tableKeyList = new Table();
            tableKeyList.align(Align.topLeft);

            if (shortcut.isControl())
                tableKeyList.add(new KeyEntryWidget(style.keyEntryStyle).setKey("CTRL", true)).padRight(4f);
            if (shortcut.isAlt())
                tableKeyList.add(new KeyEntryWidget(style.keyEntryStyle).setKey("ALT", true)).padRight(4f);
            if (shortcut.isShift())
                tableKeyList.add(new KeyEntryWidget(style.keyEntryStyle).setKey("SHIFT", true)).padRight(4f);
            if (shortcut.isSym())
                tableKeyList.add(new KeyEntryWidget(style.keyEntryStyle).setKey(Shortcut.getSymKeyName(), true)).padRight(4f);

            String keyDisplayText = Input.Keys.toString(shortcut.getKeyCode()).toUpperCase();
            tableKeyList.add(new KeyEntryWidget(style.keyEntryStyle).setKey(keyDisplayText, false));

            hotkeyTable.add(
                    new Container(tableKeyList).left().background(rowBackground).pad(8f)
            ).fill().growX();

            Image imgStateIndicator = new Image(skin, "custom/shortcut-custom-indicator");
            Container<Image> stateIndicatorContainer = new Container<>(imgStateIndicator);
            stateIndicatorContainer.setBackground(rowBackground, false);
            hotkeyTable.add(stateIndicatorContainer.padLeft(12).padRight(12f)).fill();

            boolean isCustomShortcut = shortcut.isUserDefined();

            imgStateIndicator.setVisible(isCustomShortcut);

            if (isCustomShortcut) {
                final Tooltip tooltip = new Tooltip();
                tooltip.clearChildren(); // Remove the empty cell with predefined paddings.
                tooltip.add(new VisLabel(getString("dSettingsHkCustomHk")))
                        .center().pad(1f, 8f, 2f, 8f);
                tooltip.pack();
                tooltip.setTarget(imgStateIndicator);
            }

            hotkeyTable.row();
        }
    }

    private void animateHotkeyListRefreshIndicator() {
        hotkeyListRefreshIndicator.clearActions();
        hotkeyListRefreshIndicator.addAction(Actions.sequence(
                Actions.alpha(1f),
                Actions.visible(true),
                Actions.alpha(0f, 1f),
                Actions.visible(false)
        ));
    }

    /** @return localized string */
    private String getString(String key) {
        return localeService.getI18nBundle().get(key);
    }

    public static class KeyEntryWidget extends Container<Label> implements Pool.Poolable {

        private final Style style;
        private final Label lblKey;

        public KeyEntryWidget(Style style) {
            this.style = style;

            lblKey = new Label("", style.keyLabelStyle);
            this.setActor(lblKey);
            this.setBackground(style.background, true);
            this.setTouchable(Touchable.disabled);
        }

        public KeyEntryWidget setKey(String displayText, boolean isModifierKey) {
            lblKey.setText(displayText);
            lblKey.setStyle(resolveLabelStyle(isModifierKey));

            return this;
        }

        @Override
        public void reset() {
            lblKey.setText("");
        }

        private Label.LabelStyle resolveLabelStyle(boolean isModifierKey) {
            return isModifierKey && style.modifierLabelStyle != null ? style.modifierLabelStyle : style.keyLabelStyle;
        }

        public static class Style {
            public Label.LabelStyle keyLabelStyle;
            @Null
            public Label.LabelStyle modifierLabelStyle;
            @Null
            public Drawable background;
        }
    }

    public static class HotkeyTableStyle {
        public Label.LabelStyle actionLabelStyle;
        public KeyEntryWidget.Style keyEntryStyle;
        @Null
        public Drawable rowBackgroundOdd;
        @Null
        public Drawable rowBackgroundEven;
    }

//    private static class KeyEntryWidgetPool extends Pool<KeyEntryWidget> {
//
//        private final KeyEntryWidget.Style style;
//
//        public KeyEntryWidgetPool(KeyEntryWidget.Style style
//        ) {
//            this.style = style;
//        }
//
//        @Override
//        protected KeyEntryWidget newObject() {
//            return new KeyEntryWidget(style);
//        }
//    }
}

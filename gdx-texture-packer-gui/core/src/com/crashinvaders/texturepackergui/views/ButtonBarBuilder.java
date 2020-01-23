package com.crashinvaders.texturepackergui.views;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.ButtonBar;

/**
 * Fluent interface to construct ButtonBar
 */
public class ButtonBarBuilder {

    private final ButtonBar buttonBar;

    public ButtonBarBuilder() {
        buttonBar = new ButtonBar();
    }

    public ButtonBarBuilder ignoreSpacing(boolean ignoreSpacing) {
        buttonBar.setIgnoreSpacing(ignoreSpacing);
        return this;
    }

    public ButtonBarBuilder order(String order) {
        buttonBar.setOrder(order);
        return this;
    }

    public ButtonBarBuilder button(ButtonBar.ButtonType type, ChangeListener listener) {
        buttonBar.setButton(type, listener);
        return this;
    }

    public ButtonBarBuilder button(ButtonBar.ButtonType type, String text, ChangeListener listener) {
        buttonBar.setButton(type, text, listener);
        return this;
    }

    public ButtonBarBuilder button(ButtonBar.ButtonType type, Button button) {
        buttonBar.setButton(type, button);
        return this;
    }

    public ButtonBarBuilder button(ButtonBar.ButtonType type, Button button, ChangeListener listener) {
        buttonBar.setButton(type, button, listener);
        return this;
    }

    public ButtonBar prepare() {
        return buttonBar;
    }
}

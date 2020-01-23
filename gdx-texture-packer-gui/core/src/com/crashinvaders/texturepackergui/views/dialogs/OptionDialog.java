package com.crashinvaders.texturepackergui.views.dialogs;

import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.Sizes;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.OptionDialogListener;
import com.kotcrab.vis.ui.widget.*;

/** Adapted version of {@link Dialogs.OptionDialog}. */
public class OptionDialog extends VisWindow {

    private final ButtonBar buttonBar;

    public OptionDialog (String title, String text, Dialogs.OptionDialogType type, final OptionDialogListener listener) {
        super(title);

        setModal(true);
        setResizable(false);
        setMovable(false);
        this.padLeft(8f).padRight(8f).padBottom(4f);

//        getTitleLabel().setColor(Colors.get("text-grey"));

        VisLabel lblMessage = new VisLabel(text, Align.left);
        lblMessage.setColor(Colors.get("text-grey"));

        add(lblMessage).spaceTop(8f);
        row().padTop(4f);
        defaults().space(6f);
        defaults().padBottom(3f);

        buttonBar = new ButtonBar();
        buttonBar.setIgnoreSpacing(true);

        ChangeListener yesBtnListener = new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                listener.yes();
                fadeOut();
            }
        };

        ChangeListener noBtnListener = new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                listener.no();
                fadeOut();
            }
        };

        ChangeListener cancelBtnListener = new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                listener.cancel();
                fadeOut();
            }
        };

        switch (type) {
            case YES_NO:
                buttonBar.setButton(ButtonBar.ButtonType.YES, yesBtnListener);
                buttonBar.setButton(ButtonBar.ButtonType.NO, noBtnListener);
                break;
            case YES_CANCEL:
                buttonBar.setButton(ButtonBar.ButtonType.YES, yesBtnListener);
                buttonBar.setButton(ButtonBar.ButtonType.CANCEL, cancelBtnListener);
                break;
            case YES_NO_CANCEL:
                buttonBar.setButton(ButtonBar.ButtonType.YES, yesBtnListener);
                buttonBar.setButton(ButtonBar.ButtonType.NO, noBtnListener);
                buttonBar.setButton(ButtonBar.ButtonType.CANCEL, cancelBtnListener);
                break;
        }

        add(createButtonTable()).right();

        pack();
        centerWindow();
    }

    /**
     * This is improved version of {@link ButtonBar#createTable()} with customized layout.
     */
    public VisTable createButtonTable() {
        String order = buttonBar.getOrder();
        boolean ignoreSpacing = buttonBar.isIgnoreSpacing();

        VisTable table = new VisTable(true);
        table.defaults().fillX().uniformX();
        table.right();

        boolean spacingValid = false;
        for (int i = 0; i < order.length(); i++) {
            char ch = order.charAt(i);

            if (ignoreSpacing == false && ch == ' ' && spacingValid) {
                table.add().width(4f);
                spacingValid = false;
            }

            ButtonBar.ButtonType buttonType = findButtonTypeForId(ch);
            if (buttonType == null) {
                continue;
            }

            Button button = buttonBar.getButton(buttonType);

            if (button != null) {
                ((VisTextButton)button).setFocusBorderEnabled(false);
                table.add(button);
                spacingValid = true;
            }
        }

        return table;
    }

    public OptionDialog setNoButtonText (String text) {
        buttonBar.getTextButton(ButtonBar.ButtonType.NO).setText(text);
        pack();
        return this;
    }

    public OptionDialog setYesButtonText (String text) {
        buttonBar.getTextButton(ButtonBar.ButtonType.YES).setText(text);
        pack();
        return this;
    }

    public OptionDialog setCancelButtonText (String text) {
        buttonBar.getTextButton(ButtonBar.ButtonType.CANCEL).setText(text);
        pack();
        return this;
    }

    public static OptionDialog show(Stage stage, String title, String text, Dialogs.OptionDialogType type, OptionDialogListener listener) {
        OptionDialog dialog = new OptionDialog(title, text, type, listener);
        stage.addActor(dialog.fadeIn());
        return dialog;
    }

    private static ButtonBar.ButtonType findButtonTypeForId(char id) {
        for (ButtonBar.ButtonType buttonType : ButtonBar.ButtonType.values()) {
            if (buttonType.getId() == id) return buttonType;
        }
        return null;
    }
}

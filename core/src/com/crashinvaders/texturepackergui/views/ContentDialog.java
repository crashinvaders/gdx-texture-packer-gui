package com.crashinvaders.texturepackergui.views;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.crashinvaders.common.scene2d.Scene2dUtils;
import com.kotcrab.vis.ui.widget.ButtonBar;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisTable;

public class ContentDialog extends VisDialog {

    private final Actor content;

    public ContentDialog(String title, Actor content) {
        super(title);
        this.content = content;

        getContentTable().padTop(8f);
        getContentTable().add(content);

        getTitleTable().padLeft(4f).padTop(4f);

        pack();
        centerWindow();
    }

    public void setupButtons(ButtonBar buttonBar) {
        buttonBar.setIgnoreSpacing(true);
        buttonBar.setOrder(ButtonBar.WINDOWS_ORDER);
        VisTable buttonTable = buttonBar.createTable();
        buttonTable.right();
        buttonTable.getCells().forEach(cell -> {
            cell.uniformX();
            cell.fillX();
            Table button = (Table) cell.getActor();
            button.padLeft(16f);
            button.padRight(16f);
        });

        getContentTable().row().spaceTop(12f);
        getContentTable().add(buttonTable).growX();

        // Try resolve as positive action on "Enter" press.
        Button positiveButton = FindPositiveButton(buttonBar);
        if (positiveButton != null) {
            addListener(new InputListener() {
                @Override
                public boolean keyDown(InputEvent event, int keycode) {
                    if (keycode != Input.Keys.ENTER) return false;

                    if (positiveButton.isDisabled()) {
                        Scene2dUtils.simulateClick(positiveButton);
                    }
                    return true;
                }
            });
        }

        pack();
        centerWindow();
    }

    @SuppressWarnings("unchecked")
    public <T extends Actor> T getContent() {
        return (T) content;
    }

    private static Button FindPositiveButton(ButtonBar buttonBar) {
        Button button = null;

        button = buttonBar.getButton(ButtonBar.ButtonType.APPLY);
        if (button != null) return button;
        button = buttonBar.getButton(ButtonBar.ButtonType.OK);
        if (button != null) return button;
        button = buttonBar.getButton(ButtonBar.ButtonType.FINISH);
        if (button != null) return button;
        button = buttonBar.getButton(ButtonBar.ButtonType.YES);
        if (button != null) return button;

        return null;
    }
}

package com.crashinvaders.texturepackergui.views;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kotcrab.vis.ui.widget.ButtonBar;
import com.kotcrab.vis.ui.widget.VisDialog;

public class ContentDialog extends VisDialog {

    private final Actor content;

    public ContentDialog(String title, Actor content) {
        super(title);
        this.content = content;

        this.padBottom(0);
        getContentTable().padTop(4);
        getContentTable().add(content);

        pack();
        centerWindow();
    }

	public void setupButtons (ButtonBar buttonBar) {
		buttonBar.setIgnoreSpacing(true);
		getContentTable().row().spaceTop(8);
		getContentTable().add(buttonBar.createTable());

		pack();
		centerWindow();
	}

    @SuppressWarnings("unchecked")
    public <T extends Actor> T getContent() {
        return (T)content;
    }
}

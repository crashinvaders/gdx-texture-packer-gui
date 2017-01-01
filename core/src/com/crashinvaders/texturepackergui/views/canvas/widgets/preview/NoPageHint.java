package com.crashinvaders.texturepackergui.views.canvas.widgets.preview;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.crashinvaders.texturepackergui.App;

class NoPageHint extends Container {
    public NoPageHint(Skin skin) {
        String text = App.inst().getI18n().get("atlasPreviewNoPageMsg");

        Label lblMessage = new Label(text, new Label.LabelStyle(skin.getFont("default-font"), Color.WHITE));
        lblMessage.setAlignment(Align.center);
        lblMessage.getColor().a = 0.25f;
        setActor(lblMessage);

        setFillParent(true);
        align(Align.center);
        setTouchable(Touchable.disabled);
        setBackground(skin.getDrawable("noPreviewFill"));
    }
}

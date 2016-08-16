package com.crashinvaders.texturepackergui.views.canvas.widgets.preview;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

class NoPageHint extends Container<Label> {
    public NoPageHint(Skin skin) {
        Label lblMessage = new Label("[#ffffffaa]PACK ATLAS TO SEE PAGES", new Label.LabelStyle(skin.getFont("default-font"), new Color(0xffffffff)));
        setActor(lblMessage);

        setFillParent(true);
        align(Align.center);
        setTouchable(Touchable.disabled);
        setBackground(skin.newDrawable("white", new Color(0x000000aa)), false);
    }
}

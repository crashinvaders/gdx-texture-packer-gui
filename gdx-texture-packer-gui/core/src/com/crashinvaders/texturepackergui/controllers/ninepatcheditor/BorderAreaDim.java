package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class BorderAreaDim extends Widget {
    private final Drawable drawable;
    private final Actor actor;

    public BorderAreaDim(Skin skin, Actor actor) {
        drawable = skin.getDrawable("white");
        this.actor = actor;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color col = getColor();
        batch.setColor(col.r, col.g, col.b, col.a * parentAlpha);

        drawable.draw(batch, getX(), getY(), getWidth(), actor.getY());
        drawable.draw(batch, getX(), getY() + actor.getY() + actor.getHeight(), getWidth(), getHeight() - (actor.getY() + actor.getHeight()));
        drawable.draw(batch, getX(), getY() + actor.getY(), actor.getX(), actor.getHeight());
        drawable.draw(batch, getX() + (actor.getX() + actor.getWidth()), getY() + actor.getY(), getWidth() - (actor.getX() + actor.getWidth()), actor.getHeight());
    }
}

package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class ContentGrid extends PatchGrid {

    private final Drawable contentAreaDrawable;

    private Action flickerAction;

    public ContentGrid(Skin skin, Color primaryColor) {
        super(skin, primaryColor);
        contentAreaDrawable = ((TextureRegionDrawable)skin.getDrawable("white"))
                .tint(new Color(primaryColor.r, primaryColor.g, primaryColor.b, 0.25f));
    }

    @Override
    protected void drawAreaGraphics(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        contentAreaDrawable.draw(batch, getX(), getY(), left.getX(), getHeight());
        contentAreaDrawable.draw(batch, getX() + right.getX(), getY(), getWidth() - right.getX(), getHeight());
        contentAreaDrawable.draw(batch, getX() + left.getX(), getY(), right.getX() - left.getX(), bottom.getY());
        contentAreaDrawable.draw(batch, getX() + left.getX(), getY() + top.getY(), right.getX() - left.getX(), getHeight() - top.getY());
    }
}

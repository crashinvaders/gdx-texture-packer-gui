package com.crashinvaders.texturepackergui.views.canvas.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;

import java.util.Locale;

public class InfoPanel extends Container<VerticalGroup> {
    private final Label lblCurrentPage;
    private final Label lblZoom;

    private int pagesAmount, currentPage;

    public InfoPanel(Skin skin) {
        setTouchable(Touchable.disabled);

        setBackground(skin.newDrawable("white", new Color(0x333333ff)));

        // Labels
        {
            VerticalGroup verticalGroup = new VerticalGroup();
            verticalGroup.align(Align.left);
            verticalGroup.space(0f);

            Label.LabelStyle labelStyle = new Label.LabelStyle(skin.getFont("default-font"), Color.WHITE);
            lblCurrentPage = new Label("", labelStyle);
            lblZoom = new Label("", labelStyle);

            verticalGroup.addActor(lblCurrentPage);
            verticalGroup.addActor(lblZoom);

            setActor(verticalGroup);
        }

        pad(4f, 12f, 4f, 12f);

        updatePagesText();
        setZoomLevel(100f);
    }

    public void setZoomLevel(float zoom) {
        lblZoom.setText(String.format(Locale.US, "Zoom: %.0f%%", zoom));
    }

    public void setPagesAmount(int pagesAmount) {
        this.pagesAmount = pagesAmount;
        updatePagesText();
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        updatePagesText();
    }

    private void updatePagesText() {
        if (pagesAmount <= 0) {
            lblCurrentPage.setText("No page to show");
        } else {
            lblCurrentPage.setText("Page " + currentPage + " / " + pagesAmount);
        }
    }
}

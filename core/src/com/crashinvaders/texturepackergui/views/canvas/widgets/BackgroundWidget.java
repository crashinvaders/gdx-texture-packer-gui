package com.crashinvaders.texturepackergui.views.canvas.widgets;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;

public class BackgroundWidget extends Stack {

    private final Image imgFill;

    private boolean initialized = false;

    public BackgroundWidget(Skin skin) {
        this.imgFill = new Image(skin.getTiledDrawable("custom/transparent-light"));
        addActor(imgFill);
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);

        if (stage != null && !initialized) {
            initialized = true;
            initialize();
        }
        if (stage == null && initialized) {
            initialized = false;
            dispose();
        }
    }

    private void initialize() {
//        eventDispatcher.addListener(new EventListener<ProjectPropertyChangedEvent>() {
//            @Override
//            public boolean processEvent(ProjectPropertyChangedEvent event) {
//                if (!initialized) return false;
//
//                return true;
//            }
//        }, ProjectPropertyChangedEvent.class, true);
    }

    private void dispose() {

    }
}

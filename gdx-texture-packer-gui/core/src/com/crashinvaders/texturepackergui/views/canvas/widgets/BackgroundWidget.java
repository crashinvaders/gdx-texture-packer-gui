package com.crashinvaders.texturepackergui.views.canvas.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.events.ProjectInitializedEvent;
import com.crashinvaders.texturepackergui.events.ProjectPropertyChangedEvent;
import com.github.czyzby.autumn.processor.event.EventListener;

public class BackgroundWidget extends Stack {

    private final Image imgFill;

    private boolean initialized = false;
    private CommonEventListener eventListener;

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
        eventListener = new CommonEventListener();
        App.inst().getEventDispatcher().addListener(eventListener, ProjectPropertyChangedEvent.class, true);
        App.inst().getEventDispatcher().addListener(eventListener, ProjectInitializedEvent.class, true);

        onBackgroundColorChanged(App.inst().getModelService().getProject().getPreviewBackgroundColor());
    }

    private void dispose() {
        App.inst().getEventDispatcher().removeListener(eventListener, ProjectPropertyChangedEvent.class);
        App.inst().getEventDispatcher().removeListener(eventListener, ProjectInitializedEvent.class);
    }

    private void onBackgroundColorChanged(Color color) {
        imgFill.setColor(color);
    }

    private class CommonEventListener implements EventListener<Object> {
        @Override
        public boolean processEvent(Object eventObject) {
            if (eventObject instanceof ProjectPropertyChangedEvent) {
                ProjectPropertyChangedEvent event = (ProjectPropertyChangedEvent) eventObject;
                if (event.getProperty() == ProjectPropertyChangedEvent.Property.PREVIEW_BG_COLOR) {
                    onBackgroundColorChanged(event.getProject().getPreviewBackgroundColor());
                }
            }
            else if (eventObject instanceof ProjectInitializedEvent) {
                ProjectInitializedEvent event = (ProjectInitializedEvent) eventObject;
                onBackgroundColorChanged(event.getProject().getPreviewBackgroundColor());
            }
            return true;
        }
    }
}

package com.crashinvaders.texturepackergui.views.seekbar;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Pools;
import com.kotcrab.vis.ui.FocusManager;
import com.kotcrab.vis.ui.Focusable;
import com.kotcrab.vis.ui.util.BorderOwner;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

//TODO Implement Disableable.
public class SeekBar extends Table implements Focusable, BorderOwner {

    private final SeekBarModel model;
    private final Style style;
    private final VisValidatableTextField textField;
    private final VisSlider slider;

    private ChangeEventPolicy changeEventPolicy = ChangeEventPolicy.ON_FOCUS_LOST;

    private boolean focusBorderEnabled = true;
    private boolean focused = false;

    private boolean programmaticChangeEvents = true;
    private boolean listenTextFieldChangeEvents = false;
    private boolean listenSliderChangeEvents = false;

    public SeekBar(final SeekBarModel model, final Style style) {
        this.model = model;
        this.style = style;
        this.setTransform(false);

        textField = new VisValidatableTextField("", style.textFieldStyle);
        slider = new VisSlider(0f, 1f, 0.01f, false, style.sliderStyle);

        this.add(textField).width(style.textFieldWidth);
        this.add(slider).minWidth(72f).growX();

        addListener(new InputListener() {
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                /*if (isDisabled() == false)*/ FocusManager.switchFocus(getStage(), SeekBar.this);
                return false;
            }

            @Override
            public boolean handle(Event e) {
                if (super.handle(e)) return true;

                if (!(e instanceof InputEvent)) return false;
                InputEvent event = (InputEvent)e;
                switch (event.getType()) {
                    case enter:
                    case exit:
                        return textField.notify(e, false);
                    default:
                        return false;
                }
            }
        });

        // Register view listeners.
        {
            textField.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    event.stop();
                    if (!listenTextFieldChangeEvents) return;

                    model.onTextValueChanged();
                    if (textField.isInputValid() && changeEventPolicy == ChangeEventPolicy.ON_TEXT_SLIDER_CHANGED) {
                        notifyValueChanged(false, true, true);
                    }
                }
            });

            textField.addListener(new InputListener() {
                @Override
                public boolean keyDown (InputEvent event, int keycode) {
                    if (keycode == Input.Keys.ENTER) {
                        notifyValueChanged(false, false, true);
                        return true;
                    }
                    return false;
                }
            });

            slider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    event.stop();
                    if (!listenSliderChangeEvents) return;

                    model.onSliderValueChanged();
                    if (changeEventPolicy == ChangeEventPolicy.ON_TEXT_SLIDER_CHANGED) {
                        notifyValueChanged(false, false, true);
                    }
                    textField.selectAll();
                }
            });
        }

        model.bind(this);

        setListenTextChangeEvents(true);
        setListenSliderChangeEvents(true);
    }

    @Override
    public boolean isFocusBorderEnabled() {
        return focusBorderEnabled;
    }

    @Override
    public void setFocusBorderEnabled(boolean focusBorderEnabled) {
        this.focusBorderEnabled = focusBorderEnabled;
    }

    @Override
    public void focusGained() {
        focused = true;

        getStage().setKeyboardFocus(textField);
        textField.focusGained();
    }

    @Override
    public void focusLost() {
        focused = false;

        notifyValueChanged(true, true, false);
        if (changeEventPolicy == ChangeEventPolicy.ON_FOCUS_LOST) {
            notifyValueChanged(false, false, true);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (focused && focusBorderEnabled && style.focusBorder != null) {
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            style.focusBorder.draw(batch, getX(), getY(), getWidth(), getHeight());
        }
    }

    public VisValidatableTextField getTextField() {
        return textField;
    }

    public VisSlider getSlider() {
        return slider;
    }

    /** If false, methods changing spinner value form code won't trigger change event, it will be fired only when user has changed value. */
    public void setProgrammaticChangeEvents (boolean programmaticChangeEvents) {
        this.programmaticChangeEvents = programmaticChangeEvents;
    }

    public boolean isProgrammaticChangeEvents () {
        return programmaticChangeEvents;
    }

    /**
     * Called by {@link SeekBarModel}. Notifies when underlying model value has changed and text field and slider must update.
     * Typically there is no need to call this method manually.
     * @param fireEvent if true then {@link ChangeListener.ChangeEvent} will be fired
     */
    public void notifyValueChanged (boolean updateTextField, boolean updateSlider, boolean fireEvent) {
        if (updateTextField) {
            VisValidatableTextField textField = getTextField();
            int cursor = textField.getCursorPosition();
            textField.setCursorPosition(0);
            this.setListenTextChangeEvents(false);
            textField.setText(model.prepareTextValue());
            this.setListenTextChangeEvents(true);
            textField.setCursorPosition(cursor);
        }

        if (updateSlider) {
            VisSlider slider = getSlider();
            this.setListenSliderChangeEvents(false);
            slider.setValue(model.prepareSliderValue());
            this.setListenSliderChangeEvents(true);
        }

        if (fireEvent) {
            ChangeListener.ChangeEvent changeEvent = Pools.obtain(ChangeListener.ChangeEvent.class);
            fire(changeEvent);
            Pools.free(changeEvent);
        }
    }

    public void setListenTextChangeEvents(boolean listenChangeEvents) {
        this.listenTextFieldChangeEvents = listenChangeEvents;
    }

    public void setListenSliderChangeEvents(boolean listenChangeEvents) {
        this.listenSliderChangeEvents = listenChangeEvents;
    }

    public ChangeEventPolicy getChangeEventPolicy() {
        return changeEventPolicy;
    }

    public void setChangeEventPolicy(ChangeEventPolicy changeEventPolicy) {
        this.changeEventPolicy = changeEventPolicy;
    }

    public SeekBarModel getModel() {
        return model;
    }

    public Style getStyle() {
        return style;
    }

    public static class Style {
        public VisTextField.VisTextFieldStyle textFieldStyle;
        public Slider.SliderStyle sliderStyle;
        public float textFieldWidth = 54f;
        /** Optional */
        public Drawable focusBorder;
    }

    /** @see Spinner.TextFieldEventPolicy */
    public enum ChangeEventPolicy {
        /**
         * Spinner change event will be only fired after user has pressed enter in text field.
         */
        ON_ENTER_ONLY,
        /**
         * Seek bar change event will be always fired after it has lost focus and entered value is valid. Note
         * that event will be fired even if user has not changed actual value of seek bar.
         * This mode is the default one.
         */
        ON_FOCUS_LOST,
        /**
         * Seek bar change event will be fired right after user has dragged slider or typed something in the text field and model has
         * determined that entered value is valid. Event won't be fired if entered value is invalid.
         */
        ON_TEXT_SLIDER_CHANGED
    }
}

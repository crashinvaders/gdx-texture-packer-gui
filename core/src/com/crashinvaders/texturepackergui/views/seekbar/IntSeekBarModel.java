package com.crashinvaders.texturepackergui.views.seekbar;

import com.kotcrab.vis.ui.util.InputValidator;
import com.kotcrab.vis.ui.util.IntDigitsOnlyFilter;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;

/** Based on {@link IntSpinnerModel}. */
public class IntSeekBarModel implements SeekBarModel {
    private BoundsValidator boundsValidator = new BoundsValidator();
    private IntDigitsOnlyFilter textFieldFilter;

    private SeekBar seekBar;

    private int min;
    private int max;
    private int step;
    private int current;

    public IntSeekBarModel(int initialValue, int min, int max, int step) {
        this.min = min;
        this.max = max;
        this.step = step;
        this.current = initialValue;
    }

    @Override
    public void bind(SeekBar seekBar) {
        this.seekBar = seekBar;

        VisValidatableTextField valueText = seekBar.getTextField();
        valueText.getValidators().clear();
        valueText.addValidator(boundsValidator);
        valueText.addValidator(Validators.INTEGERS);
        valueText.setTextFieldFilter(textFieldFilter = new IntDigitsOnlyFilter(true));

        VisSlider slider = seekBar.getSlider();
        slider.setRange(min, max);
        slider.setStepSize(step);
        slider.setValue(current);

        textFieldFilter.setUseFieldCursorPosition(true);
        if (min >= 0) {
            textFieldFilter.setAcceptNegativeValues(false);
        } else {
            textFieldFilter.setAcceptNegativeValues(true);
        }

        seekBar.notifyValueChanged(true, true, true);
    }

    @Override
    public void onTextValueChanged() {
        String text = seekBar.getTextField().getText();
        if (text.equals("")) {
            current = min;
        } else if (checkInputBounds(text)) {
            current = Integer.parseInt(text);
        }

        seekBar.notifyValueChanged(false, true, false);
    }

    @Override
    public void onSliderValueChanged() {
        float value = seekBar.getSlider().getValue();
        current = Math.round(value);

        seekBar.notifyValueChanged(true, false, false);
    }

    @Override
    public String prepareTextValue() {
        return String.valueOf(current);
    }

    @Override
    public float prepareSliderValue() {
        return (float)current;
    }

    public void setValue (int newValue) {
        setValue(newValue, seekBar.isProgrammaticChangeEvents());
    }

    public void setValue (int newValue, boolean fireEvent) {
        if (newValue > max) {
            current = max;
        } else if (newValue < min) {
            current = min;
        } else {
            current = newValue;
        }

        seekBar.notifyValueChanged(true, true, fireEvent);
    }

    public int getValue () {
        return current;
    }

    public int getMin () {
        return min;
    }

    /** Sets min value. If current is lesser than min, the current value is set to min value. */
    public void setMin (int min) {
        if (min > max) throw new IllegalArgumentException("min can't be > max");

        this.min = min;

        if (min >= 0) {
            textFieldFilter.setAcceptNegativeValues(false);
        } else {
            textFieldFilter.setAcceptNegativeValues(true);
        }

        seekBar.setListenTextChangeEvents(false);
        seekBar.getSlider().setRange(min, max);
        seekBar.setListenTextChangeEvents(true);

        if (current < min) {
            current = min;
            seekBar.notifyValueChanged(true, true, seekBar.isProgrammaticChangeEvents());
        }
    }

    public int getMax () {
        return max;
    }

    /** Sets max value. If current is greater than max, the current value is set to max value. */
    public void setMax (int max) {
        if (min > max) throw new IllegalArgumentException("min can't be < min");

        this.max = max;

        seekBar.setListenTextChangeEvents(false);
        seekBar.getSlider().setRange(min, max);
        seekBar.setListenTextChangeEvents(true);

        if (current > max) {
            current = max;
            seekBar.notifyValueChanged(true, true, seekBar.isProgrammaticChangeEvents());
        }
    }

    public float getStep () {
        return step;
    }

    public void setStep (int step) {
        if (step <= 0) throw new IllegalArgumentException("step must be > 0");

        this.step = step;

        seekBar.setListenTextChangeEvents(false);
        seekBar.getSlider().setStepSize(step);
        seekBar.setListenTextChangeEvents(true);
    }

    private boolean checkInputBounds (String input) {
        try {
            float x = Integer.parseInt(input);
            return x >= min && x <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private class BoundsValidator implements InputValidator {
        @Override
        public boolean validateInput (String input) {
            return checkInputBounds(input);
        }
    }
}

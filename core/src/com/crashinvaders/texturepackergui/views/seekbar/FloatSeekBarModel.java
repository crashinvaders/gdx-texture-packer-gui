/*
 * Copyright 2014-2016 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.crashinvaders.texturepackergui.views.seekbar;

import com.kotcrab.vis.ui.util.*;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import com.kotcrab.vis.ui.widget.spinner.FloatSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;

import java.math.BigDecimal;

/**
 * Spinner models allowing to select float values. Uses float to store values, good for small numbers
 * with low precession. If high precession is required or very big numbers are used then {@link FloatSpinnerModel} should be used.
 * If only ints are needed then {@link IntSpinnerModel} should be used.
 * @author Kotcrab
 * @see FloatSpinnerModel
 * @see IntSpinnerModel
 * @since 1.0.2
 */
public class FloatSeekBarModel implements SeekBarModel {
	private InputValidator boundsValidator = new BoundsValidator();
	private NumberDigitsTextFieldFilter textFieldFilter;

	private SeekBar seekBar;

	private float max;
	private float min;
	private float step;
	private float current;
	private int precision = 0;

	public FloatSeekBarModel(float initialValue, float min, float max, float step, int precision) {
		if (min > max) throw new IllegalArgumentException("min can't be > max");
		if (step <= 0) throw new IllegalArgumentException("step must be > 0");
		if (precision < 0) throw new IllegalArgumentException("precision must be >= 0");

		this.current = initialValue;
		this.max = max;
		this.min = min;
		this.step = step;
		this.precision = precision;
	}

	@Override
	public void bind (SeekBar seekBar) {
		this.seekBar = seekBar;

		setPrecision(precision, false);

		VisSlider slider = seekBar.getSlider();
		slider.setRange(min, max);
		slider.setStepSize(step);
		slider.setValue(current);

		seekBar.notifyValueChanged(true, true, true);
	}

	@Override
	public void onTextValueChanged() {
		String text = seekBar.getTextField().getText();
		if (text.equals("")) {
			current = min;
		} else if (checkInputBounds(text)) {
			current = Float.parseFloat(text);
		}

		seekBar.notifyValueChanged(false, true, false);
	}

	@Override
	public void onSliderValueChanged() {
		current = seekBar.getSlider().getValue();

		seekBar.notifyValueChanged(true, false, false);
	}

	@Override
	public String prepareTextValue() {
		if (precision >= 1) {
			//dealing with float rounding errors
			BigDecimal bd = new BigDecimal(String.valueOf(current));
			bd = bd.setScale(precision, BigDecimal.ROUND_HALF_UP);
			return String.valueOf(bd.floatValue());
		} else {
			return String.valueOf((int) current);
		}
	}

	@Override
	public float prepareSliderValue() {
		return current;
	}

	public int getPrecision () {
		return precision;
	}

	/**
	 * Sets precision of this selector. Precision defines how many digits after decimal point can be entered. By default
	 * this is set to 0, meaning that only integers are allowed. Setting precision to 1 would allow 0.0, precision = 2 would
	 * allow 0.00 and etc.
	 */
	public void setPrecision (final int precision) {
		setPrecision(precision, true);
	}

	private void setPrecision (final int precision, boolean notifySeekBar) {
		if (precision < 0) throw new IllegalStateException("Precision can't be < 0");
		this.precision = precision;

		VisValidatableTextField valueText = seekBar.getTextField();
		valueText.getValidators().clear();
		valueText.addValidator(boundsValidator); //Both need the bounds check
		if (precision == 0) {
			valueText.addValidator(Validators.INTEGERS);
			valueText.setTextFieldFilter(textFieldFilter = new IntDigitsOnlyFilter(true));
		} else {
			valueText.addValidator(Validators.FLOATS);
			valueText.addValidator(new InputValidator() {
				@Override
				public boolean validateInput (String input) {
					int dotIndex = input.indexOf('.');
					if (dotIndex == -1) return true;
					return input.length() - input.indexOf('.') - 1 <= precision;
				}
			});
			valueText.setTextFieldFilter(textFieldFilter = new FloatDigitsOnlyFilter(true));
		}

		textFieldFilter.setUseFieldCursorPosition(true);
		if (min >= 0) {
			textFieldFilter.setAcceptNegativeValues(false);
		} else {
			textFieldFilter.setAcceptNegativeValues(true);
		}

		if (notifySeekBar) {
			seekBar.notifyValueChanged(true, true, seekBar.isProgrammaticChangeEvents());
		}
	}

	public void setValue (float newValue) {
		setValue(newValue, seekBar.isProgrammaticChangeEvents());
	}

	public void setValue (float newValue, boolean fireEvent) {
		if (newValue > max) {
			current = max;
		} else if (newValue < min) {
			current = min;
		} else {
			current = newValue;
		}

		seekBar.notifyValueChanged(true, true, fireEvent);
	}

	public float getValue () {
		return current;
	}

	public float getMin () {
		return min;
	}

	/** Sets min value, if current is lesser than min, the current value is set to min value */
	public void setMin (float min) {
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

	public float getMax () {
		return max;
	}

	/** Sets max value. If current is greater than max, the current value is set to max value. */
	public void setMax (float max) {
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

	public void setStep (float step) {
		if (step <= 0) throw new IllegalArgumentException("step must be > 0");

		this.step = step;

		seekBar.setListenTextChangeEvents(false);
		seekBar.getSlider().setStepSize(step);
		seekBar.setListenTextChangeEvents(true);
	}

	private boolean checkInputBounds (String input) {
		try {
			float x = Float.parseFloat(input);
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

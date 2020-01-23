package com.crashinvaders.texturepackergui.lml.tags.seekbar;

import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.builder.AlignedLmlActorBuilder;

public class FloatSeekBarLmlBuilder extends AlignedLmlActorBuilder {
    private float min = 0f;
    private float max = 1f;
    private float value;
    private float stepSize = (max - min) / 100f;
    private int precision = 1;

    /** MUST be called before using range's numeric values.
     *
     * @param parser used to throw exception if range is invalid. */
    public void validateRange(final LmlParser parser) {
        if (min >= max || stepSize > max - min || value < min || value > max || stepSize <= 0f) {
            parser.throwError(
                    "Range widget not properly constructed. Min value has to be lower than max and step size cannot be higher than the difference between min and max values. Initial value cannot be lower than min or higher than max value. Step size cannot be zero or negative.");
        }
    }

    /** @return range's min value. */
    public float getMin() {
        return min;
    }

    /** @param min becomes range's min value. If current initial value matches current min value, it will also be
     *            adjusted to match the new min value. */
    public void setMin(final float min) {
        if (Float.compare(this.min, value) == 0) {
            value = min;
        }
        this.min = min;
    }

    /** @return range's max value. */
    public float getMax() {
        return max;
    }

    /** @param max range's max value. */
    public void setMax(final float max) {
        this.max = max;
    }

    /** @return range's initial value. */
    public float getValue() {
        return value;
    }

    /** @param value range's initial value. */
    public void setValue(final float value) {
        this.value = value;
    }

    /** @return the smallest step size used to iterate over range. */
    public float getStepSize() {
        return stepSize;
    }

    /** @param stepSize the smallest step size used to iterate over range. */
    public void setStepSize(final float stepSize) {
        this.stepSize = stepSize;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }
}

package com.crashinvaders.texturepackergui.lml.tags.seekbar;

import com.github.czyzby.lml.parser.tag.LmlActorBuilder;

public class IntSeekBarLmlBuilder extends LmlActorBuilder {
    private int min = 0;
    private int max = 100;
    private int step = 1;
    private int value;

    /** @return range start. */
    public int getMin() {
        return min;
    }

    /** @param min range start. */
    public void setMin(final int min) {
        this.min = min;
    }

    /** @return range end. */
    public int getMax() {
        return max;
    }

    /** @param max range end. */
    public void setMax(final int max) {
        this.max = max;
    }

    /** @return lowest possible incrementation value in the range. */
    public int getStep() {
        return step;
    }

    /** @param step lowest possible incrementation value in the range. */
    public void setStep(final int step) {
        this.step = step;
    }

    /** @return initial range value. */
    public int getValue() {
        return value;
    }

    /** @param value initial range value. */
    public void setValue(final int value) {
        this.value = value;
    }
}

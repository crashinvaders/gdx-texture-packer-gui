package com.crashinvaders.common;

import com.badlogic.gdx.utils.Pool;

public class MutableInt implements Pool.Poolable {
    private int value;
    private ChangeListener listener;

    public int get() {
        return value;
    }

    public void set(int value) {
        if (this.value == value) return;

        this.value = value;

        if (listener != null) {
            listener.onValueChanged(this);
        }
    }

    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void reset() {
        value = 0;
        listener = null;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MutableInt that = (MutableInt) o;

        return value == that.value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    public interface ChangeListener {
        void onValueChanged(MutableInt value);
    }
}

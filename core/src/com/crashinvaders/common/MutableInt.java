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

    public interface ChangeListener {
        void onValueChanged(MutableInt value);
    }
}

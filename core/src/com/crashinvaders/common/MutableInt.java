package com.crashinvaders.common;

public class MutableInt {
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

    public interface ChangeListener {
        void onValueChanged(MutableInt value);
    }
}

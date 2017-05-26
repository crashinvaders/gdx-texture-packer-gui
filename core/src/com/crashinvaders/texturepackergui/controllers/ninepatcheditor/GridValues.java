package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.crashinvaders.common.MutableInt;

public class GridValues implements MutableInt.ChangeListener, Pool.Poolable {
    public final MutableInt left = new MutableInt();
    public final MutableInt right = new MutableInt();
    public final MutableInt top = new MutableInt();
    public final MutableInt bottom = new MutableInt();

    private boolean programmaticChangeEvents = true;

    private final Array<ChangeListener> listeners = new Array<>();

    public GridValues() {
        left.setListener(this);
        right.setListener(this);
        top.setListener(this);
        bottom.setListener(this);
    }

    public void addListener(ChangeListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(ChangeListener listener) {
        this.listeners.removeValue(listener, true);
    }

    public boolean hasValues() {
        return left.get() > 0 || right.get() > 0 || bottom.get() > 0 || top.get() > 0;
    }

    public void set(GridValues values) {
        if (this.equals(values)) return;

        programmaticChangeEvents = false;
        left.set(values.left.get());
        right.set(values.right.get());
        top.set(values.top.get());
        bottom.set(values.bottom.get());
        programmaticChangeEvents = true;

        // Notify listeners
        for (int i = 0; i < listeners.size; i++) {
            listeners.get(i).onValuesChanged(this);
        }
    }

    @Override
    public void onValueChanged(MutableInt value) {
        if (programmaticChangeEvents && listeners.size > 0) {
            for (int i = 0; i < listeners.size; i++) {
                listeners.get(i).onValuesChanged(this);
            }
        }
    }

    @Override
    public void reset() {
        listeners.clear();
        left.reset();
        right.reset();
        top.reset();
        bottom.reset();
        left.setListener(this);
        right.setListener(this);
        top.setListener(this);
        bottom.setListener(this);
    }

    @Override
    public String toString() {
        return "[" +
                "left=" + left +
                ", right=" + right +
                ", top=" + top +
                ", bottom=" + bottom +
                ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GridValues that = (GridValues) o;

        if (!left.equals(that.left)) return false;
        if (!right.equals(that.right)) return false;
        if (!top.equals(that.top)) return false;
        return bottom.equals(that.bottom);
    }

    @Override
    public int hashCode() {
        int result = left.hashCode();
        result = 31 * result + right.hashCode();
        result = 31 * result + top.hashCode();
        result = 31 * result + bottom.hashCode();
        return result;
    }

    public interface ChangeListener {
        void onValuesChanged(GridValues values);

    }
}

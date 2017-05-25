package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.crashinvaders.common.MutableInt;

public class GridValues implements MutableInt.ChangeListener, Pool.Poolable {
    public final MutableInt left = new MutableInt();
    public final MutableInt right = new MutableInt();
    public final MutableInt top = new MutableInt();
    public final MutableInt bottom = new MutableInt();

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

    @Override
    public void onValueChanged(MutableInt value) {
        if (listeners.size > 0) {
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

    public interface ChangeListener {
        void onValuesChanged(GridValues values);

    }
}

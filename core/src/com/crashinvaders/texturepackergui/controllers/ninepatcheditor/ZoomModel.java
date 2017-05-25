package com.crashinvaders.texturepackergui.controllers.ninepatcheditor;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class ZoomModel implements Pool.Poolable {
    private final float[] scales = new float[]{0.75f, 1f, 1.25f, 1.5f, 2f, 3f, 5f, 10f, 20f};
    private int index;

    private final Array<ChangeListener> listeners = new Array<>();

    public float[] getScales() {
        return scales;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        if (this.index == index) return;

        this.index = MathUtils.clamp(index, 0, scales.length-1);

        if (listeners.size > 0) {
            for (ChangeListener listener : listeners) {
                listener.onZoomIndexChanged(index, scales[this.index]);
            }
        }
    }

    public void addListener(ChangeListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(ChangeListener listener) {
        this.listeners.removeValue(listener, true);
    }

    @Override
    public void reset() {
        index = 0;
        listeners.clear();
    }

    public interface ChangeListener {
        void onZoomIndexChanged(int zoomIndex, float scale);
    }
}

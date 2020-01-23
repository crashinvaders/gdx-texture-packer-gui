package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public abstract class SyncPool<T> extends Pool<T> {

    public SyncPool() {
        super();
    }

    public SyncPool(int initialCapacity) {
        super(initialCapacity);
    }

    public SyncPool(int initialCapacity, int max) {
        super(initialCapacity, max);
    }

    @Override
    synchronized
    public T obtain() {
        return super.obtain();
    }

    @Override
    synchronized
    public void free(T object) {
        super.free(object);
    }

    @Override
    synchronized
    protected void reset(T object) {
        super.reset(object);
    }

    @Override
    synchronized
    public void freeAll(Array<T> objects) {
        super.freeAll(objects);
    }

    @Override
    synchronized
    public void clear() {
        super.clear();
    }

    @Override
    synchronized
    public int getFree() {
        return super.getFree();
    }

    @Override
    synchronized
    final protected T newObject() {
        return newObjectInternal();
    }

    protected abstract T newObjectInternal();
}
package com.crashinvaders.texturepackergui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import java.io.File;
import java.util.List;

public class DragDropManager {

    private final Array<Listener> listeners = new Array<>();

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.removeValue(listener, true);
    }

    public void handleFileDrop(int screenX, int screenY, Array<FileHandle> files) {
        for (int i = 0; i < listeners.size; i++) {
            listeners.get(i).handleFileDrop(screenX, screenY, files);
        }
    }

    public interface Listener {
        void handleFileDrop(int screenX, int screenY, Array<FileHandle> files);
    }

    public static abstract class ListenerAdapter implements Listener {
        @Override
        public void handleFileDrop(int screenX, int screenY, Array<FileHandle> files) { }
    }
}

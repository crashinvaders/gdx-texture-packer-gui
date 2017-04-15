package com.crashinvaders.texturepackergui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import java.io.File;
import java.util.List;

public class DragDropManager {
    private static final Array<FileHandle> tmpFiles = new Array<>();

    private final Array<Listener> listeners = new Array<>();

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.removeValue(listener, true);
    }

    public void onDragStarted(int screenX, int screenY) {
        for (int i = 0; i < listeners.size; i++) {
            listeners.get(i).onDragStarted(screenX, screenY);
        }
    }

    public void onDragMoved(int screenX, int screenY) {
        for (int i = 0; i < listeners.size; i++) {
            listeners.get(i).onDragMoved(screenX, screenY);
        }
    }

    public void onDragFinished() {
        for (int i = 0; i < listeners.size; i++) {
            listeners.get(i).onDragFinished();
        }
    }

    public void handleFileDrop(int screenX, int screenY, List<File> files) {
        for (File file : files) {
            tmpFiles.add(new FileHandle(file));
        }
        for (int i = 0; i < listeners.size; i++) {
            listeners.get(i).handleFileDrop(screenX, screenY, tmpFiles);
        }
        tmpFiles.clear();
    }

    public interface Listener {
        void onDragStarted(int screenX, int screenY);
        void onDragMoved(int screenX, int screenY);
        void onDragFinished();
        void handleFileDrop(int screenX, int screenY, Array<FileHandle> files);
    }

    public static abstract class ListenerAdapter implements Listener {
        @Override
        public void onDragStarted(int screenX, int screenY) { }
        @Override
        public void onDragMoved(int screenX, int screenY) { }
        @Override
        public void onDragFinished() { }
        @Override
        public void handleFileDrop(int screenX, int screenY, Array<FileHandle> files) { }
    }
}

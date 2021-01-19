package com.crashinvaders.common;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.Disposable;
import com.crashinvaders.common.basisu.BasisuTextureData;
import com.crashinvaders.texturepackergui.controllers.model.FileTypeType;

/** Simple utility class that starts loading texture asynchronously immediately after creation. */
public class AsyncTextureLoader implements Disposable {

    private final FileHandle textureFile;
    private final CompletionListener listener;

    volatile private boolean disposed;

    public AsyncTextureLoader(FileHandle textureFile, CompletionListener listener) {
        this.textureFile = textureFile;
        this.listener = listener;

        startLoadingProcess();
    }

    @Override
    public void dispose() {
        disposed = true;
    }

    private void startLoadingProcess() {
        new Thread(() -> {
            if (disposed) {
                reportListenerError(null);
                return;
            }

            final TextureData textureData;

            try {
                if (FileTypeType.BASIS.key.equals(textureFile.extension())) {
                    textureData = new BasisuTextureData(textureFile, 0, 0);
                } else {
                    textureData = TextureData.Factory
                            .loadFromFile(textureFile, Pixmap.Format.RGBA8888, false);
                }
                if (!textureData.isPrepared()) textureData.prepare();
            } catch (Exception e) {
                reportListenerError(e);
                return;
            }

            if (disposed) {
                textureData.disposePixmap();
                reportListenerError(null);
                return;
            }

            Gdx.app.postRunnable(() -> {
                if (disposed) {
                    textureData.disposePixmap();
                    listener.onTextureLoadFailed(null);
                    return;
                }

                Texture texture = new Texture(textureData);
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

                listener.onTextureLoaded(texture);
            });
        }).start();
    }

    private void reportListenerError(final Exception e) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                listener.onTextureLoadFailed(e);
            }
        });
    }

    public interface CompletionListener {
        void onTextureLoaded(Texture texture);
        void onTextureLoadFailed(Exception e);
    }
}

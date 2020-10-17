package com.crashinvaders;

import com.badlogic.gdx.Gdx;

import static com.badlogic.gdx.Net.HttpResponse;
import static com.badlogic.gdx.Net.HttpResponseListener;

/** By default {@link HttpResponseListener} receives all callbacks in a dedicated thread (not render thread),
 * this class synchronizes all callbacks by posting them to the main thread queue. */
public abstract class SyncHttpResponseListener implements HttpResponseListener {

    @Override
    public void handleHttpResponse(final HttpResponse httpResponse) {
        try {
            handleResponseAsync(httpResponse);
        } catch (final Exception e) {
            Gdx.app.postRunnable(() -> onFailed(e));
            return;
        }

        Gdx.app.postRunnable(() -> handleResponseSync(httpResponse));
    }

    @Override
    public void failed(final Throwable t) {
        Gdx.app.postRunnable(() -> onFailed(t));
    }

    @Override
    public void cancelled() {
        Gdx.app.postRunnable(() -> onCancelled());
    }

    /** Called in an async thread.
     * During that call a connection to the host is up and
     * you should obtain any data that you will need later
     * to process response inside synchronous {@link #handleResponseSync(HttpResponse)} call.
     * If any exceptions will be thrown during the process, {@link #onFailed(Throwable)} will be called. */
    protected abstract void handleResponseAsync(HttpResponse httpResponse) throws Exception;
    /** Called in the main thread.
     * Process any data here that you obtained during async
     * {@link #handleResponseAsync(HttpResponse)} call. */
    protected abstract void handleResponseSync(HttpResponse httpResponse);
    protected abstract void onFailed(Throwable t);
    protected abstract void onCancelled();
}

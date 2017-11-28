package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.crashinvaders.common.stringencriptor.Base64StringEncryptor;
import com.crashinvaders.common.stringencriptor.CompositeStringEncryptor;
import com.crashinvaders.common.stringencriptor.StringEncryptor;
import com.crashinvaders.common.stringencriptor.XorStringEncryptor;
import com.crashinvaders.texturepackergui.events.TinifyServicePropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.TinifyServicePropertyChangedEvent.Property;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.tinify.AccountException;
import com.tinify.Exception;
import com.tinify.Tinify;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Provides functionality to compress images using TinyPng or TinyJpeg
 */
@Component
public class TinifyService {
    public static final String PREF_NAME = "tinify.xml";
    public static final String PREF_KEY_API_KEY = "api_key";
    public static final String PREF_KEY_COMPRESSION_COUNT = "compression_count";

    private final StringEncryptor encryptor = new CompositeStringEncryptor(
            new XorStringEncryptor("tinify"),
            new Base64StringEncryptor());

    @Inject EventDispatcher eventDispatcher;

    private Preferences prefs;
    private ExecutorService executorService;

    @Initiate void initialize() {
        prefs = Gdx.app.getPreferences(PREF_NAME);
        executorService = Executors.newSingleThreadExecutor();

        Tinify.setKey(encryptor.decrypt(prefs.getString(PREF_KEY_API_KEY)));
        Tinify.setCompressionCount(prefs.getInteger(PREF_KEY_COMPRESSION_COUNT, Tinify.compressionCount()));

        eventDispatcher.postEvent(new TinifyServicePropertyChangedEvent(Property.API_KEY));
        eventDispatcher.postEvent(new TinifyServicePropertyChangedEvent(Property.COMPRESSION_COUNT));
    }

    public synchronized String getApiKey() {
        return Tinify.key();
    }

    public int getCompressionCount() {
        return Tinify.compressionCount();
    }

    public synchronized void setApiKey(String apiKey) {
        Tinify.setKey(apiKey);
        prefs.putString(PREF_KEY_API_KEY, encryptor.encrypt(apiKey)).flush();
        eventDispatcher.postEvent(new TinifyServicePropertyChangedEvent(Property.API_KEY));
    }

    public void validateApiKey(final ValidationListener validationListener) {
        executorService.submit(new ValidationCheckRunnable(validationListener));
    }

    /** WARNING: blocking call, use separate thread */
    public void compressImageSync(FileHandle fileHandle) throws IOException {
        Tinify.fromFile(fileHandle.path()).toFile(fileHandle.path());

        updateCompressionCount();
    }

    private synchronized void updateCompressionCount() {
        Gdx.app.postRunnable(updateCompressionCountRunnable);
    }

    public interface ValidationListener {
        void onValid();
        void onInvalid();
        void onError(Exception e);
    }

    private static class ValidationCheckRunnable implements Runnable {
        private final ValidationListener validationListener;

        public ValidationCheckRunnable(ValidationListener validationListener) {
            this.validationListener = validationListener;
        }

        @Override
        public void run() {
            try {
                final boolean result = Tinify.validate();
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (result)
                            validationListener.onValid();
                        else
                            validationListener.onInvalid();
                    }
                });
            } catch (final AccountException e) {
                validationListener.onInvalid();
            } catch (final Exception e) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        validationListener.onError(e);
                    }
                });
            }
        }
    }

    private Runnable updateCompressionCountRunnable = new Runnable() {
        @Override
        public void run() {
            prefs.putInteger(PREF_KEY_COMPRESSION_COUNT, Tinify.compressionCount());
            prefs.flush();
            eventDispatcher.postEvent(new TinifyServicePropertyChangedEvent(Property.API_KEY));
        }
    };

}
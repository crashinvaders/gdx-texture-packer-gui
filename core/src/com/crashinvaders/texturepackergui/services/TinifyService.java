package com.crashinvaders.texturepackergui.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
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

    private Preferences prefs;

    private String apiKey;
    private ExecutorService executorService;

    @Initiate void initialize() {
        prefs = Gdx.app.getPreferences(PREF_NAME);
        apiKey = prefs.getString(PREF_KEY_API_KEY);

        Tinify.setKey(apiKey);

        executorService = Executors.newSingleThreadExecutor();
    }

    public synchronized String getApiKey() {
        return apiKey;
    }

    public synchronized void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        prefs.putString(PREF_KEY_API_KEY, apiKey).flush();

        Tinify.setKey(apiKey);
    }

    public void validateApiKey(final ValidationListener validationListener) {
        executorService.submit(new ValidationCheckRunnable(validationListener));
    }

    /** WARNING: blocking call, use separate thread */
    public void compressImageSync(FileHandle fileHandle) throws IOException {
        Tinify.fromFile(fileHandle.path()).toFile(fileHandle.path());
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
}
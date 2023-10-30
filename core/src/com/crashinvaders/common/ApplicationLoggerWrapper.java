package com.crashinvaders.common;

import com.badlogic.gdx.ApplicationLogger;

public class ApplicationLoggerWrapper implements ApplicationLogger {

    protected final ApplicationLogger wrapped;

    public ApplicationLoggerWrapper(ApplicationLogger wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void log(String tag, String message) {
        wrapped.log(tag, message);
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        wrapped.log(tag, message, exception);
    }

    @Override
    public void error(String tag, String message) {
        wrapped.error(tag, message);
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        wrapped.error(tag, message, exception);
    }

    @Override
    public void debug(String tag, String message) {
        wrapped.debug(tag, message);
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        wrapped.debug(tag, message, exception);
    }
}

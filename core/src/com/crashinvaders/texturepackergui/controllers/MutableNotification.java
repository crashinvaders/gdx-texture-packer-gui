package com.crashinvaders.texturepackergui.controllers;

public enum MutableNotification {
    NEW_VERSION_IS_AVAILABLE("ignore_version_update_notification", "mnNewVersionIsAvailable"),
    BASIS_RECOMMENDED_SETTINGS("ignore_basisu_compat_notif", "mnBasisRecommendedSettings"),
    ;

    public final String prefKey;
    public final String settingsDescI18nKey;

    MutableNotification(String prefKey, String settingsDescI18nKey) {
        this.prefKey = prefKey;
        this.settingsDescI18nKey = settingsDescI18nKey;
    }
}

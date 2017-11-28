package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.crashinvaders.common.Version;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.events.ShowToastEvent;
import com.crashinvaders.texturepackergui.events.VersionUpdateCheckEvent;
import com.crashinvaders.texturepackergui.controllers.versioncheck.VersionCheckService;
import com.crashinvaders.texturepackergui.controllers.versioncheck.VersionData;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.toast.ToastTable;

/** Checks if new version is available (on application launch) */
@Component
public class InitialUpdateCheckService {
    private static final String TAG = InitialUpdateCheckService.class.getSimpleName();
    private static final String PREF_KEY_IGNORE_NOTIFICATION = "ignore_version_update_notification";

    @Inject InterfaceService interfaceService;
    @Inject EventDispatcher eventDispatcher;
    @Inject VersionCheckService versionCheckService;

    private Preferences prefs;

    @Initiate(priority = Integer.MIN_VALUE) void init() {
        prefs = Gdx.app.getPreferences(AppConstants.PREF_NAME_COMMON);

        // Initiate update checking operation. We will listen for its result and once get it, we stop our activity.
        versionCheckService.requestVersionCheck();
    }

    @OnEvent(VersionUpdateCheckEvent.class) boolean onEvent(VersionUpdateCheckEvent event) {
        switch (event.getAction()) {
            case FINISHED_ERROR:
            case FINISHED_UP_TO_DATE:
                // Everything appears to be fine, stop listening for updates
                return OnEvent.REMOVE;
            case FINISHED_UPDATE_AVAILABLE:
                showUpdateNotification(event.getLatestVersion());
                return OnEvent.REMOVE;
        }
        return OnEvent.KEEP;
    }

    private void showUpdateNotification(VersionData version) {
        // Check if current version should be ignored
        try {
            String ignoreVersionSt = prefs.getString(PREF_KEY_IGNORE_NOTIFICATION, null);
            if (ignoreVersionSt != null) {
                Version ignoreVersion = new Version(ignoreVersionSt);
                if (ignoreVersion.equals(version.version)) {
                    Gdx.app.log(TAG, "Update is available " + version.version + ", but notification is muted for that version.");
                    return;
                }
            }
        } catch (IllegalArgumentException e) {
            // If something went wrong during ignore version checking, we simply ignore it
            e.printStackTrace();
        }

        LmlParser parser = interfaceService.getParser();
        ToastActions toastActions = new ToastActions(prefs, version);

        parser.getData().addArgument("newVersionCode", version.getVersion().toString());
        parser.getData().addArgument("newVersionUrl", version.getUrl());
        parser.getData().addActionContainer(ToastActions.class.getSimpleName(), toastActions);
        Actor content = parser.parseTemplate(Gdx.files.internal("lml/toastNewVersionAvailable.lml")).first();
        parser.getData().removeActionContainer(ToastActions.class.getSimpleName());

        ToastTable toastTable = new ToastTable();
        toastTable.add(content).grow();
        toastActions.setToastTable(toastTable);

        eventDispatcher.postEvent(new ShowToastEvent()
                .content(toastTable)
                .duration(ShowToastEvent.DURATION_INDEFINITELY));
    }

    public static class ToastActions implements ActionContainer {

        private final Preferences prefs;
        private final VersionData versionData;
        private ToastTable toastTable;

        public ToastActions(Preferences prefs, VersionData versionData) {
            this.prefs = prefs;
            this.versionData = versionData;
        }

        public void setToastTable(ToastTable toastTable) {
            this.toastTable = toastTable;
        }

        @LmlAction("muteNotifications") void muteNotifications() {
            Gdx.app.log(TAG, "Muting notifications for version: " + versionData.version);

            prefs.putString(PREF_KEY_IGNORE_NOTIFICATION, versionData.version.toString());
            prefs.flush();
            dismissToast();
        }

        @LmlAction("dismissToast") void dismissToast() {
            toastTable.fadeOut();
        }
    }
}

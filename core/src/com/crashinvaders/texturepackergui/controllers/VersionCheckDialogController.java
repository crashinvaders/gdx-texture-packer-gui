package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.events.VersionUpdateCheckEvent;
import com.crashinvaders.texturepackergui.services.versioncheck.VersionCheckService;
import com.crashinvaders.texturepackergui.services.versioncheck.VersionData;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.FocusManager;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;

@ViewDialog(id = "dialog_version_check", value = "lml/dialogVersionCheck.lml")
public class VersionCheckDialogController implements ActionContainer {

    @Inject VersionCheckService versionCheckService;

    @LmlActor("dialog") VisDialog dialog;
    @LmlActor("groupError") Group groupError;
    @LmlActor("groupChecking") Group groupChecking;
    @LmlActor("groupUpdateDetails") Group groupUpdateDetails;
    @LmlActor("groupUpToDate") Group groupUpToDate;
    @LmlActor("btnVisitUpdatePage") VisTextButton btnVisitUpdatePage;
    @LmlActor("lblVersionNew") VisLabel lblVersionNew;
    @LmlActor("lblVersionCurrent") VisLabel lblVersionCurrent;

    private VersionData latestVersion;

    @LmlAfter
    public void initialize() {
        lblVersionCurrent.setText(AppConstants.version.toString());

        launchVersionCheck();
    }

    @OnEvent(VersionUpdateCheckEvent.class) void onEvent(VersionUpdateCheckEvent event) {
        switch (event.getAction()) {
            case CHECK_STARTED:
                showGroup(groupChecking);
                break;
            case CHECK_FINISHED:
                break;
            case FINISHED_ERROR:
                showGroup(groupError);
                break;
            case FINISHED_UP_TO_DATE:
                showGroup(groupUpToDate);
                break;
            case FINISHED_UPDATE_AVAILABLE:
                showGroup(groupUpdateDetails);
                latestVersion = event.getLatestVersion();
                fillUpdateDetailsGroup(latestVersion);
                break;
        }
    }

    @LmlAction("launchVersionCheck") void launchVersionCheck() {
        versionCheckService.requestVersionCheck();
    }

    @LmlAction("navigateToUpdatePage") void navigateToUpdatePage() {
        if (latestVersion != null) {
            Gdx.net.openURI(latestVersion.getUrl());
        }
    }

    private void showGroup(Group group) {
        groupError.setVisible(false);
        groupChecking.setVisible(false);
        groupUpdateDetails.setVisible(false);
        groupUpToDate.setVisible(false);

        group.setVisible(true);
    }

    private void fillUpdateDetailsGroup(VersionData versionData) {
        lblVersionNew.setText(versionData.getVersion().toString());
        FocusManager.switchFocus(btnVisitUpdatePage.getStage(), btnVisitUpdatePage);
    }
}

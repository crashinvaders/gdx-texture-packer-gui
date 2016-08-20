package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.crashinvaders.texturepackergui.services.versioncheck.VersionCheckService;
import com.crashinvaders.texturepackergui.services.versioncheck.VersionData;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;

@ViewDialog(id = "dialog_version_check", value = "lml/dialogVersionCheck.lml")
public class VersionCheckDialogController implements ActionContainer {

    @Inject VersionCheckService versionCheckService;

    @ViewStage Stage stage;

    @LmlActor("dialog") VisDialog dialog;
    @LmlActor("groupError") Group groupError;
    @LmlActor("groupChecking") Group groupChecking;
    @LmlActor("groupUpdateDetails") Group groupUpdateDetails;
    @LmlActor("groupUpToDate") Group groupUpToDate;
    @LmlActor("lblVersionName") VisLabel lblVersionName;
    @LmlActor("lblVersionDescription") VisLabel lblVersionDescription;

    private VersionData latestVersion;

    @LmlAfter
    public void initialize() {
        launchVersionCheck();
    }

    @LmlAction("launchVersionCheck") void launchVersionCheck() {
        showGroup(groupChecking);

//        stage.addAction(Actions.delay(1f, Actions.run(new Runnable() {
//            @Override
//            public void run() {
//                showGroup(groupUpToDate);
//            }
//        })));

        versionCheckService.obtainLatestVersionInfo(new VersionCheckService.VersionCheckListener() {
            @Override
            public void onResult(VersionData data) {
                latestVersion = data;
                if (versionCheckService.isVersionNever(data)) {
                    showGroup(groupUpdateDetails);
                    fillUpdateDetailsGroup(latestVersion);
                } else {
                    showGroup(groupUpToDate);
                }
            }
            @Override
            public void onError(Throwable throwable) {
                showGroup(groupError);
                latestVersion = null;
            }
        });
    }

    @LmlAction("navigateToUpdatePage") void navigateToUpdatePage() {
        if (versionCheckService != null) {
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
        lblVersionName.setText(versionData.getName());
        lblVersionDescription.setText(versionData.getDescription());
    }
}

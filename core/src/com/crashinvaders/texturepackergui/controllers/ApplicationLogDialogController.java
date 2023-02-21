package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.utils.CommonUtils;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.kiwi.util.common.Strings;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;

import java.io.File;

@ViewDialog(id="application_log", value = "lml/dialogApplicationLog.lml")
public class ApplicationLogDialogController implements ActionContainer {
    private static final String TAG = ApplicationLogDialogController.class.getSimpleName();

    @LmlActor("lblMessage") Label lblMessage;

    private String appLog = "";

    @LmlAfter void initView() {
        this.appLog = CommonUtils.retrieveApplicationLog();
        String messageText = appLog;

        if (Strings.isBlank(appLog)) {
            messageText = "[red]Failed to retrieve the application log :([]";
        }

        lblMessage.setText(messageText);
    }

    @LmlAction("copyToClipboard")
    void copyToClipboard() {
        Gdx.app.getClipboard().setContents(appLog);
    }

    @LmlAction("locateLogFile")
    void btnLocateLogFile() {
        File logFile = AppConstants.logFile;
        if (logFile == null || !logFile.exists())
            return;

        File parentDir = logFile.getParentFile();

        App.inst().getSystemFileOpener().openFile(new FileHandle(parentDir));

//        try {
//            Desktop.getDesktop().open(parentDir);
//        } catch (IOException e) {
//            Gdx.app.error(TAG, "Error opening " + parentDir, e);
//        }
    }

    /** Displays the dialog if the InterfaceService is initialized. */
    public static void show() {
        InterfaceService interfaceService = App.inst().getInterfaceService();
        if (interfaceService == null) {
            return;
        }

        interfaceService.showDialog(ApplicationLogDialogController.class);
    }
}

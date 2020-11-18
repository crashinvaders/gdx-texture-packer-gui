package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.utils.CommonUtils;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;

@ViewDialog(id="dialog_error", value = "lml/dialogError.lml")
public class ErrorDialogController implements ActionContainer {
    private static final String LOG = ErrorDialogController.class.getSimpleName();

    @LmlActor("lblMessage") Label lblMessage;

    private String errorMessage;

    public void setError(Throwable error) {
        StringBuilder sb = new StringBuilder();
        sb.append("[text-red]").append(CommonUtils.fetchMessageStack(error)).append("[]");
        sb.append("\n");
        sb.append(CommonUtils.obtainStackTrace(error));

        errorMessage = sb.toString();
    }

    public void setError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @LmlAfter void initView() {
        if (errorMessage == null) {
            Gdx.app.error(LOG, "Error wasn't specified", new IllegalStateException());
            return;
        }

        lblMessage.setText(errorMessage);
    }

    @LmlAction("copyToClipboard") void copyToClipboard() {
        Gdx.app.getClipboard().setContents(errorMessage);
    }

    /** Displays the dialog if the InterfaceService is initialized. */
    public static void show(Exception e) {
        InterfaceService interfaceService = App.inst().getInterfaceService();
        if (interfaceService == null) {
            return;
        }

        ErrorDialogController errorDialog = (ErrorDialogController)App.inst().getContext().getComponent(ErrorDialogController.class);
        errorDialog.setError(e);
        interfaceService.showDialog(ErrorDialogController.class);
    }
}

package com.crashinvaders.texturepackergui.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.crashinvaders.texturepackergui.events.TinifyServicePropertyChangedEvent;
import com.crashinvaders.texturepackergui.controllers.TinifyService;
import com.crashinvaders.texturepackergui.utils.WidgetUtils;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.annotation.OnEvent;
import com.github.czyzby.autumn.mvc.component.i18n.LocaleService;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.mvc.stereotype.ViewDialog;
import com.github.czyzby.autumn.mvc.stereotype.ViewStage;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.action.ActionContainer;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.tinify.Exception;

@ViewDialog(id = "dialog_comp_tinify", value = "lml/compression/dialogTinify.lml")
public class TinifyCompDialogController implements ActionContainer {
    private static final String TAG = TinifyCompDialogController.class.getSimpleName();

    @Inject InterfaceService interfaceService;
    @Inject LocaleService localeService;
    @Inject TinifyService tinifyService;
    @Inject ErrorDialogController errorDialogController;

    @ViewStage Stage stage;

    @LmlActor("edtApiKey") VisTextField edtApiKey;
    @LmlActor("lblCompressionCount") VisLabel lblCompressionCount;

    @LmlAfter
    public void initialize() {
        updateValuesFromTinifyService();
    }

    @OnEvent(TinifyServicePropertyChangedEvent.class)
    void onTinifyPropertyChangedEvent(TinifyServicePropertyChangedEvent event) {
        if (stage != null) {
            updateValuesFromTinifyService();
        }
    }

    @LmlAction("onApiKeyChanged") void onApiKeyChanged() {
        tinifyService.setApiKey(edtApiKey.getText());
    }

    @LmlAction("registerNewUser") void registerNewUser() {
        Gdx.net.openURI("https://tinypng.com/developers");
    }

    @LmlAction("validateApiKey") void validateApiKey() {
        final VisWindow pleaseWaitDialog = (VisWindow) interfaceService.getParser().parseTemplate(Gdx.files.internal("lml/compression/dialogTinifyValidation.lml")).first();
        final VisImageButton btnClose = WidgetUtils.obtainCloseButton(pleaseWaitDialog);
        btnClose.setGenerateDisabledImage(true);
        btnClose.setDisabled(true);
        pleaseWaitDialog.setCenterOnAdd(true);
        pleaseWaitDialog.pack();
        stage.addActor(pleaseWaitDialog.fadeIn());

        tinifyService.validateApiKey(new TinifyService.ValidationListener() {
            @Override
            public void onValid() {
                Gdx.app.log(TAG, "Tinify API key validation succeed");

                pleaseWaitDialog.findActor("groupChecking").setVisible(false);
                pleaseWaitDialog.findActor("groupValid").setVisible(true);
                pleaseWaitDialog.closeOnEscape();
                btnClose.setDisabled(false);
            }

            @Override
            public void onInvalid() {
                Gdx.app.log(TAG, "Tinify API key validation failed");

                pleaseWaitDialog.findActor("groupChecking").setVisible(false);
                pleaseWaitDialog.findActor("groupInvalid").setVisible(true);
                pleaseWaitDialog.closeOnEscape();
                btnClose.setDisabled(false);
            }

            @Override
            public void onError(Exception e) {
                Gdx.app.error(TAG, "Error during Tinify API key validation", e);

                pleaseWaitDialog.fadeOut();
                errorDialogController.setError(e);
                interfaceService.showDialog(errorDialogController.getClass());
            }
        });
    }

    private void updateValuesFromTinifyService() {
        edtApiKey.setText(tinifyService.getApiKey());
        lblCompressionCount.setText(localeService.getI18nBundle().format("dCompCompressionCount", tinifyService.getCompressionCount()));
    }
}
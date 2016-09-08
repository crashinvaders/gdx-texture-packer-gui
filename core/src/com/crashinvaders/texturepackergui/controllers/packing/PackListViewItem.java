package com.crashinvaders.texturepackergui.controllers.packing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.github.czyzby.lml.scene2d.ui.reflected.AnimatedImage;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

class PackListViewItem extends Container<VisTable> {

    private final PackModel pack;
    private final VisTable view;
    private final AnimatedImage imgStateIndicator;
    private final VisLabel lblPackName;
    private final VisLabel lblState;
    private final VisImageButton btnLog;

    private String log;

    public PackListViewItem(PackModel pack, VisTable view) {
        this.pack = pack;
        this.view = view;

        setActor(view);
        fill();

        imgStateIndicator = view.findActor("imgStateIndicator");
        lblPackName = view.findActor("lblPackName");
        lblState = view.findActor("lblState");
        btnLog = view.findActor("btnLog");

        btnLog.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showLogWindow();
            }
        });

        lblPackName.setText(pack.getCanonicalName());
    }

    public void setLog(String log) {
            this.log = log;
        btnLog.setDisabled(false);
    }

    public void setToError(Exception e) {
        imgStateIndicator.setFrames(Array.with(VisUI.getSkin().getDrawable("custom/ic-proc-error")));
        imgStateIndicator.setCurrentFrame(0);
    }

    public void setToSuccess() {
        imgStateIndicator.setFrames(Array.with(VisUI.getSkin().getDrawable("custom/ic-proc-success")));
        imgStateIndicator.setCurrentFrame(0);
    }

    public void showLogWindow() {
        if (log == null) return;

        VisDialog dialog = (VisDialog)App.inst().getInterfaceService().getParser().parseTemplate(Gdx.files.internal("lml/dialogPackingLog.lml")).first();
        Container containerLog = dialog.findActor("containerLog");
        final VisScrollPane scrLog = dialog.findActor("scrLog");
        final Button btnCopyToClipboard = dialog.findActor("btnCopyToClipboard");

        VisLabel lblLog = new VisLabel("", "small") {
//            @Override
//            protected void sizeChanged() {
//                super.sizeChanged();
//                // Scroll down scroller
//                scrLog.setScrollPercentY(1f);
//            }
        };
        lblLog.setAlignment(Align.topLeft);
        lblLog.setWrap(true);
        lblLog.setText(log);
        containerLog.setActor(lblLog);

        btnCopyToClipboard.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(log), null);
            }
        });

        dialog.getTitleLabel().setText(App.inst().getI18n().format("dialogTitlePackLog", pack.getName()));
        dialog.show(getStage());
        getStage().setScrollFocus(scrLog);
    }
}

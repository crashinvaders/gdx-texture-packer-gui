package com.crashinvaders.texturepackergui.controllers.packing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.github.czyzby.kiwi.util.common.Strings;
import com.github.czyzby.lml.scene2d.ui.reflected.AnimatedImage;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;

class ProcessingNodeListViewItem extends Container<VisTable> {

    private final PackProcessingNode node;
    private final VisTable view;
    private final AnimatedImage imgStateIndicator;
    private final VisLabel lblPackName;
    private final VisLabel lblMetadata;
    private final VisImageButton btnLog;

    public ProcessingNodeListViewItem(PackProcessingNode node, VisTable view) {
        this.node = node;
        this.view = view;

        setActor(view);
        fill();

        imgStateIndicator = view.findActor("imgStateIndicator");
        lblPackName = view.findActor("lblPackName");
        lblMetadata = view.findActor("lblMetadata");
        btnLog = view.findActor("btnLog");

        btnLog.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showLogWindow();
            }
        });

        lblPackName.setText(node.getPack().getCanonicalName() + "[light-grey]" + node.getPack().getSettings().scaleSuffix[0]);
    }

    public void onFinishProcessing() {
        // Enable log button
        btnLog.setDisabled(false);

        parseMetadata();
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
        final String log = node.getLog();
        if (Strings.isEmpty(log)) return;

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
                Gdx.app.getClipboard().setContents(log);
            }
        });

        dialog.getTitleLabel().setText(App.inst().getI18n().format("dialogTitlePackLog", node.getPack().getName()));
        dialog.show(getStage());
        getStage().setScrollFocus(scrLog);
    }

    @SuppressWarnings("unchecked")
    private void parseMetadata() {
        if (node.hasMetadata(PackProcessingNode.META_COMPRESSION_RATE)) {
            float compression = node.getMetadata(PackProcessingNode.META_COMPRESSION_RATE);
            lblMetadata.setText(String.format("[light-grey]%+5.2f%%[]", compression*100f));
        }
    }
}

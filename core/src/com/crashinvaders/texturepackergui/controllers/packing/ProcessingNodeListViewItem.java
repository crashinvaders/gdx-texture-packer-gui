package com.crashinvaders.texturepackergui.controllers.packing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.github.czyzby.kiwi.util.common.Strings;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.scene2d.ui.reflected.AnimatedImage;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import com.sun.javafx.binding.StringFormatter;
import com.sun.xml.internal.fastinfoset.util.StringArray;

class ProcessingNodeListViewItem extends Container<VisTable> {

    private final LmlParser parser;
    private final PackProcessingNode node;
    private final VisTable view;
    private final AnimatedImage imgStateIndicator;
    private final VisLabel lblPackName;
//    private final VisLabel lblMetadata;
    private final VisTable tableMetadata;
    private final VisImageButton btnLog;

    public ProcessingNodeListViewItem(LmlParser parser, PackProcessingNode node) {
        this.parser = parser;
        this.node = node;
        this.view = (VisTable) parser.parseTemplate(Gdx.files.internal("lml/dialogPackingListItem.lml")).first();

        setActor(view);
        fill();

        imgStateIndicator = view.findActor("imgStateIndicator");
        lblPackName = view.findActor("lblPackName");
//        lblMetadata = view.findActor("lblMetadata");
        tableMetadata = view.findActor("tableMetadata");
        btnLog = view.findActor("btnLog");

        btnLog.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showLogWindow();
            }
        });

        lblPackName.setText(node.getPack().getCanonicalName() + "[light-grey]" + node.getPack().getScaleFactors().first().getSuffix());
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
            String compressionPercents = String.format("[light-grey]%+.2f%%[]", compression * 100f);

            Group metadataRoot = (Group)parser.parseTemplate(Gdx.files.internal("lml/dialogPackingMetaRegularItem.lml")).first();
            VisLabel lblContent = metadataRoot.findActor("lblContent");
            VisImage imgIcon = metadataRoot.findActor("imgIcon");
            lblContent.setText(compressionPercents);
            imgIcon.setDrawable(VisUI.getSkin(), "custom/ic-small-compression");
            tableMetadata.add(metadataRoot);
        }
        if (node.hasMetadata(PackProcessingNode.META_FILE_SIZE)) {
            long size = node.getMetadata(PackProcessingNode.META_FILE_SIZE);
            String  sizeMb = String.format("[light-grey]%.2fMB[]", size / 1048576.0); // 1024 x 1024

            Group metadataRoot = (Group)parser.parseTemplate(Gdx.files.internal("lml/dialogPackingMetaRegularItem.lml")).first();
            VisLabel lblContent = metadataRoot.findActor("lblContent");
            VisImage imgIcon = metadataRoot.findActor("imgIcon");
            lblContent.setText(sizeMb);
            imgIcon.setDrawable(VisUI.getSkin(), "custom/ic-small-weight");
            tableMetadata.add(metadataRoot);
        }
    }
}

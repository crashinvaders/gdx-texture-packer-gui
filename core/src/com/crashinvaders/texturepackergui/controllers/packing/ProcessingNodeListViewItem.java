package com.crashinvaders.texturepackergui.controllers.packing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.utils.packprocessing.PackProcessingNode;
import com.github.czyzby.kiwi.util.common.Strings;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.scene2d.ui.reflected.AnimatedImage;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;

import java.util.Locale;

class ProcessingNodeListViewItem extends Container<VisTable> {

    private final LmlParser parser;
    private final PackProcessingNode node;
    private final VisTable view;
    private final AnimatedImage imgStateIndicator;
    private final VisLabel lblPackName;
    //    private final VisLabel lblMetadata;
    private final VisTable tableMetadata;
    private final VisImageButton btnLog;

    public ProcessingNodeListViewItem(LmlParser parser, PackProcessingNode node, int orderNum) {
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

        if (orderNum%2 == 0) {
            view.setBackground("packingItemBg");
        }

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
        Container<Actor> containerLog = dialog.findActor("containerLog");
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
        I18NBundle i18nBundle = parser.getData().getDefaultI18nBundle();

        if (node.hasMetadata(PackProcessingNode.META_TOTAL_TIME)) {
            double seconds = (Long)node.getMetadata(PackProcessingNode.META_TOTAL_TIME) / 1000000000D;

            RegularMetadataItemViewHolder viewHolder = new RegularMetadataItemViewHolder(parser);
            viewHolder.lblContent.setText(String.format(Locale.US, "%.1f", seconds));
            viewHolder.imgIcon.setDrawable(VisUI.getSkin(), "custom/ic-small-time");
            viewHolder.createTooltip("dPackingMetaTotalTime");
            tableMetadata.add(viewHolder.root);
        }
        if (node.hasMetadata(PackProcessingNode.META_FILE_SIZE)) {
            long bytes = node.getMetadata(PackProcessingNode.META_FILE_SIZE);
            double megabytes = bytes / 1048576.0;  // 1024 x 1024
            megabytes = Math.max(megabytes, 0.01); // To avoid cases when few KB displays as 0.00
            String  sizeMb = String.format(Locale.US, "%.2f%s", megabytes, i18nBundle.get("mb"));

            RegularMetadataItemViewHolder viewHolder = new RegularMetadataItemViewHolder(parser);
            viewHolder.lblContent.setText(sizeMb);
            viewHolder.imgIcon.setDrawable(VisUI.getSkin(), "custom/ic-small-weight");
            viewHolder.createTooltip("dPackingMetaTotalSize");
            tableMetadata.add(viewHolder.root);
        }
        if (node.hasMetadata(PackProcessingNode.META_COMPRESSION_RATE)) {
            float compression = node.getMetadata(PackProcessingNode.META_COMPRESSION_RATE);
            String compressionPercents = String.format(Locale.US, "%.2f%%", compression * -100f);

            RegularMetadataItemViewHolder viewHolder = new RegularMetadataItemViewHolder(parser);
            viewHolder.lblContent.setText(compressionPercents);
            viewHolder.imgIcon.setDrawable(VisUI.getSkin(), "custom/ic-small-compression");
            viewHolder.createTooltip("dPackingMetaCompression");
            tableMetadata.add(viewHolder.root);
        }
    }

    private static class RegularMetadataItemViewHolder {
        final LmlParser parser;
        final VisLabel lblContent;
        final VisImage imgIcon;
        final Group root;

        public RegularMetadataItemViewHolder(LmlParser parser) {
            root = (Group) parser.parseTemplate(Gdx.files.internal("lml/dialogPackingMetaRegularItem.lml")).first();
            this.parser = parser;
            lblContent = root.findActor("lblContent");
            imgIcon = root.findActor("imgIcon");
        }

        public void createTooltip(String i18nKey) {
            String text = parser.getData().getDefaultI18nBundle().get(i18nKey);

            final Tooltip tooltip = new Tooltip();
            tooltip.clearChildren(); // Removing empty cell with predefined paddings.
            tooltip.add(text).center().pad(0f, 4f, 2f, 4f);
            tooltip.pack();
            tooltip.setTarget(root);
        }
    }
}

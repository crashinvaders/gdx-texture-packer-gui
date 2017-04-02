package com.crashinvaders.texturepackergui.controllers.main.inputfiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.services.model.InputFile;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.LmlParser;
import com.kotcrab.vis.ui.util.adapter.ArrayAdapter;
import com.kotcrab.vis.ui.widget.Tooltip;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

class SourceFileSetAdapter extends ArrayAdapter<InputFile, VisTable> {

    private final LmlParser lmlParser;

    public SourceFileSetAdapter(LmlParser lmlParser) {
        super(new Array<InputFile>());
        this.lmlParser = lmlParser;

        setSelectionMode(SelectionMode.MULTIPLE);
        setItemsSorter(new SourceFileComparator());
    }

    public InputFile getSelected() {
        Array<InputFile> selection = getSelectionManager().getSelection();
        if (selection.size == 0) return null;
        return selection.first();
    }

    public ViewHolder getViewHolder(InputFile item) {
        if (indexOf(item) == -1) return null;

        return (ViewHolder) getView(item).getUserObject();
    }

    public ViewHolder getViewHolder(Actor view) {
        return (ViewHolder) view.getUserObject();
    }

    @Override
    protected VisTable createView(InputFile item) {
        ViewHolder viewHolder = new ViewHolder(lmlParser.getData().getDefaultSkin(), item);
        lmlParser.createView(viewHolder, Gdx.files.internal("lml/inputFileListItem.lml"));
        viewHolder.root.setUserObject(viewHolder);
        return viewHolder.root;
    }

    @Override
    protected void prepareViewBeforeAddingToTable(InputFile item, VisTable view) {
        super.prepareViewBeforeAddingToTable(item, view);
    }

    @Override
    protected void selectView(VisTable view) {
        ViewHolder viewHolder = (ViewHolder) view.getUserObject();
        viewHolder.setSelected(true);
    }

    @Override
    protected void deselectView(VisTable view) {
        ViewHolder viewHolder = (ViewHolder) view.getUserObject();
        viewHolder.setSelected(false);
    }

    public static class ViewHolder {
        @LmlActor("root") VisTable root;
        @LmlActor("lblName") VisLabel lblName;
        @LmlActor("imgTypeIndicator") Image imgTypeIndicator;

        private final Skin skin;
        private final InputFile inputFile;
        private final Tooltip tooltip;

        private boolean selected = false;
        private boolean pathProcessed = false;

        public ViewHolder(Skin skin, InputFile inputFile) {
            this.skin = skin;
            this.inputFile = inputFile;

            tooltip = new Tooltip();
            tooltip.setAppearDelayTime(0.25f);
        }

        @LmlAfter void initView() {
            tooltip.setTarget(lblName);

            root.pack();
            updateViewData();
        }

        public void remapData() {
            pathProcessed = false;
            updateViewData();
        }

        public void updateViewData() {
            processPathText();

            tooltip.setText(inputFile.getFileHandle().path());

            String imgName = "custom/ic-fileset-";
            if (inputFile.isDirectory()) {
                imgName += "dir";
            } else {
                imgName += "file";
            }
            switch (inputFile.getType()) {
                case Input:
                    break;
                case Ignore:
                    imgName += "-ignore";
                    break;
            }
            imgTypeIndicator.setDrawable(skin.getDrawable(imgName));
        }

        public void setSelected(boolean selected) {
            if (this.selected == selected) return;
            this.selected = selected;

            root.setBackground(selected ? skin.getDrawable("padded-list-selection") : null);
        }

        public InputFile getInputFile() {
            return inputFile;
        }

        private void processPathText() {
            if (pathProcessed) return;
            pathProcessed = true;

            String pathText = inputFile.getFileHandle().path();

            // Cut the last slash
            int lastSlashIndex = pathText.lastIndexOf("/");
            if (lastSlashIndex == pathText.length()-1) {
                pathText = pathText.substring(0, lastSlashIndex);
            }

            // Try to shorten path by cutting slash divided pieces starting from beginning
            GlyphLayout glyphLayout = lblName.getGlyphLayout();
            boolean pathCut = false;
            while (true) {
                glyphLayout.setText(lblName.getStyle().font, pathText, lblName.getStyle().fontColor, 0f, lblName.getLabelAlign(), false);
                if (glyphLayout.width < (lblName.getWidth() - 8)) break;  // -8 is extra ellipsis ".../" space

                int slashIndex = pathText.indexOf("/");
                if (slashIndex == -1) break;
                pathText = pathText.substring(slashIndex+1);
                pathCut = true;
            }

            // Add ellipsis if path was cut
            if (pathCut) {
                pathText = ".../"+pathText;
            }

            lastSlashIndex = pathText.lastIndexOf("/");
            if (lastSlashIndex > 0) {
                int dotLastIndex = pathText.lastIndexOf(".");

                StringBuilder sb = new StringBuilder();
                sb.append("[light-grey]");
                sb.append(pathText.substring(0, lastSlashIndex + 1));
                sb.append("[]");
                if (!inputFile.isDirectory() && dotLastIndex > 0 && dotLastIndex - lastSlashIndex > 1) {
                    // Grey out extension text
                    sb.append(pathText.substring(lastSlashIndex + 1, dotLastIndex));
                    sb.append("[light-grey]");
                    sb.append(pathText.substring(dotLastIndex));
                    sb.append("[]");
                } else {
                    // No extension
                    sb.append(pathText.substring(lastSlashIndex + 1));
                }
                pathText = sb.toString();
            }

            lblName.setText(pathText);
        }
    }
}

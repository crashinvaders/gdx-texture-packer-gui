package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.controllers.model.PackModel;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.LmlParser;
import com.kotcrab.vis.ui.util.adapter.ArrayAdapter;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

import java.util.Iterator;

public class PackListAdapter extends ArrayAdapter<PackModel, VisTable> {

    private final LmlParser lmlParser;

    public PackListAdapter(LmlParser lmlParser) {
        super(new Array<PackModel>());
        this.lmlParser = lmlParser;

        setSelectionMode(SelectionMode.SINGLE);
    }

    public PackModel getSelected() {
    Array<PackModel> selection = getSelectionManager().getSelection();
        if (selection.size == 0) return null;
        return selection.first();
    }

    public ViewHolder getViewHolder(PackModel item) {
        if (indexOf(item) == -1) return null;

        return (ViewHolder) getView(item).getUserObject();
    }

    public ViewHolder getViewHolder(Actor view) {
        return (ViewHolder) view.getUserObject();
    }

    @Override
    protected VisTable createView(PackModel item) {
        ViewHolder viewHolder = new ViewHolder(lmlParser.getData().getDefaultSkin(), item);
        lmlParser.createView(viewHolder, Gdx.files.internal("lml/packListItem.lml"));
        viewHolder.root.setUserObject(viewHolder);
        return viewHolder.root;
    }

    @Override
    protected void prepareViewBeforeAddingToTable(PackModel item, VisTable view) {
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

        private final Skin skin;
        private final PackModel pack;
        private boolean selected;

        public ViewHolder(Skin skin, PackModel pack) {
            this.skin = skin;
            this.pack = pack;
        }

        @LmlAfter void initView() {
            updateViewData();
        }

        public void updateViewData() {
            lblName.setText(pack.getCanonicalName());
        }

        public void setSelected(boolean selected) {
            if (this.selected == selected) return;
            this.selected = selected;

            root.setBackground(selected ? skin.getDrawable("padded-list-selection") : null);
        }

        public PackModel getPack() {
            return pack;
        }
    }
}

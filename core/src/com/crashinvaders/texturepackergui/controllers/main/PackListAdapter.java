package com.crashinvaders.texturepackergui.controllers.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.annotation.LmlAfter;
import com.github.czyzby.lml.parser.LmlParser;
import com.kotcrab.vis.ui.util.adapter.ArrayAdapter;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

import java.util.Iterator;

//TODO use improved code from VisUI 1.2.4 (for selection change events)
public class PackListAdapter extends ArrayAdapter<PackModel, VisTable> {

    private final LmlParser lmlParser;

    private SelectionChangedListener selectionChangeListener;
    private boolean programmaticSelectionChangeEvents = true;

    private Runnable selectionChangedNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            if (programmaticSelectionChangeEvents && selectionChangeListener != null) {
                selectionChangeListener.onSelectionChanged(getSelected());
            }
        }
    };

    public PackListAdapter(LmlParser lmlParser) {
        super(new Array<PackModel>());
        this.lmlParser = lmlParser;

        setSelectionMode(SelectionMode.SINGLE);
    }

    public void setSelectionChangeListener(SelectionChangedListener selectionChangeListener) {
        this.selectionChangeListener = selectionChangeListener;
    }

    public void setProgrammaticSelectionChangeEvents(boolean changeEvents) {
        this.programmaticSelectionChangeEvents = changeEvents;
    }

    public PackModel getSelected() {
    Array<PackModel> selection = getSelectionManager().getSelection();
        if (selection.size == 0) return null;
        return selection.first();
}

    public void setSelected(PackModel item, boolean fireChangeEvent) {
        Array<PackModel> selection = getSelectionManager().getSelection();
        if (selection.size > 0 && selection.first() == item) return;

        boolean prevChangeEventsValue = this.programmaticSelectionChangeEvents;
        this.programmaticSelectionChangeEvents = fireChangeEvent;
        getSelectionManager().deselectAll();
        if (item != null) {
            getSelectionManager().select(item);
        }
        this.programmaticSelectionChangeEvents = prevChangeEventsValue;
    }

    public ViewHolder getViewHolder(PackModel item) {
        if (indexOf(item) == -1) return null;

        return (ViewHolder) getView(item).getUserObject();
    }

    @Override
    public void itemsChanged() {
        super.itemsChanged();

        Array<PackModel> selection = getSelectionManager().getSelection();
        Iterator<PackModel> iterator = selection.iterator();
        while (iterator.hasNext()) {
            PackModel item = iterator.next();
            if (indexOf(item) == -1) {
                iterator.remove();
            }
        }
    }

    @Override
    protected void itemRemoved(PackModel item) {
        super.itemRemoved(item);

        getSelectionManager().getSelection().removeValue(item, true);
    }

    @Override
    protected VisTable createView(PackModel item) {
        ViewHolder viewHolder = new ViewHolder(lmlParser.getData().getDefaultSkin(), item);
        lmlParser.createView(viewHolder, Gdx.files.internal("lml/packListItem.lml"));
        viewHolder.root.setUserObject(viewHolder);
        return viewHolder.root;
    }

    @Override
    protected void selectView(VisTable view) {
        ViewHolder viewHolder = (ViewHolder) view.getUserObject();
        viewHolder.setSelected(true);

        notifySelectionChangeListener();
    }

    @Override
    protected void deselectView(VisTable view) {
        ViewHolder viewHolder = (ViewHolder) view.getUserObject();
        viewHolder.setSelected(false);

        notifySelectionChangeListener();
    }

    private void notifySelectionChangeListener() {
        Gdx.app.postRunnable(selectionChangedNotificationRunnable);
    }

    public interface SelectionChangedListener {
        void onSelectionChanged(PackModel pack);
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
    }
}

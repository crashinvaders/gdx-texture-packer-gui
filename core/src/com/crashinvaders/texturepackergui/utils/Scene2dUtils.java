package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.kotcrab.vis.ui.util.adapter.AbstractListAdapter;
import com.kotcrab.vis.ui.util.adapter.ArrayAdapter;
import com.kotcrab.vis.ui.util.adapter.ItemAdapter;
import com.kotcrab.vis.ui.widget.ListView;

@SuppressWarnings("WeakerAccess")
public class Scene2dUtils {
    private static final Vector2 tmpVec2 = new Vector2();
    private static final InputEvent tmpInputEvent = new InputEvent();

    public static void simulateClick(Actor actor, int button, int pointer) {
        simulateClick(actor, button, pointer, 0f, 0f);
    }

    public static void simulateClick(Actor actor, int button, int pointer, float localX, float localY) {
        Vector2 pos = actor.stageToLocalCoordinates(tmpVec2.set(localX, localY));
        simulateClickGlobal(actor, button, pointer, pos.x, pos.y);
    }

    public static void simulateClickGlobal(Actor actor, int button, int pointer, float stageX, float stageY) {
        InputEvent event = tmpInputEvent;
        event.setStage(actor.getStage());
        event.setButton(button);
        event.setPointer(pointer);
        event.setStageX(stageX);
        event.setStageY(stageY);
        event.setType(InputEvent.Type.touchDown);
        actor.fire(event);
        event.setType(InputEvent.Type.touchUp);
        actor.fire(event);
        event.reset();
    }

    /**
     * @param scrollPane widget will be scrolled
     * @param list must be child of scrollPane
     */
    public static void scrollDownToSelectedListItem(ScrollPane scrollPane, List list) {
        if (list.getSelectedIndex() == -1) return;

        float y = list.getHeight() - (list.getSelectedIndex() * list.getItemHeight()) - list.getItemHeight();
        float height = list.getItemHeight();

        scrollPane.scrollTo(0, y, 0, height);
    }

    /**
     * @param listView should has {@link ItemAdapter}
     * @param item to scroll to
     */
    public static void scrollDownToSelectedListItem(ListView listView, Object item) {
        ItemAdapter adapter = (ItemAdapter) listView.getAdapter();
        Actor itemView = adapter.getView(item);
        listView.getScrollPane().scrollTo(0, itemView.getY(), 0, itemView.getHeight());
    }
}

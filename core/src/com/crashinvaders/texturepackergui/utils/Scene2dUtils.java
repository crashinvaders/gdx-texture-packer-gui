package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.kotcrab.vis.ui.util.adapter.ItemAdapter;
import com.kotcrab.vis.ui.widget.ListView;

@SuppressWarnings("WeakerAccess")
public class Scene2dUtils {
    private static final String FILE_PATH_ELLIPSIS = ".../";
    private static final Vector2 tmpVec2 = new Vector2();
    private static final InputEvent tmpInputEvent = new InputEvent();
    private static final GlyphLayout glyphLayout = new GlyphLayout();

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

    public static String ellipsisFilePath(String filePath, BitmapFont font, float maxWidth) {
        glyphLayout.setText(font, FILE_PATH_ELLIPSIS);
        float ellipsisWidth = glyphLayout.width;

        // Cut the last slash
        int lastSlashIndex = filePath.lastIndexOf("/");
        if (lastSlashIndex == filePath.length()-1) {
            filePath = filePath.substring(0, lastSlashIndex);
        }

        // Try to shorten path by cutting slash divided pieces starting from beginning
        boolean pathCut = false;
        while (true) {
            glyphLayout.setText(font, filePath);
            if (glyphLayout.width < (maxWidth - ellipsisWidth)) break;

            int slashIndex = filePath.indexOf("/");
            if (slashIndex == -1) break;
            filePath = filePath.substring(slashIndex+1);
            pathCut = true;
        }
        glyphLayout.reset();

        // Add ellipsis if path was cut
        if (pathCut) {
            filePath = FILE_PATH_ELLIPSIS + filePath;
        }

        return filePath;
    }

    public static String colorizeFilePath(String filePath, boolean directory, String colorPath, String colorFileName) {
        int lastSlashIndex = filePath.lastIndexOf("/");
        if (lastSlashIndex > 0) {
            int dotLastIndex = filePath.lastIndexOf(".");

            StringBuilder sb = new StringBuilder();
            sb.append("[").append(colorPath).append("]");
            sb.append(filePath.substring(0, lastSlashIndex + 1));
            sb.append("[").append(colorFileName).append("]");
            if (!directory && dotLastIndex > 0 && dotLastIndex - lastSlashIndex > 1) {
                // Grey out extension text
                sb.append(filePath.substring(lastSlashIndex + 1, dotLastIndex));
                sb.append("[").append(colorPath).append("]");
                sb.append(filePath.substring(dotLastIndex));
            } else {
                // No extension
                sb.append(filePath.substring(lastSlashIndex + 1));
            }
            return sb.toString();
        }
        //TODO handle case when there is no slashes
        return filePath;
    }
}

package com.crashinvaders.common.scene2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.kotcrab.vis.ui.util.adapter.ItemAdapter;
import com.kotcrab.vis.ui.util.adapter.ListAdapter;
import com.kotcrab.vis.ui.widget.ListView;
import com.kotcrab.vis.ui.widget.VisTextField;

@SuppressWarnings("WeakerAccess")
public class Scene2dUtils {
    public static final String TAG_INJECT_FIELDS = "InjectActorFields";
    private static final String FILE_PATH_ELLIPSIS = ".../";
    private static final Vector2 tmpVec2 = new Vector2();
    private static final GlyphLayout glyphLayout = new GlyphLayout();

    public static void simulateClick(Actor actor) {
        simulateClick(actor, 0, 0, 0f, 0f);
    }

    public static void simulateClick(Actor actor, int button, int pointer, float localX, float localY) {
        Vector2 pos = actor.stageToLocalCoordinates(tmpVec2.set(localX, localY));
        simulateClickGlobal(actor, button, pointer, pos.x, pos.y);
    }

    public static void simulateClickGlobal(Actor actor, int button, int pointer, float stageX, float stageY) {
        InputEvent event = Pools.obtain(InputEvent.class);
        event.setStage(actor.getStage());
        event.setRelatedActor(actor);
        event.setTarget(actor);
        event.setStageX(stageX);
        event.setStageY(stageY);
        event.setButton(button);
        event.setPointer(pointer);

        event.setType(InputEvent.Type.touchDown);
        actor.notify(event, false);
        event.setType(InputEvent.Type.touchUp);
        actor.notify(event, false);

        Pools.free(event);
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
    public static <T> void scrollDownToSelectedListItem(ListView<T> listView, T item) {
        ListAdapter<T> rawAdapter = listView.getAdapter();
        if (!(rawAdapter instanceof ItemAdapter)) return;

        @SuppressWarnings("unchecked")
        ItemAdapter<T> adapter = (ItemAdapter<T>) rawAdapter;
        Actor itemView = adapter.getView(item);
        listView.getScrollPane().scrollTo(0, itemView.getY(), 0, itemView.getHeight());
    }

    /** Checks if text could fully fit the specified width. */
    public static boolean isTextFitWidth(BitmapFont font, float width, String text) {
        glyphLayout.setText(font, text);
        return glyphLayout.width <= width;
    }

    /** Checks if the text could fit the current textField's width. */
    public static boolean isTextFitTextField(VisTextField textField, String text) {
        float availableWidth = textField.getWidth();
        Drawable fieldBg = textField.getStyle().background;
        if (fieldBg != null) {
            availableWidth = availableWidth - fieldBg.getLeftWidth() - fieldBg.getRightWidth();
        }
        BitmapFont font = textField.getStyle().font;
        return isTextFitWidth(font, availableWidth, text);
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

            // Cut ".9" suffix along with the extension.
            if (dotLastIndex > 2 && filePath.startsWith(".9", dotLastIndex-2)) {
                dotLastIndex -= 2;
            }

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

    /** Injects actors from group into target's fields annotated with {@link InjectActor} using reflection. */
    public static void injectActorFields(Object target, Group group) {
        Class<?> handledClass = target.getClass();
        while (handledClass != null && !handledClass.equals(Object.class)) {
            for (final Field field : ClassReflection.getDeclaredFields(handledClass)) {
                if (field != null && field.isAnnotationPresent(InjectActor.class)) {
                    try {
                        InjectActor annotation = field.getDeclaredAnnotation(InjectActor.class).getAnnotation(InjectActor.class);
                        String actorName = annotation.value();
                        if (actorName.length() == 0) {
                            actorName = field.getName();
                        }
                        Actor actor = group.findActor(actorName);
                        if (actor == null && actorName.equals(group.getName())) {
                            actor = group;
                        }
                        if (actor == null) {
                            Gdx.app.error(TAG_INJECT_FIELDS, "Can't find actor with name: " + actorName + " in group: " + group + " to inject into: " + target);
                        } else {
                            field.setAccessible(true);
                            field.set(target, actor);
                        }
                    } catch (final ReflectionException exception) {
                        Gdx.app.error(TAG_INJECT_FIELDS, "Unable to set value into field: " + field + " of object: " + target, exception);
                    }
                }
            }
            handledClass = handledClass.getSuperclass();
        }
    }

    public static boolean containsLocal(Actor actor, float x, float y) {
        return x > 0f && y > 0f && x < actor.getWidth() && y < actor.getHeight();
    }
}

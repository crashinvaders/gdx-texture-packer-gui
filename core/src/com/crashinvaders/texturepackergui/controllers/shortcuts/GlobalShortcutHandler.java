package com.crashinvaders.texturepackergui.controllers.shortcuts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.controllers.GlobalActions;
import com.github.czyzby.autumn.annotation.Component;
import com.github.czyzby.autumn.annotation.Destroy;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.lml.parser.action.ActorConsumer;

@Component
public class GlobalShortcutHandler extends InputAdapter {
    private static String TAG = GlobalShortcutHandler.class.getSimpleName();

    @Inject GlobalActions globalActions;
    @Inject InterfaceService interfaceService;

    private ArrayMap<String, Shortcut> shortcuts;

    @Initiate void initialize() {
        App.inst().getInput().addProcessor(this, -1000);

        ShortcutParser shortcutParser = new ShortcutParser();
        Array<Shortcut> shortcutArray = shortcutParser.parse(Gdx.files.internal("hotkeys.txt"));

        shortcuts = new ArrayMap<>(shortcutArray.size);
        for (Shortcut shortcut : shortcutArray) {
            shortcuts.put(shortcut.getActionName(), shortcut);
        }
    }

    @Destroy void dispose() {
        App.inst().getInput().removeProcessor(this);
    }

    public String resolveShortcutString(String actionName) {
        Shortcut shortcut = shortcuts.get(actionName);
        if (shortcut == null) {
            return null;
        }
        return shortcut.toShortcutExpression();
    }

    @Override
    public boolean keyDown(int keycode) {
        for (int i = 0; i < shortcuts.size; i++) {
            Shortcut shortcut = shortcuts.getValueAt(i);

            if (shortcut.getKeyCode() == keycode) {
                if (shortcut.isShift() && !shift()) continue;
                if (shortcut.isControl() && !ctrl()) continue;
                if (shortcut.isAlt() && !alt()) continue;
                if (shortcut.isSym() && !sym()) continue;

                executeShortcutAction(shortcut);
                return true;
            }
        }
        return false;
    }

    private void executeShortcutAction(Shortcut shortcut) {
        String actionName = shortcut.getActionName();
        ActorConsumer<?, Object> actorConsumer = interfaceService.getParser().parseAction(actionName);
        if (actorConsumer != null) {
            actorConsumer.consume(null);
        } else {
            Gdx.app.error(TAG, "Can't find action for shortcut: " + actionName);
        }
    }

    private boolean shift() {
        return Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
    }
    private boolean ctrl() {
        return Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
    }
    private boolean alt() {
        return Gdx.input.isKeyPressed(Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Keys.ALT_RIGHT);
    }
    private boolean sym() {
        return Gdx.input.isKeyPressed(Keys.SYM);
    }
}

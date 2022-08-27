package com.crashinvaders.texturepackergui.controllers.shortcuts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.AppConstants;
import com.crashinvaders.texturepackergui.controllers.GlobalActions;
import com.crashinvaders.texturepackergui.controllers.ToastFactory;
import com.crashinvaders.texturepackergui.utils.CommonUtils;
import com.github.czyzby.autumn.annotation.Destroy;
import com.github.czyzby.autumn.annotation.Initiate;
import com.github.czyzby.autumn.annotation.Inject;
import com.github.czyzby.autumn.mvc.component.ui.InterfaceService;
import com.github.czyzby.autumn.processor.event.EventDispatcher;
import com.github.czyzby.lml.parser.action.ActorConsumer;

public class GlobalShortcutHandler extends InputAdapter {
    private static String TAG = GlobalShortcutHandler.class.getSimpleName();

    @Inject InterfaceService interfaceService;
    @Inject EventDispatcher eventDispatcher;
    @Inject GlobalActions globalActions;
    @Inject ToastFactory toastFactory;

    private final ArrayMap<String, Shortcut> shortcuts = new ArrayMap<>();
    private final ShortcutParser shortcutParser = new ShortcutParser();

    private final Array<Exception> parseErrors = new Array<>();

    @Initiate void initialize() {
        App.inst().getInput().addProcessor(this, 10);

        reloadShortcuts();
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

    public Array<Exception> getParsingErrors() {
        return parseErrors;
    }

    public boolean hasParsingErrors() {
        return parseErrors.size > 0;
    }

    @Override
    public boolean keyDown(int keycode) {
        // Do not handle modifier keys.
        if (Shortcut.isModifierKey(keycode))
            return false;

        int modifierBits = Shortcut.evalModifierBits(shift(), ctrl(), alt(), sym());

        for (int i = 0; i < shortcuts.size; i++) {
            Shortcut shortcut = shortcuts.getValueAt(i);

            if (shortcut.getKeyCode() == keycode && shortcut.tryMatchModifierBits(modifierBits)) {

                executeShortcutAction(shortcut);
                return true;
            }
        }
        return false;
    }

    public Array<Shortcut> getShortcuts() {
        return shortcuts.values().toArray();
    }

    public void reloadShortcuts() {
        parseErrors.clear();
        shortcuts.clear();

        // Built-in shortcuts.
        parseAndAddShortcutFile(Gdx.files.internal("hotkeys_default.txt"));

        // Debug shortcuts.
        if (App.inst().getParams().debug) {
            parseAndAddShortcutFile(Gdx.files.internal("hotkeys_debug.txt"));
        }

        // User custom shortcuts.
        FileHandle userShortcutFile = Gdx.files.external(AppConstants.EXTERNAL_DIR + "/hotkeys_user.txt");
        if (userShortcutFile.exists()) {
            Array<Shortcut> shortcuts = parseAndAddShortcutFile(userShortcutFile);
            for (Shortcut shortcut : shortcuts) {
                shortcut.setUserDefined(true);
            }
        }
    }

    private void parseShortcutFileSafe(FileHandle fileHandle) {
        Gdx.app.log(TAG, "Parsing shortcut file: " + fileHandle);

        Array<Shortcut> shortcutArray = shortcutParser.parseSafe(fileHandle);

        for (Shortcut shortcut : shortcutArray) {
            shortcuts.put(shortcut.getActionName(), shortcut);
        }
    }

    private Array<Shortcut> parseAndAddShortcutFile(FileHandle fileHandle) {
        Gdx.app.log(TAG, "Parsing shortcut file: " + fileHandle);

        Array<Shortcut> shortcutArray = null;
        try {
            shortcutArray = shortcutParser.parse(fileHandle);
        } catch (ShortcutParser.ShortcutParseException e) {
            parseErrors.add(e);
            e.printStackTrace();
            showParseErrorToast(e);
            return shortcutArray;
        }

        for (Shortcut shortcut : shortcutArray) {
            shortcuts.put(shortcut.getActionName(), shortcut);
        }

        return shortcutArray;
    }

    private void showParseErrorToast(ShortcutParser.ShortcutParseException error) {
        String errorMessage = CommonUtils.fetchMessageStack(error, "\n");
        toastFactory.showErrorToast(error, errorMessage);
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

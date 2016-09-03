package com.crashinvaders.texturepackergui.services.shortcuts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashMap;

import static com.crashinvaders.texturepackergui.utils.CommonUtils.splitAndTrim;
import static com.crashinvaders.texturepackergui.utils.FileUtils.loadTextFromFileSilent;

public class ShortcutParser {
    private static final String TAG = ShortcutParser.class.getSimpleName();
    private static final String COMMENT_PREFIX = "//";
    private static final ShortcutComparator shortcutComparator = new ShortcutComparator();

    private final HashMap<String, Integer> keyCodes;

    public ShortcutParser() {
        keyCodes= prepareKeyCodes();
    }

    public Array<Shortcut> parse(FileHandle fileHandle) {
        String hotkeyMarkup = loadTextFromFileSilent(fileHandle);
        return parse(hotkeyMarkup);
    }

    public Array<Shortcut> parse(String hotkeyMarkup) {
        Array<Shortcut> shortcuts = new Array<>();

        Array<String> lines = splitAndTrim(hotkeyMarkup);
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith(COMMENT_PREFIX)) continue;

            Array<String> segments = splitAndTrim(line, ":");
            if (segments.size != 2) {
                Gdx.app.error(TAG, "Wrong format in line: " + line);
                continue;
            }

            String actionName = segments.get(0);
            String shortcutExpr = segments.get(1);

            if (actionName.length() == 0) {
                Gdx.app.error(TAG, "Empty shortcut action in line: " + line);
                continue;
            }

            Shortcut shortcut = parseShortcut(actionName, shortcutExpr);
            if (shortcut != null) {
                shortcuts.add(shortcut);
            }
        }

        shortcuts.sort(shortcutComparator);
        return shortcuts;
    }

    private Shortcut parseShortcut(String actionName, String shortcutExpr) {
        Array<String> keys = splitAndTrim(shortcutExpr, "\\+");

        if (keys.size == 0) {
            Gdx.app.error(TAG, "Wrong shortcut expression: " + actionName + ": " + shortcutExpr);
            return null;
        }

        Shortcut shortcut = new Shortcut(actionName);

        for (String key : keys) {
            String upCaseKey = key.toUpperCase();

            switch (upCaseKey) {
                case "SHIFT": {
                    shortcut.setKey(Input.Keys.SHIFT_LEFT);
                    continue;
                }
                case "ALT": {
                    shortcut.setKey(Input.Keys.ALT_LEFT);
                    continue;
                }
                case "CTRL":
                case "CONTROL": {
                    shortcut.setKey(Input.Keys.CONTROL_LEFT);
                    continue;
                }
                case "CMD":
                case "COMMAND":
                case "SUPER":
                case "META":
                case "WIN":
                case "WINDOWS": {
                    shortcut.setKey(Input.Keys.SYM);
                    continue;
                }
            }

            Integer code = keyCodes.get(upCaseKey);
            if (code == null) {
                Gdx.app.error(TAG, "Unknown key \"" + key + "\" in line: " + actionName + ": " + shortcutExpr);
                continue;
            }
            shortcut.setKey(code);
        }

        if (shortcut.getKeyCode() == Shortcut.EMPTY_KEY) {
            Gdx.app.error(TAG, "Shortcut should have at some key in addition to modifiers: " + actionName + ": " + shortcutExpr);
            return null;
        }

        return shortcut;
    }

    private static HashMap<String, Integer> prepareKeyCodes() {
        HashMap<String, Integer> keyCodes = new HashMap<>();

        Field[] fields = Input.Keys.class.getDeclaredFields();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && field.getType()==Integer.TYPE) {
                try {
                    int code = field.getInt(null);
                    String name = field.getName();

                    keyCodes.put(name, code);
                } catch (IllegalAccessException ignore) { }
            }
        }
        return keyCodes;
    }

    public static class ShortcutComparator implements Comparator<Shortcut> {
        @Override
        public int compare(Shortcut l, Shortcut r) {
            return Integer.compare(r.getPriority(), l.getPriority());
        }
    }
}

package com.crashinvaders.texturepackergui.controllers.shortcuts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;

import static com.crashinvaders.texturepackergui.utils.CommonUtils.splitAndTrim;

public class ShortcutParser {
    private static final String TAG = ShortcutParser.class.getSimpleName();
    private static final String COMMENT_PREFIX = "#";
    private static final ShortcutComparator shortcutComparator = new ShortcutComparator();
    private static final ObjectMap<String, Integer> keyCodes = prepareKeyCodes();

    public Array<Shortcut> parse(FileHandle fileHandle) throws ShortcutParseException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(fileHandle.reader());
            return parseBuffer(br);
        } catch (IOException | GdxRuntimeException | ShortcutParseException e) {
            throw new ShortcutParseException("Error reading shortcut file: \"" + fileHandle.toString() + "\"", e);
        } finally {
            if (br != null) try { br.close(); } catch (IOException ignore) { }
        }
    }

    public Array<Shortcut> parseSafe(FileHandle fileHandle) {
        try {
            return parse(fileHandle);
        } catch (ShortcutParseException e) {
            Gdx.app.error(TAG, "Error reading shortcut file", e);
            return new Array<>();
        }
    }

    private Array<Shortcut> parseBuffer(BufferedReader bufferedReader) throws IOException, ShortcutParseException {
        Array<Shortcut> shortcuts = new Array<>();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0 || line.startsWith(COMMENT_PREFIX)) continue;

            Array<String> segments = splitAndTrim(line, ":");
            if (segments.size != 2) {
                Gdx.app.error(TAG, "Wrong format at line: " + line);
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

    private Shortcut parseShortcut(String actionName, String shortcutExpr) throws ShortcutParseException {
        Array<String> keys = splitAndTrim(shortcutExpr, "\\+");

        if (keys.size == 0) {
            throw new ShortcutParseException("Empty shortcut expression: \"" + actionName + ": " + shortcutExpr + "\"");
        }

        Shortcut shortcut = new Shortcut(actionName);

        for (String key : keys) {
            String upCaseKey = key.toUpperCase();

            switch (upCaseKey) {
                case "SHIFT": {
                    shortcut.setKeyCode(Input.Keys.SHIFT_LEFT);
                    continue;
                }
                case "ALT": {
                    shortcut.setKeyCode(Input.Keys.ALT_LEFT);
                    continue;
                }
                case "CTRL":
                case "CONTROL": {
                    shortcut.setKeyCode(Input.Keys.CONTROL_LEFT);
                    continue;
                }
                case "CMD":
                case "COMMAND":
                case "SUPER":
                case "META":
                case "WIN":
                case "WINDOWS": {
                    shortcut.setKeyCode(Input.Keys.SYM);
                    continue;
                }
            }

            Integer code = keyCodes.get(upCaseKey);

            if (code == null) {
                throw new ShortcutParseException("Unknown key \"" + key + "\" in the line: \"" + actionName + ": " + shortcutExpr + "\"");
            }

            if (shortcut.getKeyCode() != Shortcut.EMPTY_KEY) {
                throw new ShortcutParseException("Multiple keys are not allowed: \"" + actionName + ": " + shortcutExpr + "\"");
            }

            shortcut.setKeyCode(code);
        }

        if (shortcut.getKeyCode() == Shortcut.EMPTY_KEY) {
            throw new ShortcutParseException("Missing a key. Only modifier keys are defined: \"" + actionName + ": " + shortcutExpr + "\"");
        }

        return shortcut;
    }

    private static ObjectMap<String, Integer> prepareKeyCodes() {
        ObjectMap<String, Integer> keyCodes = new ObjectMap<>();

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

        keyCodes.put("0", 7);
        keyCodes.put("1", 8);
        keyCodes.put("2", 9);
        keyCodes.put("3", 10);
        keyCodes.put("4", 11);
        keyCodes.put("5", 12);
        keyCodes.put("6", 13);
        keyCodes.put("7", 14);
        keyCodes.put("8", 15);
        keyCodes.put("9", 16);

        return keyCodes;
    }

    public static class ShortcutComparator implements Comparator<Shortcut> {
        @Override
        public int compare(Shortcut l, Shortcut r) {
            return Integer.compare(r.getPriority(), l.getPriority());
        }
    }

    public static class ShortcutParseException extends Exception {

        public ShortcutParseException(String message) {
            super(message);
        }

        public ShortcutParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

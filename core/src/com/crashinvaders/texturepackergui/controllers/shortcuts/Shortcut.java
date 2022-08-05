package com.crashinvaders.texturepackergui.controllers.shortcuts;


import com.badlogic.gdx.Input;
import com.kotcrab.vis.ui.util.OsUtils;

@SuppressWarnings("PointlessBitwiseExpression")
public class Shortcut {
    public static final int EMPTY_KEY = -23;

    private static final int FLAG_SHIFT = 1<<0;
    private static final int FLAG_CONTROL = 1<<1;
    private static final int FLAG_ALT = 1<<2;
    private static final int FLAG_SYM = 1<<3;

    private final String actionName;
    private int keyCode = EMPTY_KEY;

    /**
     * Modifier key combination representation in raw bits (for fast matching).
     */
    private int modifierBits = 0;

    public Shortcut(String actionName) {
        this.actionName = actionName;
    }

    Shortcut setKey(int keyCode) {
        if (keyCode == Input.Keys.SHIFT_LEFT || keyCode == Input.Keys.SHIFT_RIGHT) {
            modifierBits ^= FLAG_SHIFT;
            return this;
        }
        if (keyCode == Input.Keys.CONTROL_LEFT || keyCode == Input.Keys.CONTROL_RIGHT) {
            modifierBits ^= FLAG_CONTROL;
            return this;
        }
        if (keyCode == Input.Keys.ALT_LEFT || keyCode == Input.Keys.ALT_RIGHT) {
            modifierBits ^= FLAG_ALT;
            return this;
        }
        if (keyCode == Input.Keys.SYM) {
            modifierBits ^= FLAG_SYM;
            return this;
        }

        this.keyCode = keyCode;
        return this;
    }

    public String getActionName() {
        return actionName;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public boolean isShift() {
        return (modifierBits & FLAG_SHIFT) != 0;
    }
    public boolean isControl() {
        return (modifierBits & FLAG_CONTROL) != 0;
    }
    public boolean isAlt() {
        return (modifierBits & FLAG_ALT) != 0;
    }
    public boolean isSym() {
        return (modifierBits & FLAG_SYM) != 0;
    }

    public int getPriority() {
        // Longer modifier key combinations needs to be processed first.
        return Integer.bitCount(modifierBits);
    }

    public String toShortcutExpression() {
        StringBuilder sb = new StringBuilder();
        if (isControl()) sb.append("Ctrl").append("+");
        if (isShift()) sb.append("Shift").append("+");
        if (isAlt()) sb.append("Alt").append("+");
        if (isSym()) sb.append(getSymKeyName()).append("+");

        sb.append(Input.Keys.toString(keyCode));

        return sb.toString();
    }

    public boolean tryMatchModifierBits(int otherBits) {
        return modifierBits == otherBits;
    }

    @Override
    public String toString() {
        return "[" + getActionName() + "] " + toShortcutExpression();
    }

    private String getSymKeyName() {
        if (OsUtils.isWindows()) {
            return "Win";
        }
        if (OsUtils.isMac()) {
            return "Cmd";
        }
        if (OsUtils.isUnix()) {
            return "Super";
        }
        return "Sym";
    }

    public static boolean isModifierKey(int keycode) {
        switch (keycode) {
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
            case Input.Keys.ALT_LEFT:
            case Input.Keys.ALT_RIGHT:
            case Input.Keys.CONTROL_LEFT:
            case Input.Keys.CONTROL_RIGHT:
            case Input.Keys.SYM:
                return true;
        }

        return false;
    }

    public static int evalModifierBits(boolean shift, boolean ctrl, boolean alt, boolean sym) {
        int modifierBits = 0;

        if (shift)
            modifierBits ^= FLAG_SHIFT;

        if (ctrl)
            modifierBits ^= FLAG_CONTROL;

        if (alt)
            modifierBits ^= FLAG_ALT;

        if (sym)
            modifierBits ^= FLAG_SYM;

        return modifierBits;
    }
}
